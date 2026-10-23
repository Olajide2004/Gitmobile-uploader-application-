package com.example.data.github

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GitHubUser(
    val login: String,
    val name: String? = null,
    @Json(name = "avatar_url") val avatarUrl: String? = null,
    @Json(name = "public_repos") val publicRepos: Int = 0,
    val bio: String? = null,
    @Json(name = "html_url") val htmlUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class GitHubRepo(
    val id: Long = 0,
    val name: String,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "default_branch") val defaultBranch: String = "main",
    val private: Boolean = false,
    val description: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null,
    @Json(name = "html_url") val htmlUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class GitHubBranch(
    val name: String,
    val commit: BranchCommit
)

@JsonClass(generateAdapter = true)
data class BranchCommit(
    val sha: String,
    val url: String? = null
)

@JsonClass(generateAdapter = true)
data class GitRefResponse(
    val ref: String,
    @Json(name = "object") val gitObject: GitObject
)

@JsonClass(generateAdapter = true)
data class GitObject(
    val sha: String,
    val type: String? = null,
    val url: String? = null
)

@JsonClass(generateAdapter = true)
data class GitCommitDetails(
    val sha: String,
    val tree: GitTreeRef,
    val message: String? = null
)

@JsonClass(generateAdapter = true)
data class GitTreeRef(
    val sha: String,
    val url: String? = null
)

@JsonClass(generateAdapter = true)
data class CreateBlobRequest(
    val content: String,
    val encoding: String = "base64"
)

@JsonClass(generateAdapter = true)
data class CreateBlobResponse(
    val sha: String,
    val url: String? = null
)

@JsonClass(generateAdapter = true)
data class TreeItemRequest(
    val path: String,
    val mode: String = "100644",
    val type: String = "blob",
    val sha: String
)

@JsonClass(generateAdapter = true)
data class CreateTreeRequest(
    @Json(name = "base_tree") val baseTree: String? = null,
    val tree: List<TreeItemRequest>
)

@JsonClass(generateAdapter = true)
data class CreateTreeResponse(
    val sha: String,
    val url: String? = null
)

@JsonClass(generateAdapter = true)
data class CreateCommitRequest(
    val message: String,
    val tree: String,
    val parents: List<String>
)

@JsonClass(generateAdapter = true)
data class CreateCommitResponse(
    val sha: String,
    val url: String? = null,
    @Json(name = "html_url") val htmlUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class UpdateRefRequest(
    val sha: String,
    val force: Boolean = false
)

enum class UploadPhase {
    IDLE,
    SCANNING_FOLDER,
    UPLOADING_BLOBS,
    GENERATING_TREE,
    CREATING_COMMIT,
    UPDATING_BRANCH,
    COMPLETED,
    FAILED
}

data class UploadProgress(
    val currentFile: Int = 0,
    val totalFiles: Int = 0,
    val fileName: String = "",
    val phase: UploadPhase = UploadPhase.IDLE,
    val percent: Float = 0f,
    val statusText: String = ""
)

data class FolderFileItem(
    val name: String,
    val relativePath: String,
    val sizeBytes: Long,
    val isDirectory: Boolean = false,
    val uriString: String? = null,
    val rawBytes: ByteArray? = null
)

@JsonClass(generateAdapter = true)
data class UploadHistoryItem(
    val id: String,
    val repo: String,
    val branch: String,
    val folderPath: String,
    val commitSha: String,
    val commitMessage: String,
    val fileCount: Int,
    val timestamp: Long,
    val url: String
)
