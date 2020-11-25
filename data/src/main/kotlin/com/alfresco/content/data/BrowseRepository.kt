package com.alfresco.content.data

import com.alfresco.content.apis.AlfrescoApi
import com.alfresco.content.apis.NodesApi
import com.alfresco.content.apis.getMyNode
import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager
import java.io.File
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

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
            include = extraFieldsFolder()
        ))

    suspend fun fetchLibraryItems(siteId: String, skipCount: Int, maxItems: Int) =
        ResponsePaging.with(service.listNodeChildren(
            siteId,
            skipCount,
            maxItems,
            include = extraFieldsFolder(),
            relativePath = "documentLibrary"
        ))

    private fun extraFieldsFolder() =
        AlfrescoApi.csvQueryParam("isFavorite", "allowableOperations")

    suspend fun fetchEntry(entryId: String) =
        Entry.with(service.getNode(
            entryId,
            AlfrescoApi.csvQueryParam("path", "isFavorite", "allowableOperations")
        ).entry)

    suspend fun deleteEntry(entry: Entry) =
        service.deleteNode(entry.id, null)

    suspend fun uploadFile(parentId: String, file: File, name: String, contentType: String): Entry {
        val filePart = MultipartBody.Part.createFormData(
            "filedata",
            name,
            file.asRequestBody(contentType.toMediaTypeOrNull())
        )

        return Entry.with(
            service.createNode(
                parentId,
                filePart,
                autoRename = true,
                name = name,
                nodeType = "cm:content"
            ).entry
        )
    }

    fun contentUri(id: String): String {
        val baseUrl = SessionManager.currentSession?.baseUrl
        return "${baseUrl}alfresco/versions/1/nodes/$id/content?attachment=false&alf_ticket=${session.ticket}"
    }
}
