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
                                    val rel = parser.getAttributeValue(null, "rel")
                                    val href = parser.getAttributeValue(null, "href")
                                    if (!href.isNullOrBlank()) {
                                        if (rel == null || rel.equals("alternate", ignoreCase = true) || currentLink.isBlank()) {
                                            currentLink = href
                                        }
                                    } else {
                                        val text = parser.nextText().trim()
                                        if (text.isNotBlank()) currentLink = text
                                    }
                                }
                                tagName.equals("description", ignoreCase = true) ||
                                tagName.equals("summary", ignoreCase = true) ||
                                tagName.equals("encoded", ignoreCase = true) ||
                                tagName.equals("content", ignoreCase = true) -> {
                                    val rawDesc = parser.nextText().trim()
                                    if (currentImageUrl == null) {
                                        currentImageUrl = extractImageUrl(rawDesc)
                                    }
                                    if (currentDescription.isBlank() || tagName.equals("summary", ignoreCase = true) || tagName.equals("description", ignoreCase = true)) {
                                        currentDescription = cleanHtml(rawDesc)
                                    }
                                }
                                tagName.equals("pubDate", ignoreCase = true) ||
                                tagName.equals("published", ignoreCase = true) ||
                                tagName.equals("updated", ignoreCase = true) -> {
                                    currentPubDate = parser.nextText().trim()
                                }
                                tagName.equals("creator", ignoreCase = true) ||
                                tagName.equals("author", ignoreCase = true) ||
                                tagName.equals("name", ignoreCase = true) -> {
                                    val text = parser.nextText().trim()
                                    if (text.isNotBlank() && currentAuthor == null) {
                                        currentAuthor = text
                                    }
                                }
                                tagName.equals("category", ignoreCase = true) -> {
                                    val term = parser.getAttributeValue(null, "term")
                                    if (!term.isNullOrBlank()) {
                                        if (currentCategory == null) currentCategory = term
                                    } else {
                                        val catText = parser.nextText().trim()
                                        if (catText.isNotBlank() && currentCategory == null) currentCategory = catText
                                    }
                                }
                                tagName.equals("guid", ignoreCase = true) ||
                                tagName.equals("id", ignoreCase = true) -> {
                                    currentGuid = parser.nextText().trim()
                                }
                                tagName.equals("enclosure", ignoreCase = true) ||
                                tagName.equals("thumbnail", ignoreCase = true) ||
                                tagName.endsWith(":thumbnail", ignoreCase = true) ||
                                tagName.endsWith(":content", ignoreCase = true) -> {
                                    val url = parser.getAttributeValue(null, "url")
                                    val type = parser.getAttributeValue(null, "type") ?: ""
                                    val medium = parser.getAttributeValue(null, "medium") ?: ""
                                    if (!url.isNullOrBlank()) {
                                        if (medium.equals("image", ignoreCase = true) ||
                                            type.startsWith("image/", ignoreCase = true) ||
                                            currentImageUrl == null) {
                                            currentImageUrl = url
                                        }
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
        val srcMatcher = IMG_SRC_PATTERN.matcher(html)
        if (srcMatcher.find()) {
            val url = srcMatcher.group(1)?.trim()
            if (!url.isNullOrBlank() && (url.startsWith("http://") || url.startsWith("https://"))) {
                return url
            }
        }
        val dataSrcPattern = Pattern.compile("data-(?:original|src|url)\\s*=\\s*['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE)
        val dataSrcMatcher = dataSrcPattern.matcher(html)
        if (dataSrcMatcher.find()) {
            val url = dataSrcMatcher.group(1)?.trim()
            if (!url.isNullOrBlank() && (url.startsWith("http://") || url.startsWith("https://"))) {
                return url
            }
        }
        return null
    }
}
