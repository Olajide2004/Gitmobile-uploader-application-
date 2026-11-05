package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.data.local.AppPreferences
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class RemoteLinksRepository(
    private val context: Context,
    private val appPreferences: AppPreferences
) {
    private val moshi = Moshi.Builder().build()
    private val adapter = moshi.adapter(RemoteLinksConfig::class.java)

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val _config = MutableStateFlow(loadInitialConfig())
    val config: StateFlow<RemoteLinksConfig> = _config.asStateFlow()

    private val _isFetching = MutableStateFlow(false)
    val isFetching: StateFlow<Boolean> = _isFetching.asStateFlow()

    private fun loadInitialConfig(): RemoteLinksConfig {
        val cached = appPreferences.getCachedRemoteConfig()
        if (!cached.isNullOrBlank()) {
            try {
                adapter.fromJson(cached)?.let { return it }
            } catch (e: Exception) {
                Log.w("RemoteLinksRepository", "Failed to parse cached config: ${e.message}")
            }
        }
        return RemoteLinksConfig()
    }

    suspend fun fetchRemoteConfig(customUrl: String? = null): Result<RemoteLinksConfig> = withContext(Dispatchers.IO) {
        val targetUrl = customUrl?.trim()?.ifBlank { null } ?: appPreferences.getRemoteConfigUrl()
        _isFetching.value = true
        try {
            val request = Request.Builder()
                .url(targetUrl)
                .header("Cache-Control", "no-cache")
                .header("Accept", "application/json")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP error ${response.code}: ${response.message}"))
            }

            val bodyString = response.body?.string().orEmpty()
            if (bodyString.isBlank()) {
                return@withContext Result.failure(Exception("Remote configuration response was empty"))
            }

            val parsedConfig = adapter.fromJson(bodyString)
                ?: return@withContext Result.failure(Exception("Failed to decode JSON configuration"))

            appPreferences.setCachedRemoteConfig(bodyString)
            _config.value = parsedConfig
            Result.success(parsedConfig)
        } catch (e: Exception) {
            Log.e("RemoteLinksRepository", "Failed to fetch remote links: ${e.message}", e)
            Result.failure(e)
        } finally {
            _isFetching.value = false
        }
    }

    fun updateConfigUrl(newUrl: String) {
        appPreferences.setRemoteConfigUrl(newUrl)
    }

    fun getEffectiveConfigUrl(): String {
        return appPreferences.getRemoteConfigUrl()
    }
}
