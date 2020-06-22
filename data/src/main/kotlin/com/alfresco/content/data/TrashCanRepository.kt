package com.alfresco.content.data

import com.alfresco.content.apis.TrashcanApi
import com.alfresco.content.models.Node
import com.alfresco.content.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TrashCanRepository {
    private val service: TrashcanApi by lazy {
        SessionManager.requireSession.createService(TrashcanApi::class.java)
    }

    private suspend fun nodes(): List<Entry> {
        val include = listOf("path")
        return service.listDeletedNodes(
            0,
            25,
            include
        ).list?.entries?.map { Entry.with(it.entry!!) } ?: emptyList()
    }

    suspend fun getDeletedNodes(): Flow<List<Entry>> {
        return flow {
            emit(nodes())
        }
    }
}
