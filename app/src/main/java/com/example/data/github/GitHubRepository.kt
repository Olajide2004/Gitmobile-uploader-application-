package com.example.data.github

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Base64
import com.example.data.local.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.UUID

class GitHubRepository(
    private val context: Context,
    private val apiService: GitHubApiService,
    private val appPreferences: AppPreferences
) {

    private val _uploadProgress = MutableStateFlow(UploadProgress())
    val uploadProgress: StateFlow<UploadProgress> = _uploadProgress.asStateFlow()

    private fun getAuthHeader(token: String): String {
        val cleanToken = token.trim()
        return if (cleanToken.startsWith("Bearer ") || cleanToken.startsWith("token ")) {
            cleanToken
        } else {
            "Bearer $cleanToken"
        }
    }

    suspend fun verifyAndFetchUser(token: String): Result<GitHubUser> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAuthenticatedUser(getAuthHeader(token))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to verify token: HTTP ${response.code()}"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchUserRepos(token: String, username: String): Result<List<GitHubRepo>> = withContext(Dispatchers.IO) {
        try {
            val response = if (token.isNotBlank()) {
                apiService.getUserRepos(getAuthHeader(token))
            } else {
                apiService.getPublicReposForUser(null, username)
            }

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to fetch repositories (HTTP ${response.code()})"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchBranches(token: String, owner: String, repo: String): Result<List<GitHubBranch>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getBranches(getAuthHeader(token), owner, repo)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to fetch branches (HTTP ${response.code()})"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Recursively traverses an Android folder tree selected via SAF OpenDocumentTree
     * using standard Android DocumentsContract.
     */
    suspend fun scanDocumentTree(treeUri: Uri): List<FolderFileItem> = withContext(Dispatchers.IO) {
        val results = mutableListOf<FolderFileItem>()
        try {
            val rootDocId = DocumentsContract.getTreeDocumentId(treeUri)

            fun traverse(parentDocId: String, relativeParent: String) {
                val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocId)
                val projection = arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_MIME_TYPE,
                    DocumentsContract.Document.COLUMN_SIZE
                )

                context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                    val idCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                    val nameCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                    val mimeCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
                    val sizeCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE)

                    while (cursor.moveToNext()) {
                        val docId = if (idCol >= 0) cursor.getString(idCol) else continue
                        val name = if (nameCol >= 0) cursor.getString(nameCol) ?: "unnamed" else "unnamed"
                        val mime = if (mimeCol >= 0) cursor.getString(mimeCol) ?: "" else ""
                        val size = if (sizeCol >= 0 && !cursor.isNull(sizeCol)) cursor.getLong(sizeCol) else 0L
                        val relativePath = if (relativeParent.isEmpty()) name else "$relativeParent/$name"

                        if (mime == DocumentsContract.Document.MIME_TYPE_DIR) {
                            traverse(docId, relativePath)
                        } else {
                            val fileDocUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                            results.add(
                                FolderFileItem(
                                    name = name,
                                    relativePath = relativePath,
                                    sizeBytes = size,
                                    isDirectory = false,
                                    uriString = fileDocUri.toString()
                                )
                            )
                        }
                    }
                }
            }

            traverse(rootDocId, "")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        results
    }

    /**
     * Main Git Folder Upload Workflow using Git Trees, Blobs, and Commits API
     */
    suspend fun uploadFolder(
        token: String,
        owner: String,
        repo: String,
        branch: String,
        targetRepoFolderPath: String,
        commitMessage: String,
        files: List<FolderFileItem>
    ): Result<UploadHistoryItem> = withContext(Dispatchers.IO) {
        if (token.isBlank()) return@withContext Result.failure(Exception("GitHub Personal Access Token is required"))
        if (owner.isBlank()) return@withContext Result.failure(Exception("GitHub Username / Owner is required"))
        if (repo.isBlank()) return@withContext Result.failure(Exception("Target repository name is required"))
        if (files.isEmpty()) return@withContext Result.failure(Exception("No files selected in folder to upload"))

        val auth = getAuthHeader(token)
        val cleanFolder = targetRepoFolderPath.trim().trim('/')
        val totalFiles = files.size

        try {
            // 1. Get branch head commit SHA
            _uploadProgress.value = UploadProgress(
                phase = UploadPhase.SCANNING_FOLDER,
                statusText = "Checking branch $branch ref..."
            )

            val branchRefResp = apiService.getBranchRef(auth, owner, repo, branch)
            if (!branchRefResp.isSuccessful || branchRefResp.body() == null) {
                val err = branchRefResp.errorBody()?.string() ?: "Branch $branch not found in $owner/$repo"
                _uploadProgress.value = UploadProgress(phase = UploadPhase.FAILED, statusText = err)
                return@withContext Result.failure(Exception(err))
            }

            val parentCommitSha = branchRefResp.body()!!.gitObject.sha

            // 2. Get tree SHA of parent commit
            val commitDetailsResp = apiService.getCommitDetails(auth, owner, repo, parentCommitSha)
            if (!commitDetailsResp.isSuccessful || commitDetailsResp.body() == null) {
                val err = commitDetailsResp.errorBody()?.string() ?: "Failed to get commit details"
                _uploadProgress.value = UploadProgress(phase = UploadPhase.FAILED, statusText = err)
                return@withContext Result.failure(Exception(err))
            }
            val baseTreeSha = commitDetailsResp.body()!!.tree.sha

            // 3. Create Blobs for each file
            _uploadProgress.value = UploadProgress(
                phase = UploadPhase.UPLOADING_BLOBS,
                currentFile = 0,
                totalFiles = totalFiles,
                statusText = "Creating Git blobs for $totalFiles files..."
            )

            val treeItems = mutableListOf<TreeItemRequest>()

            for ((index, fileItem) in files.withIndex()) {
                val fileNum = index + 1
                val percent = fileNum.toFloat() / totalFiles.toFloat()
                _uploadProgress.value = UploadProgress(
                    phase = UploadPhase.UPLOADING_BLOBS,
                    currentFile = fileNum,
                    totalFiles = totalFiles,
                    fileName = fileItem.relativePath,
                    percent = percent,
                    statusText = "Uploading blob ($fileNum/$totalFiles): ${fileItem.relativePath}"
                )

                // Read file bytes
                val fileBytes: ByteArray = if (fileItem.rawBytes != null) {
                    fileItem.rawBytes
                } else if (fileItem.uriString != null) {
                    val uri = Uri.parse(fileItem.uriString)
                    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                    inputStream?.use { it.readBytes() } ?: ByteArray(0)
                } else {
                    ByteArray(0)
                }

                val base64Content = Base64.encodeToString(fileBytes, Base64.NO_WRAP)
                val blobResp = apiService.createBlob(auth, owner, repo, CreateBlobRequest(content = base64Content, encoding = "base64"))
                if (!blobResp.isSuccessful || blobResp.body() == null) {
                    val err = "Failed to upload blob for ${fileItem.name}: ${blobResp.errorBody()?.string()}"
                    _uploadProgress.value = UploadProgress(phase = UploadPhase.FAILED, statusText = err)
                    return@withContext Result.failure(Exception(err))
                }

                val blobSha = blobResp.body()!!.sha
                val fullRepoPath = if (cleanFolder.isEmpty()) {
                    fileItem.relativePath
                } else {
                    "$cleanFolder/${fileItem.relativePath}"
                }

                treeItems.add(
                    TreeItemRequest(
                        path = fullRepoPath,
                        mode = "100644",
                        type = "blob",
                        sha = blobSha
                    )
                )
            }

            // 4. Create new Git Tree
            _uploadProgress.value = UploadProgress(
                phase = UploadPhase.GENERATING_TREE,
                statusText = "Creating Git Tree for ${treeItems.size} items..."
            )

            val treeResp = apiService.createTree(
                auth,
                owner,
                repo,
                CreateTreeRequest(baseTree = baseTreeSha, tree = treeItems)
            )

            if (!treeResp.isSuccessful || treeResp.body() == null) {
                val err = "Failed to create tree: ${treeResp.errorBody()?.string()}"
                _uploadProgress.value = UploadProgress(phase = UploadPhase.FAILED, statusText = err)
                return@withContext Result.failure(Exception(err))
            }
            val newTreeSha = treeResp.body()!!.sha

            // 5. Create new Git Commit
            _uploadProgress.value = UploadProgress(
                phase = UploadPhase.CREATING_COMMIT,
                statusText = "Writing commit: \"$commitMessage\"..."
            )

            val commitResp = apiService.createCommit(
                auth,
                owner,
                repo,
                CreateCommitRequest(
                    message = commitMessage,
                    tree = newTreeSha,
                    parents = listOf(parentCommitSha)
                )
            )

            if (!commitResp.isSuccessful || commitResp.body() == null) {
                val err = "Failed to create commit: ${commitResp.errorBody()?.string()}"
                _uploadProgress.value = UploadProgress(phase = UploadPhase.FAILED, statusText = err)
                return@withContext Result.failure(Exception(err))
            }
            val newCommit = commitResp.body()!!
            val newCommitSha = newCommit.sha

            // 6. Update branch head ref
            _uploadProgress.value = UploadProgress(
                phase = UploadPhase.UPDATING_BRANCH,
                statusText = "Updating branch $branch to commit ${newCommitSha.take(7)}..."
            )

            val updateRefResp = apiService.updateRef(
                auth,
                owner,
                repo,
                branch,
                UpdateRefRequest(sha = newCommitSha, force = false)
            )

            if (!updateRefResp.isSuccessful) {
                val err = "Failed to update ref: ${updateRefResp.errorBody()?.string()}"
                _uploadProgress.value = UploadProgress(phase = UploadPhase.FAILED, statusText = err)
                return@withContext Result.failure(Exception(err))
            }

            val commitUrl = newCommit.htmlUrl ?: "https://github.com/$owner/$repo/commit/$newCommitSha"

            val historyItem = UploadHistoryItem(
                id = UUID.randomUUID().toString(),
                repo = "$owner/$repo",
                branch = branch,
                folderPath = if (cleanFolder.isEmpty()) "/" else cleanFolder,
                commitSha = newCommitSha,
                commitMessage = commitMessage,
                fileCount = totalFiles,
                timestamp = System.currentTimeMillis(),
                url = commitUrl
            )

            appPreferences.addUploadHistory(historyItem)

            _uploadProgress.value = UploadProgress(
                phase = UploadPhase.COMPLETED,
                currentFile = totalFiles,
                totalFiles = totalFiles,
                percent = 1f,
                statusText = "Folder committed successfully! SHA: ${newCommitSha.take(7)}"
            )

            Result.success(historyItem)
        } catch (e: Exception) {
            _uploadProgress.value = UploadProgress(
                phase = UploadPhase.FAILED,
                statusText = e.localizedMessage ?: "Upload failed"
            )
            Result.failure(e)
        }
    }

    fun resetProgress() {
        _uploadProgress.value = UploadProgress(phase = UploadPhase.IDLE)
    }
}
