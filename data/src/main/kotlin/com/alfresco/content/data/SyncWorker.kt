package com.alfresco.content.data

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.alfresco.coroutines.asyncMap
import com.alfresco.coroutines.asyncMapNotNull
import com.alfresco.download.ContentDownloader
import java.io.File
import java.lang.Exception
import java.time.ZonedDateTime

class SyncWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    private val repository = OfflineRepository()

    override suspend fun doWork(): Result {

        val toSync = buildDownloadList()
        downloadPending(toSync)

        return Result.success()
    }

    private suspend fun buildDownloadList(): List<Entry> =
        repository
            .fetchAllOfflineEntries()
            .asyncMapNotNull(MAX_CONCURRENT_FETCHES) { local ->
                try {
                    val remote = BrowseRepository().fetchEntry(local.id)
                    if (dateCompare(remote.modified, local.modified) > 0L ||
                        local.offlineStatus != OfflineStatus.Synced) {
                        remote
                    } else {
                        null
                    }
                } catch (ex: Exception) {
                    repository.updateEntry(local.copy(offlineStatus = OfflineStatus.Error))
                    null
                }
            }

    private suspend fun downloadPending(entries: List<Entry>) =
        entries.asyncMap(MAX_CONCURRENT_DOWNLOADS) {
            downloadItem(it)
            repository.updateEntry(it.copy(offlineStatus = OfflineStatus.Synced))
        }

    private fun dateCompare(d1: ZonedDateTime?, d2: ZonedDateTime?): Long {
        return (d1?.toInstant()?.epochSecond ?: 0) - (d2?.toInstant()?.epochSecond ?: 0)
    }

    private suspend fun downloadItem(entry: Entry) {
        repository.updateEntry(entry.copy(offlineStatus = OfflineStatus.InProgress))
        val output = File(repository.session.filesDir, entry.id)
        val uri = BrowseRepository().contentUri(entry.id)
        try {
            ContentDownloader.downloadFileTo(uri, output.path)
        } catch (ex: Exception) {
            repository.updateEntry(entry.copy(offlineStatus = OfflineStatus.Error))
        }
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "sync"
        private const val MAX_CONCURRENT_DOWNLOADS = 3
        private const val MAX_CONCURRENT_FETCHES = 5

        fun syncNow(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build()

            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager
                .getInstance(context)
                .beginUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.KEEP, request)
                .enqueue()
        }
    }
}
