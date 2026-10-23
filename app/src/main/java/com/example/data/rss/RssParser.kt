package com.example.data.rss

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.util.UUID
import java.util.regex.Pattern

object RssParser {

    private val HTML_TAG_PATTERN = Pattern.compile("<[^>]*>")
    private val IMG_SRC_PATTERN = Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>", Pattern.CASE_INSENSITIVE)

    fun parse(xmlContent: String): RssFeed {
        var feedTitle = "Osun Hive"
        var feedLink = "https://www.osunhive.name.ng"
        var feedDescription = "Osun Hive Media & Blog"
        var lastBuildDate: String? = null
        val items = mutableListOf<RssItem>()

        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xmlContent))

            var eventType = parser.eventType
            var inItem = false

            var currentTitle = ""
            var currentLink = ""
            var currentDescription = ""
            var currentPubDate = ""
            var currentAuthor: String? = null
            var currentCategory: String? = null
            var currentGuid: String? = null
            var currentImageUrl: String? = null

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tagName = parser.name

                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (tagName.equals("item", ignoreCase = true) || tagName.equals("entry", ignoreCase = true)) {
                            inItem = true
                            currentTitle = ""
                            currentLink = ""
                            currentDescription = ""
                            currentPubDate = ""
                            currentAuthor = null
                            currentCategory = null
                            currentGuid = null
                            currentImageUrl = null
                        } else if (!inItem) {
                            when {
                                tagName.equals("title", ignoreCase = true) -> feedTitle = parser.nextText().trim()
                                tagName.equals("link", ignoreCase = true) -> {
                                    val href = parser.getAttributeValue(null, "href")
                                    feedLink = if (!href.isNullOrBlank()) href else parser.nextText().trim()
                                }
                                tagName.equals("description", ignoreCase = true) || tagName.equals("subtitle", ignoreCase = true) -> {
                                    feedDescription = parser.nextText().trim()
                                }
                                tagName.equals("lastBuildDate", ignoreCase = true) || tagName.equals("updated", ignoreCase = true) -> {
                                    lastBuildDate = parser.nextText().trim()
                                }
                            }
                        } else {
                            // Inside item / entry
                            when {
                                tagName.equals("title", ignoreCase = true) -> {
                                    currentTitle = parser.nextText().trim()
                                }
                                tagName.equals("link", ignoreCase = true) -> {
                                    val href = parser.getAttributeValue(null, "href")
                                    currentLink = if (!href.isNullOrBlank()) href else parser.nextText().trim()
                                }
                                tagName.equals("description", ignoreCase = true) ||
                                tagName.equals("summary", ignoreCase = true) ||
                                tagName.equals("encoded", ignoreCase = true) -> {
                                    val rawDesc = parser.nextText().trim()
                                    if (currentImageUrl == null) {
                                        currentImageUrl = extractImageUrl(rawDesc)
                                    }
                                    if (currentDescription.isBlank() || tagName.equals("encoded", ignoreCase = true)) {
                                        currentDescription = cleanHtml(rawDesc)
                                    }
                                }
                                tagName.equals("pubDate", ignoreCase = true) ||
                                tagName.equals("published", ignoreCase = true) ||
                                tagName.equals("updated", ignoreCase = true) -> {
                                    currentPubDate = parser.nextText().trim()
                                }
                                tagName.equals("creator", ignoreCase = true) ||
                                tagName.equals("author", ignoreCase = true) -> {
                                    currentAuthor = parser.nextText().trim()
                                }
                                tagName.equals("category", ignoreCase = true) -> {
                                    if (currentCategory == null) {
                                        currentCategory = parser.nextText().trim()
                                    }
                                }
                                tagName.equals("guid", ignoreCase = true) ||
                                tagName.equals("id", ignoreCase = true) -> {
                                    currentGuid = parser.nextText().trim()
                                }
                                tagName.equals("enclosure", ignoreCase = true) ||
                                tagName.equals("content", ignoreCase = true) -> {
                                    val url = parser.getAttributeValue(null, "url")
                                    val type = parser.getAttributeValue(null, "type")
                                    if (!url.isNullOrBlank() && (type?.startsWith("image") == true || url.contains(".jpg") || url.contains(".png") || url.contains(".jpeg") || url.contains(".webp"))) {
                                        currentImageUrl = url
                                    }
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (tagName.equals("item", ignoreCase = true) || tagName.equals("entry", ignoreCase = true)) {
                            inItem = false
                            val id = currentGuid?.takeIf { it.isNotBlank() }
                                ?: currentLink.takeIf { it.isNotBlank() }
                                ?: UUID.randomUUID().toString()

                            if (currentTitle.isNotBlank()) {
                                items.add(
                                    RssItem(
                                        id = id,
                                        title = cleanHtmlEntities(currentTitle),
                                        link = if (currentLink.isNotBlank()) currentLink else "https://www.osunhive.name.ng",
                                        description = currentDescription,
                                        pubDate = currentPubDate,
                                        author = currentAuthor,
                                        category = currentCategory ?: "Osun Hive News",
                                        imageUrl = currentImageUrl
                                    )
                                )
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return RssFeed(
            title = feedTitle,
            link = feedLink,
            description = feedDescription,
            lastBuildDate = lastBuildDate,
            items = items
        )
    }

    private fun cleanHtml(html: String): String {
        val noTags = HTML_TAG_PATTERN.matcher(html).replaceAll("")
        return cleanHtmlEntities(noTags).trim()
    }

    private fun cleanHtmlEntities(text: String): String {
        return text.replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#039;", "'")
            .replace("&apos;", "'")
            .replace("&#8217;", "’")
            .replace("&#8216;", "‘")
            .replace("&#8220;", "“")
            .replace("&#8221;", "”")
            .replace("&nbsp;", " ")
            .replace("&#8211;", "–")
            .replace("&#8212;", "—")
    }

    private fun extractImageUrl(html: String): String? {
        val matcher = IMG_SRC_PATTERN.matcher(html)
        return if (matcher.find()) {
            matcher.group(1)
        } else null
    }
}
