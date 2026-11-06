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
    CHROME_TAB,
    HISTORY,
    SETTINGS
}

enum class WebsiteAdMode {
    INTERSTITIAL,
    REWARDED
}

data class WebsiteAdState(
    val isVisible: Boolean = false,
    val mode: WebsiteAdMode = WebsiteAdMode.INTERSTITIAL,
    val url: String = "https://www.osunhive.name.ng",
    val title: String = "Osun Hive Sponsor",
    val requiredDurationSeconds: Int = 5,
    val onRewardGranted: (() -> Unit)? = null
)

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
    val remoteLinksRepository = com.example.data.remote.RemoteLinksRepository(application, appPreferences)

    val remoteConfig: StateFlow<com.example.data.remote.RemoteLinksConfig> = remoteLinksRepository.config
    val isFetchingRemoteConfig: StateFlow<Boolean> = remoteLinksRepository.isFetching

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

    // Fast Scanning & Optimization Controls
    private val _isScanningFolder = MutableStateFlow(false)
    val isScanningFolder: StateFlow<Boolean> = _isScanningFolder.asStateFlow()

    private val _ignoreBuildDirs = MutableStateFlow(true)
    val ignoreBuildDirs: StateFlow<Boolean> = _ignoreBuildDirs.asStateFlow()

    // Upload Execution
    val uploadProgress: StateFlow<UploadProgress> = githubRepository.uploadProgress

    private val _lastUploadSuccess = MutableStateFlow<UploadHistoryItem?>(null)
    val lastUploadSuccess: StateFlow<UploadHistoryItem?> = _lastUploadSuccess.asStateFlow()

    // Upload History
    private val _uploadHistory = MutableStateFlow(appPreferences.getUploadHistory())
    val uploadHistory: StateFlow<List<UploadHistoryItem>> = _uploadHistory.asStateFlow()

    // RSS Feed
    val rssFeedState: StateFlow<RssFeedState> = rssRepository.feedState

    private val _isRssNotificationEnabled = MutableStateFlow(appPreferences.isRssNotificationEnabled())
    val isRssNotificationEnabled: StateFlow<Boolean> = _isRssNotificationEnabled.asStateFlow()

    // Transient UI Notification (Snackbar/Banner)
    private val _uiNotification = MutableStateFlow<UiNotification?>(null)
    val uiNotification: StateFlow<UiNotification?> = _uiNotification.asStateFlow()

    // Website View Ad Dialog (Interstitial & Rewarded with timer & AdSense)
    private val _websiteAdState = MutableStateFlow(WebsiteAdState())
    val websiteAdState: StateFlow<WebsiteAdState> = _websiteAdState.asStateFlow()

    private val _isRewardBoostActive = MutableStateFlow(false)
    val isRewardBoostActive: StateFlow<Boolean> = _isRewardBoostActive.asStateFlow()

    // Material 3 Customization & Preferences
    private val _themeMode = MutableStateFlow(appPreferences.getThemeMode())
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _colorPreset = MutableStateFlow(appPreferences.getColorPreset())
    val colorPreset: StateFlow<String> = _colorPreset.asStateFlow()

    private val _m3LoadingStyle = MutableStateFlow(appPreferences.getM3LoadingStyle())
    val m3LoadingStyle: StateFlow<String> = _m3LoadingStyle.asStateFlow()

    private val _rssSyncIntervalMinutes = MutableStateFlow(appPreferences.getRssSyncIntervalMinutes())
    val rssSyncIntervalMinutes: StateFlow<Int> = _rssSyncIntervalMinutes.asStateFlow()

    private val _fastUploadMode = MutableStateFlow(appPreferences.isFastUploadMode())
    val fastUploadMode: StateFlow<Boolean> = _fastUploadMode.asStateFlow()

    init {
        try {
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

            // Fetch Remote Links Config (GitHub JSON)
            viewModelScope.launch {
                try {
                    remoteLinksRepository.fetchRemoteConfig()
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
        val sanitized = path.trim().trim('/')
        val finalPath = if (sanitized == "." || sanitized == "/" || sanitized == "./") "" else sanitized
        _targetFolderPath.value = finalPath
        appPreferences.setFolderPath(finalPath)
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

    fun setIgnoreBuildDirs(enabled: Boolean) {
        _ignoreBuildDirs.value = enabled
        _selectedFolderUri.value?.let { onFolderSelected(it) }
    }

    fun toggleIgnoreBuildDirs() {
        setIgnoreBuildDirs(!_ignoreBuildDirs.value)
    }

    fun onFolderSelected(treeUri: Uri) {
        _selectedFolderUri.value = treeUri
        viewModelScope.launch {
            _isScanningFolder.value = true
            try {
                val files = githubRepository.scanDocumentTree(treeUri, _ignoreBuildDirs.value)
                _folderFiles.value = files
                val pathSegments = treeUri.lastPathSegment?.split(":")
                _folderName.value = pathSegments?.lastOrNull() ?: "Selected Folder"
                showNotification("Scanned ${files.size} files ready for upload")
            } catch (e: Exception) {
                showNotification("Failed to scan folder: ${e.localizedMessage}", isError = true)
            } finally {
                _isScanningFolder.value = false
            }
        }
    }

    fun rescanFolder() {
        val uri = _selectedFolderUri.value
        if (uri != null) {
            onFolderSelected(uri)
        } else {
            showNotification("No folder selected yet. Tap 'Pick Device Folder'.")
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

    fun resetUploadProgress() {
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
        showNotification("Sent Osun Hive post alert notification with image preview!")
    }

    fun setRssNotificationEnabled(enabled: Boolean, context: android.content.Context) {
        _isRssNotificationEnabled.value = enabled
        appPreferences.setRssNotificationEnabled(enabled)
        if (enabled) {
            com.example.data.rss.RssBackgroundReceiver.schedulePeriodicSync(context)
            showNotification("Background RSS check enabled (every 30m)")
        } else {
            com.example.data.rss.RssBackgroundReceiver.cancelPeriodicSync(context)
            showNotification("Background RSS check paused")
        }
    }

    fun syncRssInBackgroundNow(context: android.content.Context) {
        viewModelScope.launch {
            showNotification("Checking Osun Hive for new stories in background...")
            rssRepository.fetchFeed(forceRefresh = true)
        }
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

    fun showInterstitialAd(
        url: String = "https://www.osunhive.name.ng",
        title: String = "Osun Hive Interstitial Sponsor"
    ) {
        _websiteAdState.value = WebsiteAdState(
            isVisible = true,
            mode = WebsiteAdMode.INTERSTITIAL,
            url = url,
            title = title,
            requiredDurationSeconds = 5
        )
    }

    fun showRewardedAd(
        url: String = "https://www.osunhive.name.ng",
        title: String = "Osun Hive Rewarded Ad",
        onReward: (() -> Unit)? = null
    ) {
        _websiteAdState.value = WebsiteAdState(
            isVisible = true,
            mode = WebsiteAdMode.REWARDED,
            url = url,
            title = title,
            requiredDurationSeconds = 6,
            onRewardGranted = {
                _isRewardBoostActive.value = true
                onReward?.invoke()
                showNotification("🎉 Reward Claimed: Fast Mode & VIP Speed Active!")
            }
        )
    }

    fun dismissWebsiteAd() {
        _websiteAdState.value = _websiteAdState.value.copy(isVisible = false)
    }

    fun refreshRemoteConfig(customUrl: String? = null) {
        viewModelScope.launch {
            val result = remoteLinksRepository.fetchRemoteConfig(customUrl)
            result.fold(
                onSuccess = { cfg ->
                    showNotification("Updated remote links: ${cfg.websiteUrl}")
                },
                onFailure = { error ->
                    showNotification("Failed to fetch remote links: ${error.localizedMessage}", isError = true)
                }
            )
        }
    }

    fun updateRemoteConfigUrl(url: String) {
        remoteLinksRepository.updateConfigUrl(url)
        refreshRemoteConfig(url)
    }

    fun getRemoteConfigUrl(): String = remoteLinksRepository.getEffectiveConfigUrl()

    fun updateThemeMode(mode: String) {
        _themeMode.value = mode
        appPreferences.setThemeMode(mode)
    }

    fun updateColorPreset(preset: String) {
        _colorPreset.value = preset
        appPreferences.setColorPreset(preset)
        showNotification("Applied M3 theme preset: $preset")
    }

    fun updateM3LoadingStyle(style: String) {
        _m3LoadingStyle.value = style
        appPreferences.setM3LoadingStyle(style)
        showNotification("Updated Material 3 loading style")
    }

    fun updateRssSyncInterval(minutes: Int, context: android.content.Context) {
        _rssSyncIntervalMinutes.value = minutes
        appPreferences.setRssSyncIntervalMinutes(minutes)
        if (_isRssNotificationEnabled.value) {
            com.example.data.rss.RssBackgroundReceiver.schedulePeriodicSync(context)
        }
        showNotification("Background RSS check set to every ${minutes}m")
    }

    fun updateFastUploadMode(enabled: Boolean) {
        _fastUploadMode.value = enabled
        appPreferences.setFastUploadMode(enabled)
        showNotification(if (enabled) "Fast parallel upload enabled" else "Standard upload mode enabled")
    }

    fun clearAppCache() {
        showNotification("App preferences & cache refreshed")
    }

    private fun showNotification(message: String, isError: Boolean = false) {
        _uiNotification.value = UiNotification(message = message, isError = isError)
    }
}

