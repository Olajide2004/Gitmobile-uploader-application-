package com.example.data.local

import android.content.Context
import com.example.data.github.UploadHistoryItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class AppPreferences(context: Context) {

    private val prefs = context.getSharedPreferences("gitmobile_prefs", Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder().build()

    private val historyAdapter = moshi.adapter<List<UploadHistoryItem>>(
        Types.newParameterizedType(List::class.java, UploadHistoryItem::class.java)
    )

    fun getGitHubToken(): String = prefs.getString("gh_token", "") ?: ""

    fun setGitHubToken(token: String) {
        prefs.edit().putString("gh_token", token.trim()).apply()
    }

    fun getGitHubUsername(): String = prefs.getString("gh_username", "Olajide2004") ?: "Olajide2004"

    fun setGitHubUsername(username: String) {
        prefs.edit().putString("gh_username", username.trim()).apply()
    }

    fun getSelectedRepo(): String = prefs.getString("gh_repo", "Gitmobile-uploader-application-") ?: "Gitmobile-uploader-application-"

    fun setSelectedRepo(repo: String) {
        prefs.edit().putString("gh_repo", repo.trim()).apply()
    }

    fun getSelectedBranch(): String = prefs.getString("gh_branch", "main") ?: "main"

    fun setSelectedBranch(branch: String) {
        prefs.edit().putString("gh_branch", branch.trim()).apply()
    }

    fun getFolderPath(): String = prefs.getString("gh_folder_path", "") ?: ""

    fun setFolderPath(path: String) {
        prefs.edit().putString("gh_folder_path", path.trim()).apply()
    }

    fun getCommitMessage(): String = prefs.getString("gh_commit_msg", "Upload folder via GitMobile") ?: "Upload folder via GitMobile"

    fun setCommitMessage(msg: String) {
        prefs.edit().putString("gh_commit_msg", msg).apply()
    }

    fun isRssNotificationEnabled(): Boolean = prefs.getBoolean("rss_notifications", true)

    fun setRssNotificationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("rss_notifications", enabled).apply()
    }

    fun getLastNotifiedId(): String = prefs.getString("last_notified_id", "") ?: ""

    fun setLastNotifiedId(id: String) {
        prefs.edit().putString("last_notified_id", id).apply()
    }

    fun getUploadHistory(): List<UploadHistoryItem> {
        val json = prefs.getString("upload_history", null) ?: return emptyList()
        return try {
            historyAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addUploadHistory(item: UploadHistoryItem) {
        val current = getUploadHistory().toMutableList()
        current.add(0, item) // add newest at top
        val trimmed = if (current.size > 50) current.take(50) else current
        prefs.edit().putString("upload_history", historyAdapter.toJson(trimmed)).apply()
    }

    fun clearUploadHistory() {
        prefs.edit().remove("upload_history").apply()
    }

    fun getBookmarkedIds(): Set<String> {
        return prefs.getStringSet("bookmarked_rss_ids", emptySet()) ?: emptySet()
    }

    fun toggleBookmark(id: String): Boolean {
        val current = getBookmarkedIds().toMutableSet()
        val isNowBookmarked = if (current.contains(id)) {
            current.remove(id)
            false
        } else {
            current.add(id)
            true
        }
        prefs.edit().putStringSet("bookmarked_rss_ids", current).apply()
        return isNowBookmarked
    }

    // Default GitHub raw JSON URL
    fun getRemoteConfigUrl(): String = prefs.getString(
        "remote_config_url",
        "https://raw.githubusercontent.com/Olajide2004/Gitmobile-uploader-application-/main/remote_links_config.json"
    ) ?: "https://raw.githubusercontent.com/Olajide2004/Gitmobile-uploader-application-/main/remote_links_config.json"

    fun setRemoteConfigUrl(url: String) {
        prefs.edit().putString("remote_config_url", url.trim()).apply()
    }

    fun getCachedRemoteConfig(): String? = prefs.getString("cached_remote_links_json", null)

    fun setCachedRemoteConfig(json: String) {
        prefs.edit().putString("cached_remote_links_json", json).apply()
    }

    // Material 3 Theme Mode (DARK, LIGHT, SYSTEM)
    fun getThemeMode(): String = prefs.getString("theme_mode", "DARK") ?: "DARK"

    fun setThemeMode(mode: String) {
        prefs.edit().putString("theme_mode", mode).apply()
    }

    // Material 3 Color Preset (HIVE_GOLD, DYNAMIC, OCEAN_BLUE, CYBER_PURPLE, SUNSET_AMBER, AMOLED_BLACK)
    fun getColorPreset(): String = prefs.getString("color_preset", "HIVE_GOLD") ?: "HIVE_GOLD"

    fun setColorPreset(preset: String) {
        prefs.edit().putString("color_preset", preset).apply()
    }

    // Material 3 Loading Indicator Style (CIRCULAR_EXPRESSIVE, LINEAR_WAVE, SKELETON_SHIMMER)
    fun getM3LoadingStyle(): String = prefs.getString("m3_loading_style", "CIRCULAR_EXPRESSIVE") ?: "CIRCULAR_EXPRESSIVE"

    fun setM3LoadingStyle(style: String) {
        prefs.edit().putString("m3_loading_style", style).apply()
    }

    // RSS Background Sync Interval in Minutes (15, 30, 60, 180)
    fun getRssSyncIntervalMinutes(): Int = prefs.getInt("rss_sync_interval_mins", 30)

    fun setRssSyncIntervalMinutes(minutes: Int) {
        prefs.edit().putInt("rss_sync_interval_mins", minutes).apply()
    }

    // Fast Mode / Parallel Uploading
    fun isFastUploadMode(): Boolean = prefs.getBoolean("fast_upload_mode", true)

    fun setFastUploadMode(enabled: Boolean) {
        prefs.edit().putBoolean("fast_upload_mode", enabled).apply()
    }
}
