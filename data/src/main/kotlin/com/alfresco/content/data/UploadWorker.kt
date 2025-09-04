package com.alfresco.content.data

import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getString
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.alfresco.Logger
import com.alfresco.content.data.notifications.UploadNotificationHelper
import com.alfresco.coroutines.asyncMap
import retrofit2.HttpException
import java.util.concurrent.atomic.AtomicInteger

class UploadWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    private val repository = OfflineRepository()

    private val completedCount = AtomicInteger(0)
    private var totalCount = 0
    private var successCount = 0
    private var failedCount = 0

    override suspend fun doWork(): Result {
        if (runAttemptCount > MAX_RETRIES) return Result.success()

        val uploads = pendingUploads()
        totalCount = uploads.size
        if (totalCount == 0) return Result.success()

        val context = applicationContext
        UploadNotificationHelper.createChannel(context)

        // Start initial notification
        safeNotify(context, UploadNotificationHelper.NOTIFICATION_ID,
            UploadNotificationHelper.progressNotification(context, 0, totalCount))

        // Run concurrent uploads
        val result = uploads.asyncMap(MAX_CONCURRENT_OPERATIONS) { entry ->
            val success = createItem(entry)

            // increment immediately after this file is done
            val done = completedCount.incrementAndGet()

            trackProgress(context, success) // update counters + notifications

            // update notification (indefinite or determinate)
            val notification = UploadNotificationHelper.progressNotification(context, done, totalCount)
            safeNotify(context, UploadNotificationHelper.NOTIFICATION_ID, notification)
            success
        }

        // Final summary notification
        safeNotify(context, UploadNotificationHelper.NOTIFICATION_ID,
            UploadNotificationHelper.completeNotification(context, successCount, failedCount))

        return if (result.any { !it }) Result.retry() else Result.success()
    }

    @Synchronized
    private fun trackProgress(context: Context, success: Boolean) {
        completedCount.incrementAndGet()
        if (success) successCount++ else failedCount++
    }




    private fun pendingUploads(): List<Entry> = repository.fetchPendingUploads()

    private suspend fun createItem(entry: Entry): Boolean {
        val file = repository.contentFile(entry)
        return try {
            repository.update(entry.copy(offlineStatus = OfflineStatus.SYNCING))
            AnalyticsManager().apiTracker(
                if (entry.uploadServer == UploadServerType.UPLOAD_TO_TASK) APIEvent.UploadTaskAttachment else APIEvent.UploadFiles,
                status = true,
                size = "${file.length().div(1024).div(1024)} MB",
            )
            val res =
                if (entry.uploadServer == UploadServerType.DEFAULT) {
                    BrowseRepository().createEntry(
                        entry,
                        file,
                    )
                } else {
                    TaskRepository().createEntry(entry, file, entry.uploadServer)
                }
            file.delete() // TODO: what if delete fails?
            repository.update(
                entry.copyWithMetadata(res)
                    .copy(id = res.id, offlineStatus = OfflineStatus.SYNCED),
            )
            true
        } catch (ex: Exception) {
            ex.printStackTrace()
            if ((ex as HttpException).response()?.code() == 404 || (ex as HttpException).response()?.code() == 413) {
                repository.remove(entry)
                file.delete()
            }
            Logger.e(ex)
            AnalyticsManager().apiTracker(
                APIEvent.UploadFiles,
                status = false,
                size = "0",
            )
            false
        }
    }

    private fun safeNotify(context: Context, notificationId: Int, notification: Notification) {
        val nm = NotificationManagerCompat.from(context)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            nm.notify(notificationId, notification)
        } else {
            // Permission not granted → skip silently
            Logger.e(getString(context,R.string.permission_not_granted))
        }
    }


    private companion object {
        private const val MAX_CONCURRENT_OPERATIONS = 3
        private const val MAX_RETRIES = 5
    }
}
