package com.alfresco.content.data.notifications

import android.content.Context
import androidx.core.content.ContextCompat.getString
import com.alfresco.content.data.R

object UploadNotificationHelper {
    const val CHANNEL_ID = "uploads"
    const val NOTIFICATION_ID = 2001

    fun createChannel(context: Context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                CHANNEL_ID,
                getString(context,R.string.title_upload_files),
                android.app.NotificationManager.IMPORTANCE_LOW
            )
            context.getSystemService(android.app.NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    fun progressNotification(context: Context, current: Int, total: Int): android.app.Notification {
        return androidx.core.app.NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(getString(context,R.string.text_upload_files))
            .setContentText(context.getString(R.string.upload_progress, current, total))
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setProgress(0, 0, true)
            .setOngoing(true)
            .build()
    }

    fun completeNotification(context: Context, success: Int, failed: Int): android.app.Notification {
        return androidx.core.app.NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(getString(context,R.string.text_upload_complete))
            .setContentText(context.getString(R.string.upload_status, success, failed))
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
            .setAutoCancel(true)
            .build()
    }
}
