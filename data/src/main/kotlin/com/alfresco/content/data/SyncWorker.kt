package com.alfresco.content.data

import android.content.Context
import android.net.Uri
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.alfresco.coroutines.asFlow
import com.alfresco.coroutines.asyncMap
import com.alfresco.coroutines.asyncMapNotNull
import com.alfresco.download.ContentDownloader
import java.io.File
import java.lang.Exception
import java.time.ZonedDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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
            downloadRendition(it)
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

    private suspend fun downloadRendition(entry: Entry) {
        if (!typeSupported(entry)) {
            val uri = RenditionRepository().fetchRenditionUri(entry.id)
            if (uri != null) {
                try {
                    val typeSuffix = renditionTypeSuffix(uri)
                    val output = File(repository.session.filesDir, "${entry.id}$RENDITION_FILE_SUFFIX$typeSuffix")
                    ContentDownloader.downloadFileTo(uri, output.path)
                } catch (ex: Exception) {
                    repository.updateEntry(entry.copy(offlineStatus = OfflineStatus.Error))
                }
            }
        }
    }

    private fun renditionTypeSuffix(uri: String) =
        if (Uri.parse(uri).pathSegments.contains("pdf")) {
            "_pdf"
        } else {
            "_img"
        }

    private fun typeSupported(entry: Entry) =
        entry.mimeType == "application/pdf" ||
            supportedImageFormats.contains(entry.mimeType) ||
            entry.mimeType?.startsWith("text/") == true ||
            entry.mimeType?.startsWith("audio/") == true ||
            entry.mimeType?.startsWith("video/") == true

    companion object {
        private const val UNIQUE_WORK_NAME = "sync"
        private const val MAX_CONCURRENT_DOWNLOADS = 3
        private const val MAX_CONCURRENT_FETCHES = 5
        private const val RENDITION_FILE_SUFFIX = "_pv"
        private val supportedImageFormats = setOf("image/bmp", "image/jpeg", "image/png", "image/gif", "image/webp", "image/gif", "image/svg+xml")

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

        fun observe(context: Context): Flow<WorkInfo.State?> =
            WorkManager
                .getInstance(context)
                .getWorkInfosForUniqueWorkLiveData(UNIQUE_WORK_NAME)
                .asFlow()
                .map {
                    with(it?.first()) { this?.state }
                }
    }
}
