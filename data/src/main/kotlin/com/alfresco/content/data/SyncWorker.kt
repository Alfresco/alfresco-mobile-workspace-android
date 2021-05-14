package com.alfresco.content.data

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.alfresco.coroutines.asyncMap
import com.alfresco.download.ContentDownloader
import java.io.File
import java.lang.Exception
import retrofit2.HttpException

class SyncWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    private val repository = OfflineRepository()

    override suspend fun doWork(): Result {
        try {
            val localEntries = buildLocalList()
            val remoteEntries = buildRemoteList()
            val ops = calculateDiff(localEntries, remoteEntries)
            val uploadOps = pendingUploads()
            Log.d("SyncWorker", uploadOps.toString())
            processOperations(ops + uploadOps)
        } catch (_: Exception) {
        }

        // Always return success so we don't cancel APPEND work
        return Result.success()
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
                    if (ex.code() == HTTP_STATUS_FORBIDDEN ||
                        ex.code() == HTTP_STATUS_NOT_FOUND) {
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
                var page: ResponsePaging?
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
        val queue = ArrayDeque(initial)
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
            (local.isFile && local.offlineStatus != OfflineStatus.SYNCED)

    private fun pendingUploads(): List<Operation> =
        repository.fetchPendingUploads().map {
            Operation.CreateRemote(it)
        }

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
                is Operation.CreateRemote -> {
                    createItem(it.local)
                }
                is Operation.Delete -> {
                    removeEntry(it.local)
                }
            }
        }

    private fun createEntry(entry: Entry) =
        repository.updateEntry(entry.copy(offlineStatus = OfflineStatus.PENDING))

    private fun updateEntryMetadata(local: Entry, remote: Entry) =
        repository.updateEntry(local.copyWithMetadata(remote))

    private fun removeEntry(entry: Entry): Boolean {
        val dir = repository.contentDir(entry)
        var deleted = true
        if (dir.exists()) {
            deleted = dir.deleteRecursively()
        }
        if (deleted) {
            deleted = repository.remove(entry)
        }
        return deleted
    }

    private suspend fun downloadContent(entry: Entry) {
        if (entry.isFile) {
            try {
                downloadItem(entry)
                downloadRendition(entry)
                repository.updateEntry(entry.copy(offlineStatus = OfflineStatus.SYNCED))
            } catch (_: Exception) {
                repository.updateEntry(entry.copy(offlineStatus = OfflineStatus.ERROR))
            }
        }
    }

    private suspend fun downloadItem(entry: Entry) {
        repository.updateEntry(entry.copy(offlineStatus = OfflineStatus.SYNCING))
        val outputDir = repository.contentDir(entry)
        outputDir.mkdir()
        val output = File(outputDir, entry.name)
        val uri = BrowseRepository().contentUri(entry)
        ContentDownloader.downloadFileTo(uri, output.path)
    }

    private suspend fun downloadRendition(entry: Entry) {
        if (!typeSupported(entry)) {
            val rendition = RenditionRepository().fetchRendition(entry.id)
            if (rendition != null) {
                val outputDir = repository.contentDir(entry)
                outputDir.mkdir()
                val output = File(outputDir, rendition.offlineFileName)
                ContentDownloader.downloadFileTo(rendition.uri, output.path)
            }
        }
    }

    private fun typeSupported(entry: Entry) =
        previewRegistry?.isPreviewSupported(entry.mimeType) ?: false

    private suspend fun createItem(entry: Entry) {
        try {
            repository.updateEntry(entry.copy(offlineStatus = OfflineStatus.SYNCING))
            val file = repository.contentFile(entry)
            val res = BrowseRepository().createEntry(entry, file)
            file.delete() // TODO: what if delete fails?
            repository.updateEntry(
                entry.copyWithMetadata(res)
                    .copy(id = res.id, offlineStatus = OfflineStatus.SYNCED)
            )
        } catch (ex: Exception) {
            repository.updateEntry(entry.copy(offlineStatus = OfflineStatus.ERROR))
            Log.e("SyncWorker", ex.toString())
        }
    }

    companion object {
        private const val MAX_CONCURRENT_OPERATIONS = 3
        private const val MAX_PAGE_SIZE = 100
        private const val HTTP_STATUS_FORBIDDEN = 403
        private const val HTTP_STATUS_NOT_FOUND = 404
        private var previewRegistry: PreviewRegistry? = null

        fun use(previewRegistry: PreviewRegistry) {
            this.previewRegistry = previewRegistry
        }
    }

    sealed class Operation {
        class Create(val remote: Entry) : Operation()
        class CreateRemote(val local: Entry) : Operation()
        class UpdateMetadata(val local: Entry, val remote: Entry) : Operation()
        class UpdateContent(val local: Entry, val remote: Entry) : Operation()
        class Delete(val local: Entry) : Operation()
    }
}
