package com.alfresco.content.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.alfresco.Logger
import com.alfresco.coroutines.asyncMap

class UploadWorker(
    appContext: Context,
    params: WorkerParameters
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
        return try {
            repository.update(entry.copy(offlineStatus = OfflineStatus.SYNCING))
            val file = repository.contentFile(entry)
            AnalyticsManager().apiTracker(APIEvent.UploadFiles, size = "${file.length().div(1024).div(1024)} MB")
            val res = BrowseRepository().createEntry(entry, file)
            file.delete() // TODO: what if delete fails?
            repository.update(
                entry.copyWithMetadata(res)
                    .copy(id = res.id, offlineStatus = OfflineStatus.SYNCED)
            )
            true
        } catch (ex: Exception) {
            Logger.e(ex)
            false
        }
    }

    private companion object {
        private const val MAX_CONCURRENT_OPERATIONS = 3
        private const val MAX_RETRIES = 5
    }
}
