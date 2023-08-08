package com.alfresco.content.data

import com.alfresco.content.apis.AlfrescoApi
import com.alfresco.content.apis.TrashcanApi
import com.alfresco.content.apis.TrashcanApiExt
import com.alfresco.content.session.ActionSessionInvalid
import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager
import com.alfresco.content.session.SessionNotFoundException
import com.alfresco.events.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TrashCanRepository {

    lateinit var session: Session
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    init {
        try {
            session = SessionManager.requireSession
        } catch (e: SessionNotFoundException) {
            e.printStackTrace()
            coroutineScope.launch {
                EventBus.default.send(ActionSessionInvalid(true))
            }
        }
    }

    private val service: TrashcanApi by lazy {
        session.createService(TrashcanApi::class.java)
    }

    private val serviceExt: TrashcanApiExt by lazy {
        session.createService(TrashcanApiExt::class.java)
    }

    suspend fun getDeletedNodes(skipCount: Int, maxItems: Int) =
        ResponsePaging.with(
            service.listDeletedNodes(
                skipCount,
                maxItems,
                AlfrescoApi.csvQueryParam("path"),
            ),
        )

    suspend fun restoreEntry(entry: Entry) =
        Entry.with(serviceExt.restoreDeletedNode(entry.id).entry)

    suspend fun deleteForeverEntry(entry: Entry) {
        service.deleteDeletedNode(entry.id)
    }
}
