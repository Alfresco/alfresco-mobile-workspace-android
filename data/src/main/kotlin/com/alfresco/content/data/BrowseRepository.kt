package com.alfresco.content.data

import android.content.Context
import com.alfresco.content.apis.NodesApi
import com.alfresco.content.models.Node
import com.alfresco.content.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class BrowseRepository(context: Context) {

    private val service: NodesApi by lazy {
        SessionManager.requireSession.createService(NodesApi::class.java)
    }

    private suspend fun myFiles(): List<Node> {
        return service.listNodeChildren(
            "-my-",
            null,
            25,
            null,
            null,
            null,
            null,
            null,
            null).list?.entries?.map { with(it.entry) } ?: emptyList()
    }

    suspend fun getNodes(): Flow<List<Node>> {
        return flow {
            emit(myFiles())
        }
    }
}
