package com.alfresco.content.data

import com.alfresco.content.apis.SitesApi
import com.alfresco.content.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SitesRepository {
    private val service: SitesApi by lazy {
        SessionManager.requireSession.createService(SitesApi::class.java)
    }

    private suspend fun nodes(userId: String, skipCount: Int, maxItems: Int): ResponsePaging {
        return ResponsePaging.with(service.listSiteMembershipsForPerson(
            userId,
            skipCount,
            maxItems,
            null,
            null,
            null,
            null
        ))
    }

    suspend fun getMySites(skipCount: Int, maxItems: Int): Flow<ResponsePaging> {
        return flow {
            emit(nodes("-me-", skipCount, maxItems))
        }
    }

    suspend fun deleteSite(entry: Entry) = service.deleteSite(entry.otherId ?: "", null)
}
