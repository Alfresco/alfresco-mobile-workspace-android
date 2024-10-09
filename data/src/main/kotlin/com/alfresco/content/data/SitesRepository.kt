package com.alfresco.content.data

import com.alfresco.content.apis.AlfrescoApi
import com.alfresco.content.apis.SitesApi
import com.alfresco.content.session.ActionSessionInvalid
import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager
import com.alfresco.content.session.SessionNotFoundException
import com.alfresco.events.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SitesRepository {
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

    private val service: SitesApi by lazy {
        session.createService(SitesApi::class.java)
    }

    suspend fun getMySites(
        skipCount: Int,
        maxItems: Int,
    ) = ResponsePaging.with(
        service.listSiteMembershipsForPerson(
            AlfrescoApi.CURRENT_USER,
            skipCount,
            maxItems,
        ),
    )

    suspend fun deleteSite(entry: Entry) = service.deleteSite(entry.otherId ?: "", null)
}
