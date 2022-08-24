package com.alfresco.download

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.text.format.Formatter
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.atomic.AtomicInteger

object DownloadMonitor {
    private const val CHANNEL_ID = "downloads"
    private var receiver: DownloadCompleteReceiver? = null
    private var smallIcon: Int? = null
    private var tint: Int? = null
    private val notificationId = AtomicInteger(0)

    fun smallIcon(@DrawableRes drawableResId: Int) =
        apply { smallIcon = drawableResId }

    fun tint(@ColorInt color: Int) =
        apply { tint = color }

    fun observe(context: Context) =
        apply {
            if (receiver != null) return this

            createNotificationChannel(context)

            val newReceiver = DownloadCompleteReceiver(::onDownloadComplete)
            val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            context.applicationContext.registerReceiver(newReceiver, filter)
            receiver = newReceiver
        }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val name = context.getString(R.string.notification_channel_downloads)
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance)

        ContextCompat
            .getSystemService(context, NotificationManager::class.java)
            ?.createNotificationChannel(channel)
    }

    private fun onDownloadComplete(context: Context?, downloadId: Long) {
        if (context == null) return
        if (downloadId == -1L) return

        val query = DownloadManager.Query().apply {
            setFilterById(downloadId)
        }

        val manager = ContextCompat.getSystemService(context, DownloadManager::class.java) ?: return

        val cursor = manager.query(query)
        if (!cursor.moveToFirst()) return
        val title = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE))
        val size = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
        val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
        cursor.close()

        val formattedSize = Formatter.formatFileSize(context, size)
        val content = if (status == DownloadManager.STATUS_SUCCESSFUL) {
            context.getString(R.string.notification_download_content, formattedSize)
        } else {
            context.getString(R.string.notification_download_content_error)
        }

        val intent = if (status == DownloadManager.STATUS_SUCCESSFUL) {
            Intent(DownloadManager.ACTION_VIEW_DOWNLOADS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        } else {
            context.packageManager.getLaunchIntentForPackage(context.applicationContext.packageName)
        }

        val pendingIntent = PendingIntent.getActivity(context, 0, intent, when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
            else -> FLAG_UPDATE_CURRENT
        })

        val smallIcon = this.smallIcon ?: return
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setContentText(content)
            .setColor(tint ?: Color.WHITE)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId.incrementAndGet(), builder.build())
        }
    }
}

class DownloadCompleteReceiver(
    private val onComplete: (context: Context?, downloadId: Long) -> Unit
) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) =
        onComplete(context, intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1)
}
