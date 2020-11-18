package com.alfresco.content.data

import com.alfresco.content.apis.AlfrescoApi
import com.alfresco.content.apis.SharedLinksApi
import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager

class SharedLinksRepository(val session: Session = SessionManager.requireSession) {
    private val service: SharedLinksApi by lazy {
        session.createService(SharedLinksApi::class.java)
    }

    suspend fun getSharedLinks(skipCount: Int, maxItems: Int) =
        ResponsePaging.with(service.listSharedLinks(
            skipCount,
            maxItems,
            include = AlfrescoApi.csvQueryParam("path", "isFavorite", "allowableOperations")
        ))
}
