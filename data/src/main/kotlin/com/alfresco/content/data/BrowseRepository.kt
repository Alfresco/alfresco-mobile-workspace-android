package com.alfresco.content.data

import com.alfresco.content.apis.NodesApi
import com.alfresco.content.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class BrowseRepository() {

    private val service: NodesApi by lazy {
        SessionManager.requireSession.createService(NodesApi::class.java)
    }

    private suspend fun files(path: String, skipCount: Int, maxItems: Int): ResponsePaging {
        return ResponsePaging.with(service.listNodeChildren(
            path,
            skipCount,
            maxItems,
            null,
            null,
            null,
            null,
            null,
            null
        ))
    }

    suspend fun getNodes(path: String, skipCount: Int, maxItems: Int): Flow<ResponsePaging> {
        return flow {
            emit(files(path, skipCount, maxItems))
        }
    }

    suspend fun getMyFiles(skipCount: Int, maxItems: Int): Flow<ResponsePaging> {
        return getNodes("-my-", skipCount, maxItems)
    }
}
