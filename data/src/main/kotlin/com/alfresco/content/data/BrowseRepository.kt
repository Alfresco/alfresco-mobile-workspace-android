package com.alfresco.content.data

import com.alfresco.content.apis.NodesApi
import com.alfresco.content.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class BrowseRepository() {

    private val service: NodesApi by lazy {
        SessionManager.requireSession.createService(NodesApi::class.java)
    }

    private suspend fun files(path: String): List<Entry> {
        return service.listNodeChildren(
            path,
            null,
            25,
            null,
            null,
            null,
            null,
            null,
            null
        ).list?.entries?.map { Entry.with(it.entry) } ?: emptyList()
    }

    suspend fun getNodes(path: String): Flow<List<Entry>> {
        return flow {
            emit(files(path))
        }
    }

    suspend fun getMyFiles(): Flow<List<Entry>> {
        return getNodes("-my-")
    }
}
