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

    fun removeOffline(entry: Entry) =
        entry.copy(isOffline = false)
            .also {
                val box: Box<Entry> = ObjectBox.boxStore.boxFor()
                box.remove(it)
            }

    fun updateEntry(entry: Entry) =
        entry.also {
            val box: Box<Entry> = ObjectBox.boxStore.boxFor()
            box.put(it)
        }

    fun observeOfflineEntries(): Flow<ResponsePaging> = callbackFlow {
        val box: Box<Entry> = ObjectBox.boxStore.boxFor()
        val query = box.query()
            .order(Entry_.title)
            .build()
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

    fun fetchAllOfflineEntries(): List<Entry> {
        val box: Box<Entry> = ObjectBox.boxStore.boxFor()
        val query = box.query()
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

    fun contentUri(id: String): String =
        "file://${File(SessionManager.requireSession.filesDir, id).absolutePath}"
}
