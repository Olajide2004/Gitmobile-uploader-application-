package com.example.data.ad

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class BannerAd(
    val id: String,
    val title: String,
    val tagline: String,
    val targetUrl: String = "https://www.osunhive.name.ng",
    val ctaText: String = "Visit Osun Hive",
    val badge: String = "Sponsored • Osun Hive",
    val drawableResId: Int? = null
)

data class MonetizationStats(
    val impressions: Long = 0,
    val clicks: Long = 0,
    val bannerEnabled: Boolean = true,
    val topBannerEnabled: Boolean = false,
    val bottomBannerEnabled: Boolean = true,
    val inFeedAdsEnabled: Boolean = true,
    val estimatedEarningsUsd: Double = 0.0
) {
    val ctrPercentage: Float
        get() = if (impressions > 0) (clicks.toFloat() / impressions.toFloat()) * 100f else 0f
}

class AdMonetizationManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("gitmobile_ads", Context.MODE_PRIVATE)

    private val _stats = MutableStateFlow(loadStats())
    val stats: StateFlow<MonetizationStats> = _stats.asStateFlow()

    val availableAds = listOf(
        BannerAd(
            id = "osunhive_media_main",
            title = "Osun Hive Media & Blog",
            tagline = "Digital Hub for Osun State and Beyond • News, Culture & Innovation",
            targetUrl = "https://www.osunhive.name.ng",
            ctaText = "Read Today's Stories",
            badge = "Featured • Osun Hive"
        ),
        BannerAd(
            id = "osunhive_tech_hub",
            title = "Tech & Innovation on Osun Hive",
            tagline = "Spotlighting grassroots tech founders, software engineers & creators",
            targetUrl = "https://www.osunhive.name.ng",
            ctaText = "Explore Tech News",
            badge = "Ad • Osun Hive"
        ),
        BannerAd(
            id = "osunhive_advertise",
            title = "Promote Your Business on Osun Hive",
            tagline = "Reach active readers across South-West Nigeria with targeted ad slots",
            targetUrl = "https://www.osunhive.name.ng",
            ctaText = "Monetize & Advertise",
            badge = "Partner Ad • Osun Hive"
        )
    )

    private val _currentBannerIndex = MutableStateFlow(0)
    val currentBannerIndex: StateFlow<Int> = _currentBannerIndex.asStateFlow()

    fun nextAd() {
        _currentBannerIndex.update { (it + 1) % availableAds.size }
        recordImpression()
    }

    fun recordImpression() {
        _stats.update { current ->
            val newImpressions = current.impressions + 1
            // Simulated eCPM of $2.40 per 1000 impressions
            val newEarnings = (newImpressions * 0.0024) + (current.clicks * 0.05)
            saveStats(newImpressions, current.clicks, newEarnings)
            current.copy(
                impressions = newImpressions,
                estimatedEarningsUsd = newEarnings
            )
        }
    }

    fun recordClickAndOpen(targetUrl: String = "https://www.osunhive.name.ng") {
        _stats.update { current ->
            val newClicks = current.clicks + 1
            val newEarnings = (current.impressions * 0.0024) + (newClicks * 0.05)
            saveStats(current.impressions, newClicks, newEarnings)
            current.copy(
                clicks = newClicks,
                estimatedEarningsUsd = newEarnings
            )
        }
        openUrl(targetUrl)
    }

    fun toggleBottomBanner(enabled: Boolean) {
        _stats.update { it.copy(bottomBannerEnabled = enabled) }
        prefs.edit().putBoolean("bottom_banner", enabled).apply()
    }

    fun toggleInFeedAds(enabled: Boolean) {
        _stats.update { it.copy(inFeedAdsEnabled = enabled) }
        prefs.edit().putBoolean("infeed_banner", enabled).apply()
    }

    fun openUrl(url: String) {
        com.example.util.ChromeTabHelper.openUrl(context, url)
    }

    private fun loadStats(): MonetizationStats {
        val imp = prefs.getLong("impressions", 12L) // start with realistic count
        val clk = prefs.getLong("clicks", 2L)
        val earn = prefs.getFloat("earnings", 0.12f).toDouble()
        val bottom = prefs.getBoolean("bottom_banner", true)
        val inFeed = prefs.getBoolean("infeed_banner", true)
        return MonetizationStats(
            impressions = imp,
            clicks = clk,
            bottomBannerEnabled = bottom,
            inFeedAdsEnabled = inFeed,
            estimatedEarningsUsd = earn
        )
    }

    private fun saveStats(impressions: Long, clicks: Long, earnings: Double) {
        prefs.edit()
            .putLong("impressions", impressions)
            .putLong("clicks", clicks)
            .putFloat("earnings", earnings.toFloat())
            .apply()
    }
}
