package com.alfresco.content.data

import com.alfresco.content.apis.SharedLinksApi
import com.alfresco.content.models.Node
import com.alfresco.content.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SharedLinksRepository {
    private val service: SharedLinksApi by lazy {
        SessionManager.requireSession.createService(SharedLinksApi::class.java)
    }

    private suspend fun nodes(): List<Entry> {
        return service.listSharedLinks(
            0,
            25,
            null,
            null,
            null
        ).list.entries.map { Entry.with(it.entry) } ?: emptyList()
    }

    suspend fun getSharedLinks(): Flow<List<Entry>> {
        return flow {
            emit(nodes())
        }
    }
}
