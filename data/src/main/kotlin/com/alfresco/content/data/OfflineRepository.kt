package com.alfresco.content.data

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.kotlin.boxFor
import io.objectbox.query.Query
import java.io.File
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class OfflineRepository(val session: Session = SessionManager.requireSession) {

    private object ObjectBox {
        lateinit var boxStore: BoxStore
            private set

        fun init(context: Context) {
            if (!this::boxStore.isInitialized) {
                boxStore = MyObjectBox.builder()
                    .androidContext(context)
                    .build()
            }
        }
    }

    init {
        ObjectBox.init(session.context)
    }

    fun entry(id: String): Entry? {
        val box: Box<Entry> = ObjectBox.boxStore.boxFor()
        val query = box.query()
            .equal(Entry_.id, id)
            .build()
        return query.findFirst()
    }

    fun markOffline(entry: Entry) =
        updateEntry(entry.copy(isOffline = true, offlineStatus = OfflineStatus.PENDING))

    fun markForRemoval(entry: Entry) =
        updateEntry(entry.copy(isOffline = false))

    fun remove(entry: Entry) =
        entry.let {
            val box: Box<Entry> = ObjectBox.boxStore.boxFor()
            box.remove(it)
        }

    fun updateEntry(entry: Entry) =
        entry.also {
            val box: Box<Entry> = ObjectBox.boxStore.boxFor()
            box.put(it)
        }

    fun fetchOfflineEntries(parentId: String?): ResponsePaging {
        val query = offlineEntriesQuery(parentId)
        val data = query.find()
        val count = data.count().toLong()
        return ResponsePaging(
            data,
            Pagination(
                count,
                false,
                0,
                count,
                count
            )
        )
    }

    fun observeOfflineEntries(parentId: String?): Flow<ResponsePaging> = callbackFlow {
        val query = offlineEntriesQuery(parentId)
        val subscription = query.subscribe()
            .observer { data ->
                val count = data.count().toLong()
                sendBlocking(
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

    private fun offlineEntriesQuery(parentId: String?): Query<Entry> {
        val box: Box<Entry> = ObjectBox.boxStore.boxFor()
        var query = box.query()
        query = if (parentId != null) {
            query.equal(Entry_.parentId, parentId)
        } else {
            query.equal(Entry_.isOffline, true)
        }
        query.notEqual(Entry_.isUpload, true)
        return query.order(Entry_.name).build()
    }

    fun fetchTopLevelOfflineEntries(): List<Entry> {
        val box: Box<Entry> = ObjectBox.boxStore.boxFor()
        val query = box.query()
            .equal(Entry_.isOffline, true)
            .build()
        return query.find()
    }

    fun fetchAllOfflineEntries(): List<Entry> {
        val box: Box<Entry> = ObjectBox.boxStore.boxFor()
        val query = box.query()
            .notEqual(Entry_.offlineStatus, OfflineStatus.UNDEFINED.value())
            .equal(Entry_.isUpload, false)
            .build()
        return query.find()
    }

    fun fetchOfflineEntry(target: Entry): Entry? {
        val box: Box<Entry> = ObjectBox.boxStore.boxFor()
        val query = box.query()
            .equal(Entry_.id, target.id)
            .build()
        return query.findFirst()
    }

    fun scheduleContentForUpload(
        context: Context,
        contentUri: Uri,
        parentId: String
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
        updateEntry(entry)

        val dest = File(session.uploadDir, entry.boxId.toString())

        resolver.openInputStream(contentUri).use { input ->
            requireNotNull(input)

            dest.outputStream().use { output ->
                input.copyTo(output)
            }
        }
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
        updateEntry(entry)
        File(path).renameTo(File(session.uploadDir, entry.boxId.toString()))
    }

    fun fetchPendingUploads(): List<Entry> {
        val box: Box<Entry> = ObjectBox.boxStore.boxFor()
        val query = box.query()
            .equal(Entry_.isUpload, true)
            .notEqual(Entry_.offlineStatus, OfflineStatus.SYNCED.value())
            .build()
        return query.find()
    }

    fun observeUploads(parentId: String): Flow<List<Entry>> =
        callbackFlow {
            val box: Box<Entry> = ObjectBox.boxStore.boxFor()
            val query = box.query()
                .equal(Entry_.parentId, parentId)
                .equal(Entry_.isUpload, true)
                .order(Entry_.name)
                .build()
            val subscription = query.subscribe()
                .observer {
                    sendBlocking(it)
                }
            awaitClose { subscription.cancel() }
        }

    fun removeUpload(id: String) {
        val box: Box<Entry> = ObjectBox.boxStore.boxFor()
        box.query()
            .equal(Entry_.id, id)
            .equal(Entry_.isUpload, true)
            .build()
            .remove()
    }

    fun removeCompletedUploads(parentId: String? = null) {
        val box: Box<Entry> = ObjectBox.boxStore.boxFor()
        val query = box.query()
            .equal(Entry_.isUpload, true)
            .equal(Entry_.offlineStatus, OfflineStatus.SYNCED.value())
            .apply {
                if (parentId != null) {
                    equal(Entry_.parentId, parentId)
                }
            }
            .build()
        query.remove()
    }

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
        val box: Box<Entry> = ObjectBox.boxStore.boxFor()
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
