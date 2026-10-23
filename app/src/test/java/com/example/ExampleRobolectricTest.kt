package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ad.AdMonetizationManager
import com.example.data.rss.RssParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("GitMobile", appName)
    }

    @Test
    fun `parse sample rss xml feed`() {
        val sampleXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0">
                <channel>
                    <title>Osun Hive</title>
                    <link>https://www.osunhive.name.ng</link>
                    <description>Digital Hive of Tech &amp; Culture</description>
                    <item>
                        <title>Tech Innovation in Osun State</title>
                        <link>https://www.osunhive.name.ng/tech-innovation</link>
                        <description>&lt;p&gt;Youth innovators building mobile software.&lt;/p&gt;</description>
                        <pubDate>Fri, 23 Oct 2026 12:00:00 GMT</pubDate>
                        <category>Technology</category>
                    </item>
                </channel>
            </rss>
        """.trimIndent()

        val feed = RssParser.parse(sampleXml)
        assertEquals("Osun Hive", feed.title)
        assertEquals(1, feed.items.size)
        val item = feed.items.first()
        assertEquals("Tech Innovation in Osun State", item.title)
        assertEquals("https://www.osunhive.name.ng/tech-innovation", item.link)
        assertEquals("Youth innovators building mobile software.", item.description)
        assertEquals("Technology", item.category)
    }

    @Test
    fun `ad monetization manager tracks impressions and clicks`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val adManager = AdMonetizationManager(context)

        val initialImpressions = adManager.stats.value.impressions
        adManager.recordImpression()
        val afterImpression = adManager.stats.value.impressions
        assertEquals(initialImpressions + 1, afterImpression)

        assertTrue(adManager.availableAds.isNotEmpty())
        assertEquals("https://www.osunhive.name.ng", adManager.availableAds.first().targetUrl)
    }
}
