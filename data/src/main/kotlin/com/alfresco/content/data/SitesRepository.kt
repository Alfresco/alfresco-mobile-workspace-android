package com.alfresco.content.data

import com.alfresco.content.apis.AlfrescoApi
import com.alfresco.content.apis.SitesApi
import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager

class SitesRepository(val session: Session = SessionManager.requireSession) {
    private val service: SitesApi by lazy {
        session.createService(SitesApi::class.java)
    }

    suspend fun getMySites(skipCount: Int, maxItems: Int) =
        ResponsePaging.with(service.listSiteMembershipsForPerson(
            AlfrescoApi.CURRENT_USER,
            skipCount,
            maxItems
        ))

    suspend fun deleteSite(entry: Entry) =
        service.deleteSite(entry.otherId ?: "", null)
}
