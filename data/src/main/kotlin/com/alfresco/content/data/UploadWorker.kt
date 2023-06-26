package com.alfresco.content.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.alfresco.Logger
import com.alfresco.coroutines.asyncMap
import retrofit2.HttpException

class UploadWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    private val repository = OfflineRepository()

    override suspend fun doWork(): Result {
        // Always return success so we don't cancel APPEND work
        if (runAttemptCount > MAX_RETRIES) return Result.success()

        val result = pendingUploads().asyncMap(MAX_CONCURRENT_OPERATIONS) { createItem(it) }

        return if (result.any { !it }) Result.retry() else Result.success()
    }

    private fun pendingUploads(): List<Entry> =
        repository.fetchPendingUploads()

    private suspend fun createItem(entry: Entry): Boolean {
        val file = repository.contentFile(entry)
        return try {
            repository.update(entry.copy(offlineStatus = OfflineStatus.SYNCING))
            AnalyticsManager().apiTracker(
                if (entry.uploadServer == UploadServerType.UPLOAD_TO_TASK) APIEvent.UploadTaskAttachment else APIEvent.UploadFiles,
                status = true,
                size = "${file.length().div(1024).div(1024)} MB",
            )
            val res = if (entry.uploadServer == UploadServerType.DEFAULT) BrowseRepository().createEntry(entry, file) else TaskRepository().createEntry(entry, file, entry.uploadServer)
            file.delete() // TODO: what if delete fails?
            repository.update(
                entry.copyWithMetadata(res)
                    .copy(id = res.id, offlineStatus = OfflineStatus.SYNCED),
            )
            true
        } catch (ex: Exception) {
            ex.printStackTrace()
            if ((ex as HttpException).response()?.code() == 404 && (ex as HttpException).response()?.code() == 413) {
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

    private companion object {
        private const val MAX_CONCURRENT_OPERATIONS = 3
        private const val MAX_RETRIES = 5
    }
}
