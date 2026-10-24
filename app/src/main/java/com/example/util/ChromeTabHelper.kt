package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat

object ChromeTabHelper {

    /**
     * Opens a URL in an in-app Chrome Custom Tab with styled toolbar, title,
     * and share menu, falling back to a standard browser intent if needed.
     */
    fun openUrl(context: Context, url: String) {
        if (url.isBlank()) return

        val normalizedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else {
            url
        }

        try {
            val uri = Uri.parse(normalizedUrl)
            val defaultColor = ContextCompat.getColor(context, android.R.color.black)
            // Hex for #161B22 (GitSurface)
            val toolbarColor = 0xFF161B22.toInt()
            val navigationBarColor = 0xFF0D1117.toInt()

            val colorParams = CustomTabColorSchemeParams.Builder()
                .setToolbarColor(toolbarColor)
                .setNavigationBarColor(navigationBarColor)
                .build()

            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setUrlBarHidingEnabled(false)
                .setDefaultColorSchemeParams(colorParams)
                .setShareState(CustomTabsIntent.SHARE_STATE_ON)
                .build()

            customTabsIntent.intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            customTabsIntent.launchUrl(context, uri)
        } catch (e: Exception) {
            // Fallback to standard ACTION_VIEW intent
            try {
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(normalizedUrl)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
            } catch (ignored: Exception) {
                // Ignore or log
            }
        }
    }
}
