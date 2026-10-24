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
            // 1. Check branch head commit SHA (handle empty or new repos gracefully)
            _uploadProgress.value = UploadProgress(
                phase = UploadPhase.SCANNING_FOLDER,
                statusText = "Checking branch $branch in $owner/$repo..."
            )

            var parentCommitSha: String? = null
            var baseTreeSha: String? = null

            val branchRefResp = apiService.getBranchRef(auth, owner, repo, branch)
            if (branchRefResp.isSuccessful && branchRefResp.body() != null) {
                parentCommitSha = branchRefResp.body()!!.gitObject.sha

                // Get base tree SHA from parent commit
                val commitDetailsResp = apiService.getCommitDetails(auth, owner, repo, parentCommitSha)
                if (commitDetailsResp.isSuccessful && commitDetailsResp.body() != null) {
                    baseTreeSha = commitDetailsResp.body()!!.tree.sha
                }
            }

            // 2. Create Blobs for each file
            _uploadProgress.value = UploadProgress(
                phase = UploadPhase.UPLOADING_BLOBS,
                currentFile = 0,
                totalFiles = totalFiles,
                statusText = "Uploading blobs for $totalFiles files..."
            )

            val treeItems = mutableListOf<TreeItemRequest>()

            for ((index, fileItem) in files.withIndex()) {
                val fileNum = index + 1
                val percent = fileNum.toFloat() / totalFiles.toFloat()

                // Sanitize path according to strict GitHub Git Tree rules
                val sanitizedPath = sanitizeGitPath(cleanFolder, fileItem.relativePath)
                if (sanitizedPath.isBlank()) continue

                _uploadProgress.value = UploadProgress(
                    phase = UploadPhase.UPLOADING_BLOBS,
                    currentFile = fileNum,
                    totalFiles = totalFiles,
                    fileName = sanitizedPath,
                    percent = percent,
                    statusText = "Uploading blob ($fileNum/$totalFiles): $sanitizedPath"
                )

                // Read file bytes
                val fileBytes: ByteArray = if (fileItem.rawBytes != null) {
                    fileItem.rawBytes
                } else if (fileItem.uriString != null) {
                    try {
                        val uri = Uri.parse(fileItem.uriString)
                        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                        inputStream?.use { it.readBytes() } ?: ByteArray(0)
                    } catch (e: Exception) {
                        ByteArray(0)
                    }
                } else {
                    ByteArray(0)
                }

                val base64Content = Base64.encodeToString(fileBytes, Base64.NO_WRAP)
                val blobResp = apiService.createBlob(
                    auth,
                    owner,
                    repo,
                    CreateBlobRequest(content = base64Content, encoding = "base64")
                )

                if (!blobResp.isSuccessful || blobResp.body() == null) {
                    val rawErr = blobResp.errorBody()?.string() ?: ""
                    val err = "Failed to upload blob for ${fileItem.name}: ${parseGitHubError(rawErr)}"
                    _uploadProgress.value = UploadProgress(phase = UploadPhase.FAILED, statusText = err)
                    return@withContext Result.failure(Exception(err))
                }

                val blobSha = blobResp.body()!!.sha
                treeItems.add(
                    TreeItemRequest(
                        path = sanitizedPath,
                        mode = "100644",
                        type = "blob",
                        sha = blobSha
                    )
                )
            }

            if (treeItems.isEmpty()) {
                val err = "No valid files found in selected folder to commit"
                _uploadProgress.value = UploadProgress(phase = UploadPhase.FAILED, statusText = err)
                return@withContext Result.failure(Exception(err))
            }

            // 3. Create new Git Tree (with automatic fallback to baseTree = null if merged tree fails)
            _uploadProgress.value = UploadProgress(
                phase = UploadPhase.GENERATING_TREE,
                statusText = "Creating Git Tree for ${treeItems.size} items..."
            )

            var treeResp = apiService.createTree(
                auth,
                owner,
                repo,
                CreateTreeRequest(baseTree = baseTreeSha?.takeIf { it.isNotBlank() }, tree = treeItems)
            )

            // If tree creation with base_tree failed, retry without baseTree
            if (!treeResp.isSuccessful && !baseTreeSha.isNullOrBlank()) {
                treeResp = apiService.createTree(
                    auth,
                    owner,
                    repo,
                    CreateTreeRequest(baseTree = null, tree = treeItems)
                )
            }

            if (!treeResp.isSuccessful || treeResp.body() == null) {
                val rawErr = treeResp.errorBody()?.string() ?: ""
                val err = "Failed to create tree: ${parseGitHubError(rawErr)}"
                _uploadProgress.value = UploadProgress(phase = UploadPhase.FAILED, statusText = err)
                return@withContext Result.failure(Exception(err))
            }

            val newTreeSha = treeResp.body()!!.sha

            // 4. Create new Git Commit
            _uploadProgress.value = UploadProgress(
                phase = UploadPhase.CREATING_COMMIT,
                statusText = "Writing commit: \"$commitMessage\"..."
            )

            val parentsList = if (parentCommitSha != null) listOf(parentCommitSha) else emptyList()
            val commitResp = apiService.createCommit(
                auth,
                owner,
                repo,
                CreateCommitRequest(
                    message = commitMessage,
                    tree = newTreeSha,
                    parents = parentsList
                )
            )

            if (!commitResp.isSuccessful || commitResp.body() == null) {
                val rawErr = commitResp.errorBody()?.string() ?: ""
                val err = "Failed to create commit: ${parseGitHubError(rawErr)}"
                _uploadProgress.value = UploadProgress(phase = UploadPhase.FAILED, statusText = err)
                return@withContext Result.failure(Exception(err))
            }

            val newCommit = commitResp.body()!!
            val newCommitSha = newCommit.sha

            // 5. Update or Create branch head ref
            _uploadProgress.value = UploadProgress(
                phase = UploadPhase.UPDATING_BRANCH,
                statusText = "Updating branch $branch to commit ${newCommitSha.take(7)}..."
            )

            val refSuccess = if (parentCommitSha != null) {
                val updateResp = apiService.updateRef(
                    auth,
                    owner,
                    repo,
                    branch,
                    UpdateRefRequest(sha = newCommitSha, force = true)
                )
                updateResp.isSuccessful
            } else {
                // Initialize branch ref on new repository
                val createRefResp = apiService.createRef(
                    auth,
                    owner,
                    repo,
                    CreateRefRequest(ref = "refs/heads/$branch", sha = newCommitSha)
                )
                createRefResp.isSuccessful
            }

            if (!refSuccess) {
                val err = "Failed to update branch $branch ref to commit ${newCommitSha.take(7)}"
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

    companion object {
        fun sanitizeGitPath(folderPrefix: String, relativePath: String): String {
            val cleanPrefix = folderPrefix.replace('\\', '/').trim().trim('/')
            val cleanRelative = relativePath.replace('\\', '/').trim().trim('/')

            val combined = if (cleanPrefix.isEmpty()) {
                cleanRelative
            } else if (cleanRelative.isEmpty()) {
                cleanPrefix
            } else {
                "$cleanPrefix/$cleanRelative"
            }

            return combined.split('/')
                .map { it.trim() }
                .filter { it.isNotEmpty() && it != "." && it != ".." }
                .joinToString("/")
        }

        fun parseGitHubError(errorJson: String): String {
            if (errorJson.isBlank()) return "Unknown GitHub error"
            return try {
                val messageRegex = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"")
                val match = messageRegex.find(errorJson)
                match?.groupValues?.getOrNull(1) ?: errorJson.take(140)
            } catch (e: Exception) {
                errorJson.take(140)
            }
        }
    }
}
