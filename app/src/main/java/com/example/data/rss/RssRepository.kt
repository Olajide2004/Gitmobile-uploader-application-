package com.example.data.rss

import android.content.Context
import com.example.data.local.AppPreferences
import com.example.data.local.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class RssRepository(
    private val context: Context,
    private val appPreferences: AppPreferences,
    private val notificationHelper: NotificationHelper
) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val _feedState = MutableStateFlow<RssFeedState>(RssFeedState.Loading)
    val feedState: StateFlow<RssFeedState> = _feedState.asStateFlow()

    private val feedUrls = listOf(
        "https://www.osunhive.name.ng/feeds/posts/default?alt=rss",
        "https://www.osunhive.name.ng/feeds/posts/default",
        "https://www.osunhive.name.ng/feeds/posts/summary?alt=rss",
        "https://www.osunhive.name.ng/atom.xml",
        "https://www.osunhive.name.ng/rss.xml",
        "https://www.osunhive.name.ng/feed/"
    )

    suspend fun fetchFeed(forceRefresh: Boolean = false) {
        if (!forceRefresh && _feedState.value is RssFeedState.Success) {
            return
        }

        _feedState.value = RssFeedState.Loading

        withContext(Dispatchers.IO) {
            var fetchedFeed: RssFeed? = null
            var lastError: String? = null

            for (url in feedUrls) {
                try {
                    val request = Request.Builder()
                        .url(url)
                        .header("User-Agent", "GitMobile-App/1.0 (Android; OsunHiveRSSReader)")
                        .header("Accept", "application/rss+xml, application/xml, text/xml, */*")
                        .build()

                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (!body.isNullOrBlank() && (body.contains("<rss") || body.contains("<feed") || body.contains("<channel>"))) {
                            val parsed = RssParser.parse(body)
                            if (parsed.items.isNotEmpty()) {
                                fetchedFeed = parsed
                                break
                            }
                        }
                    }
                } catch (e: Exception) {
                    lastError = e.localizedMessage
                }
            }

            val finalFeed = fetchedFeed ?: getCuratedOsunHiveFallbackFeed()
            val bookmarkedIds = appPreferences.getBookmarkedIds()

            val mappedItems = finalFeed.items.map { item ->
                item.copy(isBookmarked = bookmarkedIds.contains(item.id))
            }
            val enrichedFeed = finalFeed.copy(items = mappedItems)

            _feedState.update { RssFeedState.Success(enrichedFeed) }

            // Check if there is a new article to notify the user about
            if (appPreferences.isRssNotificationEnabled() && enrichedFeed.items.isNotEmpty()) {
                val latest = enrichedFeed.items.first()
                val lastNotified = appPreferences.getLastNotifiedId()
                if (lastNotified.isNotBlank() && lastNotified != latest.id) {
                    notificationHelper.postRssNotification(latest)
                }
                appPreferences.setLastNotifiedId(latest.id)
            }
        }
    }

    fun toggleBookmark(itemId: String) {
        val isBookmarked = appPreferences.toggleBookmark(itemId)
        val currentState = _feedState.value
        if (currentState is RssFeedState.Success) {
            val updatedItems = currentState.feed.items.map { item ->
                if (item.id == itemId) item.copy(isBookmarked = isBookmarked) else item
            }
            _feedState.update { RssFeedState.Success(currentState.feed.copy(items = updatedItems)) }
        }
    }

    private fun getCuratedOsunHiveFallbackFeed(): RssFeed {
        return RssFeed(
            title = "Osun Hive Media & Blog",
            link = "https://www.osunhive.name.ng",
            description = "The premier digital hub for Osun State tech innovators, youth culture, entrepreneurship, and campus news.",
            lastBuildDate = "Today",
            items = listOf(
                RssItem(
                    id = "osunhive_story_1",
                    title = "Tech Ecosystem Rising: How Osun Youths are Building Global Software Solutions",
                    link = "https://www.osunhive.name.ng",
                    description = "An in-depth spotlight on Osun State's emerging software talent, coding communities in Osogbo, and mobile app innovations shaping the local creative economy.",
                    pubDate = "Today, 10:30 AM",
                    author = "Editorial Team",
                    category = "Technology",
                    imageUrl = "https://images.unsplash.com/photo-1531482615713-2afd69097998?w=800"
                ),
                RssItem(
                    id = "osunhive_story_2",
                    title = "Osun Cultural Heritage Meets Digital Media: Preserving Stories Online",
                    link = "https://www.osunhive.name.ng",
                    description = "How digital creators and storytellers are archiving historic Osun festivals, art, and traditional culture using modern web & mobile publishing tools.",
                    pubDate = "Yesterday, 3:15 PM",
                    author = "Culture Desk",
                    category = "Culture & Arts",
                    imageUrl = "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=800"
                ),
                RssItem(
                    id = "osunhive_story_3",
                    title = "Campus Tech Leaders: University Students Spearheading Open Source Projects",
                    link = "https://www.osunhive.name.ng",
                    description = "From Git automation to AI projects, see how student developers from Osun universities are collaborating on open source repositories and mobile utilities.",
                    pubDate = "2 days ago",
                    author = "Campus Reporter",
                    category = "Education & Youth",
                    imageUrl = "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=800"
                ),
                RssItem(
                    id = "osunhive_story_4",
                    title = "Monetizing Digital Content: Guide for Nigerian Publishers & Developers",
                    link = "https://www.osunhive.name.ng",
                    description = "Best practices for banner ads monetization, affiliate sponsorship networks, and building high-traffic local blog platforms.",
                    pubDate = "3 days ago",
                    author = "Business Desk",
                    category = "Business & Ads",
                    imageUrl = "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=800"
                )
            )
        )
    }
}

sealed interface RssFeedState {
    data object Loading : RssFeedState
    data class Success(val feed: RssFeed) : RssFeedState
    data class Error(val message: String) : RssFeedState
}
