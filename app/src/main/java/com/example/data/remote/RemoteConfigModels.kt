package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RemoteSocialLinks(
    val telegram: String = "https://t.me/Osunhive",
    val youtube: String = "https://youtube.com/@osunhivetech",
    val yofan: String = "https://yo.fan/osunhivetech",
    val tiktok: String = "https://www.tiktok.com/@osunhive.name.ng"
)

@JsonClass(generateAdapter = true)
data class RemoteBookmarkItem(
    val title: String,
    val description: String,
    val url: String
)

@JsonClass(generateAdapter = true)
data class RemoteLinksConfig(
    val version: Int = 1,
    @Json(name = "website_url") val websiteUrl: String = "https://www.osunhive.name.ng",
    @Json(name = "reward_ad_url") val rewardAdUrl: String = "https://www.osunhive.name.ng",
    @Json(name = "interstitial_ad_url") val interstitialAdUrl: String = "https://www.osunhive.name.ng",
    val announcement: String = "",
    @Json(name = "social_links") val socialLinks: RemoteSocialLinks = RemoteSocialLinks(),
    @Json(name = "quick_bookmarks") val quickBookmarks: List<RemoteBookmarkItem> = emptyList()
)
