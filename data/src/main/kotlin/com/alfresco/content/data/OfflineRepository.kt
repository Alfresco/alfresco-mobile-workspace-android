package com.alfresco.content.data

import android.content.Context
import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.kotlin.boxFor

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

    fun markOffline(entry: Entry): Entry =
        entry.copy(isOffline = true)
            .also {
                val box: Box<Entry> = ObjectBox.boxStore.boxFor()
                box.put(it)
            }

    fun removeOffline(entry: Entry) =
        entry.copy(isOffline = false)
            .also {
                val box: Box<Entry> = ObjectBox.boxStore.boxFor()
                box.remove(it)
            }

    suspend fun fetchOfflineEntries(skipCount: Int, maxItems: Int): ResponsePaging {
        val box: Box<Entry> = ObjectBox.boxStore.boxFor()
        val query = box.query()
            .order(Entry_.title)
            .build()
        val results = query.find(skipCount.toLong(), maxItems.toLong())
        return ResponsePaging(results, Pagination(
            results.count().toLong(),
            results.count() > 0, //  && results.count() + skipCount < query.count()
            skipCount.toLong(),
            maxItems.toLong(),
            query.count()
        ))
    }

    fun fetchOfflineEntry(target: Entry): Entry? {
        val box: Box<Entry> = ObjectBox.boxStore.boxFor()
        val query = box.query()
            .equal(Entry_.id, target.id)
            .build()
        return query.findFirst()
    }
}
