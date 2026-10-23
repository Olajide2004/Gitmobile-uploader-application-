package com.example.data.rss

data class RssItem(
    val id: String,
    val title: String,
    val link: String,
    val description: String,
    val pubDate: String,
    val author: String? = null,
    val category: String? = null,
    val imageUrl: String? = null,
    val isRead: Boolean = false,
    val isBookmarked: Boolean = false
)

data class RssFeed(
    val title: String,
    val link: String,
    val description: String,
    val lastBuildDate: String? = null,
    val items: List<RssItem> = emptyList()
)
