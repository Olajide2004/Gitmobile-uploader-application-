package com.example.data.rss

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.local.AppPreferences
import com.example.data.local.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver triggered by AlarmManager and system BOOT_COMPLETED to perform
 * periodic background checks of Osun Hive RSS feeds and post notifications for new articles.
 */
class RssBackgroundReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val appPreferences = AppPreferences(context)
        if (!appPreferences.isRssNotificationEnabled()) {
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val notificationHelper = NotificationHelper(context)
                val repository = RssRepository(context, appPreferences, notificationHelper)
                repository.fetchFeed(forceRefresh = true)
            } catch (e: Exception) {
                Log.e("RssBackgroundReceiver", "Background RSS sync failed: ${e.message}")
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE = 8801
        // Interval: Check background feed every 30 minutes
        private const val INTERVAL_MILLIS = 30 * 60 * 1000L

        fun schedulePeriodicSync(context: Context) {
            try {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
                val intent = Intent(context, RssBackgroundReceiver::class.java)
                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
                val pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, flags)

                val triggerAtMillis = System.currentTimeMillis() + (5 * 60 * 1000L) // initial check in 5 mins
                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    INTERVAL_MILLIS,
                    pendingIntent
                )
            } catch (e: Exception) {
                Log.e("RssBackgroundReceiver", "Could not schedule alarm: ${e.message}")
            }
        }

        fun cancelPeriodicSync(context: Context) {
            try {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
                val intent = Intent(context, RssBackgroundReceiver::class.java)
                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
                val pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, flags)
                alarmManager.cancel(pendingIntent)
            } catch (e: Exception) {
                Log.e("RssBackgroundReceiver", "Could not cancel alarm: ${e.message}")
            }
        }
    }
}
