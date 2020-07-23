package com.alfresco.content.data

import com.alfresco.content.apis.NodesApi
import com.alfresco.content.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class BrowseRepository() {

    private val service: NodesApi by lazy {
        SessionManager.requireSession.createService(NodesApi::class.java)
    }

    suspend fun loadItemsInFolder(folderId: String, skipCount: Int, maxItems: Int): Flow<ResponsePaging> {
        return flow {
            emit(fetchItemsInFolder(folderId, skipCount, maxItems))
        }
    }

    suspend fun loadMyFiles(skipCount: Int, maxItems: Int): Flow<ResponsePaging> {
        return loadItemsInFolder("-my-", skipCount, maxItems)
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
