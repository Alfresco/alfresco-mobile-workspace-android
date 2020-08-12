package com.alfresco.content.data

import com.alfresco.content.apis.NodesApi
import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class BrowseRepository(session: Session = SessionManager.requireSession) {

    private val service: NodesApi by lazy {
        session.createService(NodesApi::class.java)
    }

    val myFilesNodeId: String get() = SessionManager.requireSession.account.myFiles ?: ""

    suspend fun loadItemsInFolder(folderId: String, skipCount: Int, maxItems: Int): Flow<ResponsePaging> {
        return flow {
            emit(fetchItemsInFolder(folderId, skipCount, maxItems))
        }
    }

    suspend fun myFilesNodeId(): String {
        return service.getNode("-my-", null, null, null).entry.id
    }

    private suspend fun fetchItemsInFolder(folderId: String, skipCount: Int, maxItems: Int): ResponsePaging {
        return ResponsePaging.with(service.listNodeChildren(
            folderId,
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

    suspend fun loadItemsInSite(siteId: String, skipCount: Int, maxItems: Int): Flow<ResponsePaging> {
        return flow {
            emit(fetchLibraryItems(siteId, skipCount, maxItems))
        }
    }

    private suspend fun fetchLibraryItems(siteId: String, skipCount: Int, maxItems: Int): ResponsePaging {
        return ResponsePaging.with(service.listNodeChildren(
            siteId,
            skipCount,
            maxItems,
            null,
            null,
            null,
            "documentLibrary",
            null,
            null
        ))
    }
}
