package com.alfresco.content.data

import android.content.Context
import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.kotlin.boxFor
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
        updateEntry(entry.copy(isOffline = true, offlineStatus = OfflineStatus.Pending))

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

    fun observeOfflineEntries(parentId: String?): Flow<ResponsePaging> = callbackFlow {
        val box: Box<Entry> = ObjectBox.boxStore.boxFor()
        var query = box.query()
        query = if (parentId != null) {
            query.equal(Entry_.parentId, parentId)
        } else {
            query.equal(Entry_.isOffline, true)
        }
        query = query
            .order(Entry_.title)
        val subscription = query.build()
            .subscribe()
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
            .notEqual(Entry_.offlineStatus, OfflineStatus.Undefined.value())
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

    fun contentUri(entry: Entry): String =
        "file://${contentFile(entry).absolutePath}"

    fun contentFile(entry: Entry): File =
        File(contentDir(entry), entry.title)

    fun contentDir(entry: Entry): File =
        File(SessionManager.requireSession.filesDir, entry.id)

    fun cleanup() {
        SyncWorker.cancel(session.context)
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
