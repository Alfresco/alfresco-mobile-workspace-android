package com.alfresco.content.data

import android.content.Context
import android.net.Uri
import android.util.Log
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
import com.alfresco.download.ContentDownloader
import java.io.File
import java.lang.Exception
import java.time.ZonedDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException

class SyncWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    private val repository = OfflineRepository()

    override suspend fun doWork(): Result {

        val localEntries = buildLocalList().also { debugPrintEntries(it) }
        val remoteEntries = buildRemoteList().also { debugPrintEntries(it) }
        val ops = calculateDiff(localEntries, remoteEntries).also { debugPrintOperations(it) }
        processOperations(ops)

        return Result.success()
    }

    private fun debugPrintEntries(list: List<Entry>) {
        val out = if (list.count() > 0) {
            list.map { "${it.id}: ${it.title}" }.reduce { acc, string -> "$acc\n$string" }
        } else {
            "empty"
        }
        Log.d("SyncWorker", out)
    }

    private fun debugPrintOperations(list: List<Operation>) {
        val out = if (list.count() > 0) {
            list.map { "${it.javaClass}" }.reduce { acc, string -> "$acc\n$string" }
        } else {
            "empty"
        }
        Log.d("SyncWorker", out)
    }

    private fun buildLocalList(): List<Entry> =
        repository.fetchAllOfflineEntries()

    private suspend fun buildRemoteList(): List<Entry> =
        continuousMap(repository.fetchTopLevelOfflineEntries()) { entry, produce ->
            // need updated metadata only for local items
            val remote = if (entry.hasOfflineStatus) {
                try {
                    BrowseRepository().fetchEntry(entry.id)
                } catch (ex: HttpException) {
                    if (ex.code() == 404) {
                        null
                    } else {
                        throw ex
                    }
                }
            } else {
                entry
            }
            if (remote?.isFolder == true) {
                // TODO: parallel fetch after the first page
                var page: ResponsePaging? = null
                var skip = 0L
                do {
                    page = BrowseRepository()
                        .fetchFolderItems(remote.id, skip.toInt(), MAX_PAGE_SIZE)
                    page.entries.map { produce(it) }
                    skip = page.pagination.skipCount + page.pagination.count
                } while (page?.pagination?.hasMoreItems == true)
            }
            remote
        }.distinctBy { it.id }

    private suspend fun <T, R> continuousMap(initial: Collection<T>, f: suspend (T, suspend(T) -> Unit) -> R?): List<R> {
        val queue = ArrayDeque<T>(initial)
        val result = mutableListOf<R>()

        val produce: suspend (T) -> Unit = {
            queue.addLast(it)
        }

        while (!queue.isEmpty()) {
            val peek = queue.first()
            val res = f(peek, produce)
            if (res != null) {
                result.add(res)
            }
            queue.removeFirst()
        }
        return result
    }

    private fun calculateDiff(localList: List<Entry>, remoteList: List<Entry>): List<Operation> {
        val localMap = localList.associateBy({ it.id }, { it })
        val remoteMap = remoteList.associateBy({ it.id }, { it })
        val operations = arrayListOf<Operation>()

        operations.addAll(localList.mapNotNull { local ->
            val remote = remoteMap[local.id]
            if (remote != null) {
                if (contentHasChanged(local, remote)) {
                    Operation.UpdateContent(local, remote)
                } else if (!local.metadataEquals(remote)) {
                    Operation.UpdateMetadata(local, remote)
                } else {
                    null
                }
            } else {
                Operation.Delete(local)
            }
        })

        operations.addAll(remoteList.mapNotNull { remote ->
            val local = localMap[remote.id]
            if (local == null) {
                Operation.Create(remote)
            } else {
                null
            }
        })

        return operations
    }

    private fun contentHasChanged(local: Entry, remote: Entry) =
        remote.modified?.toEpochSecond() != local.modified?.toEpochSecond() ||
            (local.isFile && local.offlineStatus != OfflineStatus.Synced)

    private fun dateCompare(d1: ZonedDateTime?, d2: ZonedDateTime?) =
        (d1?.toInstant()?.epochSecond ?: 0) - (d2?.toInstant()?.epochSecond ?: 0)

    private suspend fun processOperations(operations: List<Operation>) =
        operations.asyncMap(MAX_CONCURRENT_OPERATIONS) {
            when (it) {
                is Operation.Create -> {
                    val local = createEntry(it.remote)
                    downloadContent(local)
                }
                is Operation.UpdateMetadata -> {
                    updateEntryMetadata(it.local, it.remote)
                }
                is Operation.UpdateContent -> {
                    val local = updateEntryMetadata(it.local, it.remote)
                    // TODO: fix file rename use-case
                    downloadContent(local)
                }
                is Operation.Delete -> {
                    removeEntry(it.local)
                }
            }
        }

    private fun createEntry(entry: Entry) =
        repository.updateEntry(entry.copy(offlineStatus = OfflineStatus.Pending))

    private fun updateEntryMetadata(local: Entry, remote: Entry) =
        repository.updateEntry(local.copyWithMetadata(remote))

    private fun removeEntry(entry: Entry): Boolean {
        val dir = repository.contentDir(entry)
        var deleted = true
        if (dir.exists()) {
            deleted = dir.deleteRecursively()
        }
        Log.d("SyncWorker", "Deleted: ${entry.id}: ${entry.title}")
        if (deleted) {
            deleted = repository.remove(entry)
        }
        return deleted
    }

    private suspend fun downloadContent(entry: Entry) {
        if (entry.type == Entry.Type.File) {
            downloadItem(entry)
            downloadRendition(entry)
            repository.updateEntry(entry.copy(offlineStatus = OfflineStatus.Synced))
        }
    }

    private suspend fun downloadItem(entry: Entry) {
        repository.updateEntry(entry.copy(offlineStatus = OfflineStatus.InProgress))
        val outputDir = repository.contentDir(entry)
        outputDir.mkdir()
        val output = File(outputDir, entry.title)
        val uri = BrowseRepository().contentUri(entry)
        Log.d("SyncWorker", "Downloaded: ${entry.id}: ${entry.title}")
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
                    val outputDir = repository.contentDir(entry)
                    outputDir.mkdir()
                    val output = File(outputDir, ".preview$typeSuffix")
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
        private const val MAX_CONCURRENT_OPERATIONS = 3
        private const val MAX_PAGE_SIZE = 100
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
                    with(it?.firstOrNull()) { this?.state }
                }

        fun cancel(context: Context) {
            WorkManager
                .getInstance(context)
                .cancelUniqueWork(UNIQUE_WORK_NAME)
        }
    }

    sealed class Operation() {
        class Create(val remote: Entry) : Operation()
        class UpdateMetadata(val local: Entry, val remote: Entry) : Operation()
        class UpdateContent(val local: Entry, val remote: Entry) : Operation()
        class Delete(val local: Entry) : Operation()
    }
}
