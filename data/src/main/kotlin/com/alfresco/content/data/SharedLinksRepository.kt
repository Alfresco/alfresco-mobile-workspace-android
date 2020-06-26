package com.alfresco.content.data

import com.alfresco.content.apis.SharedLinksApi
import com.alfresco.content.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SharedLinksRepository {
    private val service: SharedLinksApi by lazy {
        SessionManager.requireSession.createService(SharedLinksApi::class.java)
    }

    private suspend fun nodes(skipCount: Int, maxItems: Int): ResponsePaging {
        val include = listOf("path")
        return ResponsePaging.with(service.listSharedLinks(
            skipCount,
            maxItems,
            null,
            include,
            null
        ))
    }

    suspend fun getSharedLinks(skipCount: Int, maxItems: Int): Flow<ResponsePaging> {
        return flow {
            emit(nodes(skipCount, maxItems))
        }
    }
}
