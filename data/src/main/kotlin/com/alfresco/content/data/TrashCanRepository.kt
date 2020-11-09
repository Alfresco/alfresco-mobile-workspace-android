package com.alfresco.content.data

import com.alfresco.content.apis.TrashcanApi
import com.alfresco.content.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TrashCanRepository {
    private val service: TrashcanApi by lazy {
        SessionManager.requireSession.createService(TrashcanApi::class.java)
    }

    private suspend fun nodes(skipCount: Int, maxItems: Int): ResponsePaging {
        val include = listOf("path")
        return ResponsePaging.with(service.listDeletedNodes(
            skipCount,
            maxItems,
            include
        ))
    }

    suspend fun getDeletedNodes(skipCount: Int, maxItems: Int): Flow<ResponsePaging> {
        return flow {
            emit(nodes(skipCount, maxItems))
        }
    }

    suspend fun restoreEntry(entry: Entry) =
        Entry.with(service.restoreDeletedNode(
            entry.id,
            null
        ).entry)

    suspend fun deleteForeverEntry(entry: Entry) =
        service.deleteDeletedNode(entry.id)
}
