package com.alfresco.content.data

import com.alfresco.content.apis.AlfrescoApi
import com.alfresco.content.apis.SharedLinksApi
import com.alfresco.content.session.ActionSessionInvalid
import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager
import com.alfresco.content.session.SessionNotFoundException
import com.alfresco.events.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SharedLinksRepository() {

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

    private val service: SharedLinksApi by lazy {
        session.createService(SharedLinksApi::class.java)
    }

    suspend fun getSharedLinks(skipCount: Int, maxItems: Int) =
        ResponsePaging.with(
            service.listSharedLinks(
                skipCount,
                maxItems,
                include = AlfrescoApi.csvQueryParam("path", "isFavorite", "allowableOperations"),
            ),
        )
}
