package com.alfresco.content.data

import com.alfresco.content.apis.AlfrescoApi
import com.alfresco.content.apis.NodesApi
import com.alfresco.content.apis.getMyNode
import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager

class BrowseRepository(val session: Session = SessionManager.requireSession) {

    private val service: NodesApi by lazy {
        session.createService(NodesApi::class.java)
    }

    val myFilesNodeId: String get() = SessionManager.requireSession.account.myFiles ?: ""

    suspend fun myFilesNodeId() =
        service.getMyNode().entry.id

    suspend fun fetchFolderItems(folderId: String, skipCount: Int, maxItems: Int) =
        ResponsePaging.with(service.listNodeChildren(
            folderId,
            skipCount,
            maxItems,
            include = extraFields()
        ))

    suspend fun fetchLibraryItems(siteId: String, skipCount: Int, maxItems: Int) =
        ResponsePaging.with(service.listNodeChildren(
            siteId,
            skipCount,
            maxItems,
            include = extraFields(),
            relativePath = "documentLibrary"
        ))

    private fun extraFields() =
        AlfrescoApi.csvQueryParam("path", "isFavorite", "allowableOperations", "properties")

    suspend fun fetchEntry(entryId: String) =
        Entry.with(service.getNode(
            entryId,
            extraFields()
        ).entry)

    suspend fun deleteEntry(entry: Entry) =
        service.deleteNode(entry.id, null)

    fun contentUri(entry: Entry): String {
        val baseUrl = SessionManager.currentSession?.baseUrl
        return "${baseUrl}alfresco/versions/1/nodes/${entry.id}/content?attachment=false&alf_ticket=${session.ticket}"
    }
}
