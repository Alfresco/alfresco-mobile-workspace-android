package com.alfresco.content.data

import com.alfresco.content.apis.SitesApi
import com.alfresco.content.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SitesRepository {
    private val service: SitesApi by lazy {
        SessionManager.requireSession.createService(SitesApi::class.java)
    }

    private suspend fun nodes(userId: String): List<Entry> {
        val where = "(EXISTS(target/site))"
        return service.listSiteMembershipsForPerson(
            userId,
            null,
            25,
            null,
            null,
            null,
            null
        ).list.entries.map { Entry.with(it.entry) }
    }

    suspend fun getMySites(): Flow<List<Entry>> {
        return flow {
            emit(nodes("-me-"))
        }
    }
}
