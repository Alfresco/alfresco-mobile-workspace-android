package com.alfresco.content.data

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.kotlin.boxFor
import java.io.File
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class OfflineRepository(val session: Session = SessionManager.requireSession) {

    private object ObjectBox {
        lateinit var boxStore: BoxStore
            private set

        fun init(context: Context) {
            synchronized(this) {
                if (!this::boxStore.isInitialized) {
                    boxStore = MyObjectBox.builder()
                        .androidContext(context)
                        .build()
                }
            }
        }
    }

    private val box: Box<Entry>

    init {
        ObjectBox.init(session.context)
        box = ObjectBox.boxStore.boxFor()
    }

    fun entry(id: String): Entry? =
        box.query()
            .equal(Entry_.id, id)
            .build()
            .findFirst()

    fun markForSync(entry: Entry) =
        update(entry.copy(isOffline = true, offlineStatus = OfflineStatus.PENDING))

    fun removeFromSync(entry: Entry) =
        update(entry.copy(isOffline = false))

    fun remove(entry: Entry) =
        box.remove(entry)

    fun update(entry: Entry) =
        entry.also { box.put(it) }

    /**
     * updating the total transfer count
     */
    fun updateTransferSize(size: Int) {
        println("syncing test update size : $size")
        val list = box.query().equal(Entry_.isTotalEntry, true).build().find()
        if (list.isEmpty()) {
            val entry = Entry(totalCount = size, isTotalEntry = true)
            entry.also { box.put(it) }
        } else {
            val entryObj = list[0]
            val entry = entryObj.copy(totalCount = size)
            entry.also { box.put(it) }
        }
    }

    /**
     * returns the transfer size count
     */
    fun getTotalTransfersSize(): Int {
        val list = box.query()
            .equal(Entry_.isTotalEntry, true)
            .build()
            .find()
        val size = if (list.isEmpty()) 0 else list[0].totalCount
        println("syncing test get size : $size")
        return size
    }

    fun offlineEntries(parentId: String?): Flow<ResponsePaging> = callbackFlow {
        val query = offlineEntriesQuery(parentId)
        val subscription = query.subscribe()
            .observer { data ->
                val count = data.count().toLong()
                trySendBlocking(
                    ResponsePaging(
                        data,
                        Pagination(
                            count,
                            false,
                            0,
                            count,
                            count
                        )
                    )
                )
            }
        awaitClose { subscription.cancel() }
    }

    private fun offlineEntriesQuery(parentId: String?) =
        box.query()
            .apply {
                if (parentId != null) {
                    equal(Entry_.parentId, parentId)
                    // Exclude uploads from synced folders
                    equal(Entry_.isUpload, false)
                } else {
                    equal(Entry_.isOffline, true)
                }
            }
            .order(Entry_.name)
            .build()

    internal fun fetchTopLevelOfflineEntries() =
        box.query()
            .equal(Entry_.isOffline, true)
            .build()
            .find()

    internal fun fetchAllOfflineEntries() =
        box.query()
            .notEqual(Entry_.offlineStatus, OfflineStatus.UNDEFINED.value())
            .equal(Entry_.isUpload, false)
            .build()
            .find()

    private fun fetchAllTransferEntries() =
        box.query()
            .notEqual(Entry_.offlineStatus, OfflineStatus.UNDEFINED.value())
            .equal(Entry_.isUpload, true)
            .build()
            .find()

    internal fun fetchOfflineEntry(target: Entry) = entry(target.id)

    /**
     * returns the pending transfer list from database
     */
    fun buildTransferList(): List<Entry> =
        fetchAllTransferEntries()

    /**
     * update transfer size count using the parent ID
     */
    fun setTotalTransferSize(size: Int) {
        val count = getTotalTransfersSize()
        val list = fetchAllTransferEntries()

        println("syncing test count = $count :: list size = ${list.size} :: listById size $size")

        if (list.isEmpty())
            updateTransferSize(size)
        else updateTransferSize(count + size)
    }

    fun scheduleContentForUpload(
        context: Context,
        contentUri: Uri,
        parentId: String,
        isExtension: Boolean = false
    ) {
        val resolver = context.contentResolver
        var name: String? = null
        val projection = arrayOf(MediaStore.Files.FileColumns.DISPLAY_NAME)
        val cursor = resolver.query(contentUri, projection, null, null, null)
        if (cursor != null) {
            cursor.moveToFirst()
            name = cursor.getString(0)
            cursor.close()
        }
        val mimeType = resolver.getType(contentUri)

        requireNotNull(name)
        requireNotNull(mimeType)

        val entry = Entry(
            parentId = parentId,
            name = name,
            type = Entry.Type.FILE,
            mimeType = mimeType,
            isUpload = true,
            offlineStatus = OfflineStatus.PENDING
        )

        clearData()

        update(entry)

        val dest = File(session.uploadDir, entry.boxId.toString())

        resolver.openInputStream(contentUri).use { input ->
            requireNotNull(input)

            dest.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun clearData() {
        removeCompletedUploads()
        if (buildTransferList().isEmpty())
            updateTransferSize(0)
    }

    fun scheduleForUpload(
        path: String,
        parentId: String,
        name: String,
        description: String,
        mimeType: String
    ) {
        // TODO: This process may fail resulting in an orphan file? or node?
        val entry = Entry(
            parentId = parentId,
            name = name,
            type = Entry.Type.FILE,
            mimeType = mimeType,
            properties = mapOf("cm:description" to description),
            isUpload = true,
            offlineStatus = OfflineStatus.PENDING
        )

        clearData()

        update(entry)
        val srcPath = path.removePrefix("file://")
        File(srcPath).renameTo(File(session.uploadDir, entry.boxId.toString()))
    }

    internal fun fetchPendingUploads() =
        box.query()
            .equal(Entry_.isUpload, true)
            .notEqual(Entry_.offlineStatus, OfflineStatus.SYNCED.value())
            .build()
            .find()

    fun observeUploads(parentId: String): Flow<List<Entry>> =
        callbackFlow {
            val query = box.query()
                .equal(Entry_.parentId, parentId)
                .equal(Entry_.isUpload, true)
                .order(Entry_.name)
                .build()
            val subscription = query.subscribe()
                .observer {
                    trySendBlocking(it)
                }
            awaitClose { subscription.cancel() }
        }

    /**
     * observer for transfer uploads
     */
    fun observeTransferUploads(): Flow<List<Entry>> =
        callbackFlow {
            val query = box.query()
                .equal(Entry_.isUpload, true)
                .equal(Entry_.id, "")
                .order(Entry_.name)
                .build()
            val subscription = query.subscribe()
                .observer {
                    trySendBlocking(it)
                }
            awaitClose { subscription.cancel() }
        }

    // Removes a completed upload with id
    fun removeUpload(id: String) =
        box.query()
            .equal(Entry_.id, id)
            .equal(Entry_.isUpload, true)
            .build()
            .remove()

    fun removeCompletedUploads(parentId: String? = null) =
        box.query()
            .equal(Entry_.isUpload, true)
            .equal(Entry_.offlineStatus, OfflineStatus.SYNCED.value())
            .apply {
                if (parentId != null) {
                    equal(Entry_.parentId, parentId)
                }
            }
            .build()
            .remove()

    fun contentUri(entry: Entry): String =
        "file://${contentFile(entry).absolutePath}"

    fun contentFile(entry: Entry): File =
        if (entry.isUpload) {
            File(session.uploadDir, entry.boxId.toString())
        } else {
            File(contentDir(entry), entry.name)
        }

    fun contentDir(entry: Entry): File =
        File(SessionManager.requireSession.filesDir, entry.id)

    fun cleanup() {
        SyncService.cancel(session.context)
        removeAllEntries()
        removeAllFiles()
    }

    private fun removeAllEntries() {
        val query = box.query().build()
        query.remove()
    }

    private fun removeAllFiles() {
        SessionManager
            .requireSession
            .filesDir
            .deleteRecursively()
    }
}
