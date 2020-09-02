package com.alfresco.content.data

import android.net.Uri
import com.alfresco.content.apis.NodesApi
import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager
import java.io.InputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class BrowseRepository(val session: Session = SessionManager.requireSession) {

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

    public suspend fun fetchContent(documentId: String): String {
        return service.getNodeContent(documentId, null, null, null).string()
    }

    public suspend fun fetchContentStream(documentId: String): InputStream {
        return service.getNodeContent(documentId, null, null, null).byteStream()
    }

    fun contentUri(id: String): Uri {
        val baseUrl = SessionManager.currentSession?.baseUrl
        return Uri.parse("${baseUrl}alfresco/versions/1/nodes/$id/content?attachment=false&alf_ticket=${session.ticket}")
    }

    fun renditionUri(id: String): Uri {
        val baseUrl = SessionManager.currentSession?.baseUrl
        return Uri.parse("${baseUrl}alfresco/versions/1/nodes/$id/renditions/pdf/content?attachment=false&alf_ticket=${session.ticket}")
    }
}
