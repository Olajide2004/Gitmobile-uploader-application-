package com.example.data.local

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.rss.RssItem

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "osun_hive_rss_updates"
        const val CHANNEL_NAME = "Osun Hive News & Updates"
        const val CHANNEL_DESC = "Instant notifications when new articles are published on Osun Hive"
        const val NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                    description = CHANNEL_DESC
                    enableVibration(true)
                }
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                notificationManager?.createNotificationChannel(channel)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun postRssNotification(item: RssItem) {
        if (!hasNotificationPermission()) return

        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(item.link)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            item.id.hashCode(),
            browserIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Osun Hive: ${item.title}")
            .setContentText(item.description.take(120))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(item.description.ifBlank { item.title })
                    .setBigContentTitle(item.title)
                    .setSummaryText(item.category ?: "Osun Hive Feed")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(item.id.hashCode(), builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun postTestNotification() {
        if (!hasNotificationPermission()) return

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.osunhive.name.ng")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Osun Hive Live Alert")
            .setContentText("New post: Exploring Innovation and Digital Culture in Osun State")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Stay ahead with fresh tech stories, cultural spotlights, and campus youth reports published on https://www.osunhive.name.ng")
                    .setSummaryText("Breaking News • Osun Hive")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(999, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
