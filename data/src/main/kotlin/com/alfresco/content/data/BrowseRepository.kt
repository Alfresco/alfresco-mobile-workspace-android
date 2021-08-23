package com.alfresco.content.data

import com.alfresco.content.apis.AlfrescoApi
import com.alfresco.content.apis.NodesApi
import com.alfresco.content.apis.NodesApiExt
import com.alfresco.content.apis.getMyNode
import com.alfresco.content.models.NodeBodyCreate
import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager
import java.io.File
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class BrowseRepository(val session: Session = SessionManager.requireSession) {

    private val service: NodesApi by lazy {
        session.createService(NodesApi::class.java)
    }

    private val serviceExt: NodesApiExt by lazy {
        session.createService(NodesApiExt::class.java)
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

    suspend fun fetchLibraryDocumentsFolder(siteId: String) =
        Entry.with(service.getNode(
            siteId,
            extraFields(),
            LIB_DOCUMENTS_PATH
        ).entry)

    suspend fun fetchLibraryItems(siteId: String, skipCount: Int, maxItems: Int) =
        ResponsePaging.with(service.listNodeChildren(
            siteId,
            skipCount,
            maxItems,
            include = extraFields(),
            relativePath = LIB_DOCUMENTS_PATH
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

    suspend fun createEntry(local: Entry, file: File): Entry {
        // TODO: Support creating empty entries and folders
        requireNotNull(local.parentId)
        requireNotNull(local.mimeType)

        val filePart = file.asRequestBody(local.mimeType.toMediaTypeOrNull())
        val properties = mutableMapOf<String, RequestBody>()
        for ((k, v) in local.properties) {
            if (v.isNotEmpty()) {
                properties[k] = v.toRequestBody(MultipartBody.FORM)
            }
        }

        return Entry.with(
            serviceExt.createNode(
                local.parentId,
                filePart,
                local.name,
                "cm:content",
                properties,
                autoRename = true,
                include = extraFields()
            ).entry
        )
    }

    suspend fun createFolder(name: String, description: String, parentId: String?) {
        val list: HashMap<String, String> = HashMap()

        list["cm:title"] = name
        list["cm:description"] = description

        val nodeBodyCreate = NodeBodyCreate(
            name = name,
            nodeType = "cm:folder",
            properties = list
        )

        service.createNode(
            nodeId = requireNotNull(parentId),
            nodeBodyCreate = nodeBodyCreate,
            autoRename = true
        )
    }

    fun contentUri(entry: Entry): String {
        val baseUrl = SessionManager.currentSession?.baseUrl
        return "${baseUrl}alfresco/versions/1/nodes/${entry.id}/content?attachment=false&alf_ticket=${session.ticket}"
    }

    private companion object {
        const val LIB_DOCUMENTS_PATH = "documentLibrary"
    }
}
