package com.example

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ad.AdMonetizationManager
import com.example.data.github.FolderFileItem
import com.example.data.github.GitHubApiService
import com.example.data.github.GitHubBranch
import com.example.data.github.GitHubRepo
import com.example.data.github.GitHubRepository
import com.example.data.github.GitHubUser
import com.example.data.github.UploadHistoryItem
import com.example.data.github.UploadProgress
import com.example.data.local.AppPreferences
import com.example.data.local.NotificationHelper
import com.example.data.rss.RssFeedState
import com.example.data.rss.RssRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets

enum class AppTab {
    UPLOAD,
    REPOSITORIES,
    RSS_FEED,
    HISTORY,
    MONETIZATION
}

data class UiNotification(
    val id: Long = System.currentTimeMillis(),
    val message: String,
    val isError: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = GitHubApiService.create()
    val appPreferences = AppPreferences(application)
    val notificationHelper = NotificationHelper(application)
    val adManager = AdMonetizationManager(application)
    val githubRepository = GitHubRepository(application, apiService, appPreferences)
    val rssRepository = RssRepository(application, appPreferences, notificationHelper)

    // Navigation Tab
    private val _currentTab = MutableStateFlow(AppTab.UPLOAD)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // GitHub Auth State
    private val _githubToken = MutableStateFlow(appPreferences.getGitHubToken())
    val githubToken: StateFlow<String> = _githubToken.asStateFlow()

    private val _githubUsername = MutableStateFlow(appPreferences.getGitHubUsername())
    val githubUsername: StateFlow<String> = _githubUsername.asStateFlow()

    private val _authenticatedUser = MutableStateFlow<GitHubUser?>(null)
    val authenticatedUser: StateFlow<GitHubUser?> = _authenticatedUser.asStateFlow()

    private val _isVerifyingToken = MutableStateFlow(false)
    val isVerifyingToken: StateFlow<Boolean> = _isVerifyingToken.asStateFlow()

    // Repositories & Branches
    private val _userRepos = MutableStateFlow<List<GitHubRepo>>(emptyList())
    val userRepos: StateFlow<List<GitHubRepo>> = _userRepos.asStateFlow()

    private val _isLoadingRepos = MutableStateFlow(false)
    val isLoadingRepos: StateFlow<Boolean> = _isLoadingRepos.asStateFlow()

    private val _selectedRepo = MutableStateFlow(appPreferences.getSelectedRepo())
    val selectedRepo: StateFlow<String> = _selectedRepo.asStateFlow()

    private val _selectedBranch = MutableStateFlow(appPreferences.getSelectedBranch())
    val selectedBranch: StateFlow<String> = _selectedBranch.asStateFlow()

    private val _branches = MutableStateFlow<List<GitHubBranch>>(emptyList())
    val branches: StateFlow<List<GitHubBranch>> = _branches.asStateFlow()

    // Folder Upload Configuration
    private val _targetFolderPath = MutableStateFlow(appPreferences.getFolderPath())
    val targetFolderPath: StateFlow<String> = _targetFolderPath.asStateFlow()

    private val _commitMessage = MutableStateFlow(appPreferences.getCommitMessage())
    val commitMessage: StateFlow<String> = _commitMessage.asStateFlow()

    private val _selectedFolderUri = MutableStateFlow<Uri?>(null)
    val selectedFolderUri: StateFlow<Uri?> = _selectedFolderUri.asStateFlow()

    private val _folderName = MutableStateFlow("")
    val folderName: StateFlow<String> = _folderName.asStateFlow()

    private val _folderFiles = MutableStateFlow<List<FolderFileItem>>(emptyList())
    val folderFiles: StateFlow<List<FolderFileItem>> = _folderFiles.asStateFlow()

    // Upload Execution
    val uploadProgress: StateFlow<UploadProgress> = githubRepository.uploadProgress

    private val _lastUploadSuccess = MutableStateFlow<UploadHistoryItem?>(null)
    val lastUploadSuccess: StateFlow<UploadHistoryItem?> = _lastUploadSuccess.asStateFlow()

    // Upload History
    private val _uploadHistory = MutableStateFlow(appPreferences.getUploadHistory())
    val uploadHistory: StateFlow<List<UploadHistoryItem>> = _uploadHistory.asStateFlow()

    // RSS Feed
    val rssFeedState: StateFlow<RssFeedState> = rssRepository.feedState

    // Transient UI Notification (Snackbar/Banner)
    private val _uiNotification = MutableStateFlow<UiNotification?>(null)
    val uiNotification: StateFlow<UiNotification?> = _uiNotification.asStateFlow()

    init {
        try {
            // Initialize with default demo folder so user can test right away
            loadPresetDemoFolder("WebStarter")

            // Auto-verify if token already saved
            if (_githubToken.value.isNotBlank()) {
                verifyToken()
            }

            // Fetch RSS news feed
            viewModelScope.launch {
                try {
                    rssRepository.fetchFeed()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun updateGitHubToken(token: String) {
        _githubToken.value = token
        appPreferences.setGitHubToken(token)
    }

    fun updateGitHubUsername(username: String) {
        _githubUsername.value = username
        appPreferences.setGitHubUsername(username)
    }

    fun updateSelectedRepo(repo: String) {
        _selectedRepo.value = repo
        appPreferences.setSelectedRepo(repo)
        loadBranches(repo)
    }

    fun updateSelectedBranch(branch: String) {
        _selectedBranch.value = branch
        appPreferences.setSelectedBranch(branch)
    }

    fun updateTargetFolderPath(path: String) {
        _targetFolderPath.value = path
        appPreferences.setFolderPath(path)
    }

    fun updateCommitMessage(msg: String) {
        _commitMessage.value = msg
        appPreferences.setCommitMessage(msg)
    }

    fun verifyToken() {
        val token = _githubToken.value.trim()
        if (token.isBlank()) {
            showNotification("Please enter a GitHub Personal Access Token", isError = true)
            return
        }

        viewModelScope.launch {
            _isVerifyingToken.value = true
            val result = githubRepository.verifyAndFetchUser(token)
            _isVerifyingToken.value = false

            result.fold(
                onSuccess = { user ->
                    _authenticatedUser.value = user
                    if (_githubUsername.value.isBlank()) {
                        updateGitHubUsername(user.login)
                    }
                    showNotification("Connected as ${user.login} (${user.publicRepos} repos)")
                    fetchRepositories()
                },
                onFailure = { error ->
                    showNotification("GitHub Token verification failed: ${error.localizedMessage}", isError = true)
                }
            )
        }
    }

    fun fetchRepositories() {
        val token = _githubToken.value.trim()
        val username = _githubUsername.value.trim()
        if (token.isBlank() && username.isBlank()) return

        viewModelScope.launch {
            _isLoadingRepos.value = true
            val result = githubRepository.fetchUserRepos(token, username)
            _isLoadingRepos.value = false

            result.fold(
                onSuccess = { repos ->
                    _userRepos.value = repos
                    if (_selectedRepo.value.isBlank() && repos.isNotEmpty()) {
                        updateSelectedRepo(repos.first().name)
                    }
                },
                onFailure = { error ->
                    showNotification("Failed to load repositories: ${error.localizedMessage}", isError = true)
                }
            )
        }
    }

    private fun loadBranches(repo: String) {
        val token = _githubToken.value.trim()
        val owner = _githubUsername.value.trim()
        if (token.isBlank() || owner.isBlank() || repo.isBlank()) return

        viewModelScope.launch {
            val result = githubRepository.fetchBranches(token, owner, repo)
            result.fold(
                onSuccess = { branchList ->
                    _branches.value = branchList
                    if (branchList.none { it.name == _selectedBranch.value } && branchList.isNotEmpty()) {
                        _selectedBranch.value = branchList.first().name
                    }
                },
                onFailure = {
                    // Default to main or master
                    _branches.value = listOf(
                        GitHubBranch(name = "main", commit = com.example.data.github.BranchCommit("HEAD")),
                        GitHubBranch(name = "master", commit = com.example.data.github.BranchCommit("HEAD"))
                    )
                }
            )
        }
    }

    fun onFolderSelected(treeUri: Uri) {
        _selectedFolderUri.value = treeUri
        viewModelScope.launch {
            val files = githubRepository.scanDocumentTree(treeUri)
            _folderFiles.value = files
            val pathSegments = treeUri.lastPathSegment?.split(":")
            _folderName.value = pathSegments?.lastOrNull() ?: "Selected Folder"
            showNotification("Scanned ${files.size} files in folder")
        }
    }

    fun loadPresetDemoFolder(preset: String) {
        when (preset) {
            "WebStarter" -> {
                _folderName.value = "my-web-assets"
                _targetFolderPath.value = "public/assets"
                _commitMessage.value = "Upload web assets via GitMobile"
                _folderFiles.value = listOf(
                    FolderFileItem(
                        name = "index.html",
                        relativePath = "index.html",
                        sizeBytes = 1420,
                        rawBytes = "<!DOCTYPE html>\n<html>\n<head><title>Osun Hive Project</title></head>\n<body><h1>Built with GitMobile & Osun Hive</h1></body></html>".toByteArray(StandardCharsets.UTF_8)
                    ),
                    FolderFileItem(
                        name = "style.css",
                        relativePath = "css/style.css",
                        sizeBytes = 850,
                        rawBytes = "body { background: #0d1117; color: #fff; font-family: sans-serif; }\n.hive-card { border: 1px solid #eab308; }".toByteArray(StandardCharsets.UTF_8)
                    ),
                    FolderFileItem(
                        name = "app.js",
                        relativePath = "js/app.js",
                        sizeBytes = 1120,
                        rawBytes = "console.log('GitMobile folder commit initialized');\nfetch('https://www.osunhive.name.ng/feed/');".toByteArray(StandardCharsets.UTF_8)
                    ),
                    FolderFileItem(
                        name = "README.md",
                        relativePath = "README.md",
                        sizeBytes = 630,
                        rawBytes = "# Osun Hive Connected Repo\n\nUploaded seamlessly using GitMobile Android App.\nVisit https://www.osunhive.name.ng for more updates.".toByteArray(StandardCharsets.UTF_8)
                    )
                )
            }
            "DocsFolder" -> {
                _folderName.value = "documentation"
                _targetFolderPath.value = "docs"
                _commitMessage.value = "Update documentation & guides"
                _folderFiles.value = listOf(
                    FolderFileItem(
                        name = "overview.md",
                        relativePath = "overview.md",
                        sizeBytes = 1890,
                        rawBytes = "# System Architecture\n\nGitMobile Uploader powers direct Android-to-GitHub tree commits.".toByteArray(StandardCharsets.UTF_8)
                    ),
                    FolderFileItem(
                        name = "api_reference.md",
                        relativePath = "api/reference.md",
                        sizeBytes = 2450,
                        rawBytes = "# Git Data API Reference\n- Blobs: POST /repos/{owner}/{repo}/git/blobs\n- Trees: POST /repos/{owner}/{repo}/git/trees\n- Commits: POST /repos/{owner}/{repo}/git/commits".toByteArray(StandardCharsets.UTF_8)
                    ),
                    FolderFileItem(
                        name = "changelog.txt",
                        relativePath = "changelog.txt",
                        sizeBytes = 420,
                        rawBytes = "v1.0.0 - Initial mobile folder upload release with Osun Hive RSS & Ads integration.".toByteArray(StandardCharsets.UTF_8)
                    )
                )
            }
        }
    }

    fun startFolderUpload() {
        val token = _githubToken.value.trim()
        val owner = _githubUsername.value.trim()
        val repo = _selectedRepo.value.trim()
        val branch = _selectedBranch.value.ifBlank { "main" }
        val folderPath = _targetFolderPath.value.trim()
        val message = _commitMessage.value.ifBlank { "Upload folder via GitMobile" }
        val files = _folderFiles.value

        if (token.isBlank()) {
            showNotification("GitHub Personal Access Token is required", isError = true)
            return
        }
        if (owner.isBlank()) {
            showNotification("GitHub Username is required", isError = true)
            return
        }
        if (repo.isBlank()) {
            showNotification("Repository name is required", isError = true)
            return
        }
        if (files.isEmpty()) {
            showNotification("Please select a folder or preset files to upload", isError = true)
            return
        }

        viewModelScope.launch {
            val result = githubRepository.uploadFolder(
                token = token,
                owner = owner,
                repo = repo,
                branch = branch,
                targetRepoFolderPath = folderPath,
                commitMessage = message,
                files = files
            )

            result.fold(
                onSuccess = { historyItem ->
                    _lastUploadSuccess.value = historyItem
                    _uploadHistory.value = appPreferences.getUploadHistory()
                    showNotification("Committed ${historyItem.fileCount} files! SHA: ${historyItem.commitSha.take(7)}")
                },
                onFailure = { error ->
                    showNotification("Upload failed: ${error.localizedMessage}", isError = true)
                }
            )
        }
    }

    fun dismissLastUploadSuccess() {
        _lastUploadSuccess.value = null
        githubRepository.resetProgress()
    }

    fun refreshRssFeed() {
        viewModelScope.launch {
            rssRepository.fetchFeed(forceRefresh = true)
            showNotification("RSS Feed refreshed from Osun Hive")
        }
    }

    fun triggerTestNotification() {
        notificationHelper.postTestNotification()
        showNotification("Sent Osun Hive post alert notification!")
    }

    fun toggleBookmark(articleId: String) {
        rssRepository.toggleBookmark(articleId)
    }

    fun clearHistory() {
        appPreferences.clearUploadHistory()
        _uploadHistory.value = emptyList()
        showNotification("Upload history cleared")
    }

    fun dismissNotification() {
        _uiNotification.value = null
    }

    private fun showNotification(message: String, isError: Boolean = false) {
        _uiNotification.value = UiNotification(message = message, isError = isError)
    }
}
