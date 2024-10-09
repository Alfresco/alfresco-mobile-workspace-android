package com.alfresco.content.data

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.alfresco.content.apis.AlfrescoApi
import com.alfresco.content.apis.NodesApi
import com.alfresco.content.apis.NodesApiExt
import com.alfresco.content.apis.getMyNode
import com.alfresco.content.models.NodeBodyCreate
import com.alfresco.content.models.NodeBodyMove
import com.alfresco.content.models.NodeBodyUpdate
import com.alfresco.content.session.ActionSessionInvalid
import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager
import com.alfresco.content.session.SessionNotFoundException
import com.alfresco.events.EventBus
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

/**
 * Mark as BrowseRepository
 */
class BrowseRepository(otherSession: Session? = null) {
    lateinit var session: Session
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    init {
        try {
            session = otherSession ?: SessionManager.requireSession
        } catch (e: SessionNotFoundException) {
            e.printStackTrace()
            coroutineScope.launch {
                EventBus.default.send(ActionSessionInvalid(true))
            }
        }
    }

    private val context get() = session.context

    private val service: NodesApi by lazy {
        session.createService(NodesApi::class.java)
    }

    private val serviceExt: NodesApiExt by lazy {
        session.createService(NodesApiExt::class.java)
    }

    private val sharedPref: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    val myFilesNodeId: String get() = session.account.myFiles ?: ""

    suspend fun myFilesNodeId() = service.getMyNode().entry.id

    suspend fun fetchFolderItems(
        folderId: String,
        skipCount: Int,
        maxItems: Int,
    ) = ResponsePaging.with(
        service.listNodeChildren(
            folderId,
            skipCount,
            maxItems,
            include = extraFields(),
            includeSource = true,
        ),
    )

    /**
     * fetching the folder items
     */
    suspend fun fetchExtensionFolderItems(
        folderId: String,
        skipCount: Int,
        maxItems: Int,
    ) = ResponsePaging.withExtension(
        service.listNodeChildren(
            folderId,
            skipCount,
            maxItems,
            include = extraFields(),
            includeSource = true,
        ),
    )

    suspend fun fetchLibraryDocumentsFolder(siteId: String) =
        Entry.with(
            service.getNode(
                siteId,
                extraFields(),
                LIB_DOCUMENTS_PATH,
            ).entry,
        )

    suspend fun fetchLibraryItems(
        siteId: String,
        skipCount: Int,
        maxItems: Int,
    ) = ResponsePaging.with(
        service.listNodeChildren(
            siteId,
            skipCount,
            maxItems,
            include = extraFields(),
            relativePath = LIB_DOCUMENTS_PATH,
        ),
    )

    private fun extraFields() = AlfrescoApi.csvQueryParam("path", "isFavorite", "allowableOperations", "properties")

    suspend fun fetchEntry(entryId: String) =
        Entry.with(
            service.getNode(
                entryId,
                extraFields(),
            ).entry,
        )

    suspend fun deleteEntry(entry: Entry) {
        service.deleteNode(entry.id, null)
    }

    suspend fun createEntry(
        local: Entry,
        file: File,
    ): Entry {
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
                include = extraFields(),
            ).entry,
        )
    }

    suspend fun createFolder(
        name: String,
        description: String,
        parentId: String?,
        autoRename: Boolean = true,
    ): Entry {
        val nodeBodyCreate =
            NodeBodyCreate(
                name = name,
                nodeType = "cm:folder",
                properties = mapOf("cm:title" to name, "cm:description" to description),
            )

        val response =
            service.createNode(
                nodeId = requireNotNull(parentId),
                nodeBodyCreate = nodeBodyCreate,
                autoRename = autoRename,
            )

        return Entry.with(response.entry)
    }

    /**
     * update the file and folders data by calling update node api
     */
    suspend fun updateFileFolder(
        name: String,
        description: String,
        nodeId: String?,
        nodeType: String,
    ): Entry {
        val nodeBodyUpdate =
            NodeBodyUpdate(
                name = name,
                nodeType = nodeType,
                properties = mapOf("cm:title" to name, "cm:description" to description),
            )

        return Entry.with(
            service.updateNode(
                nodeId = requireNotNull(nodeId),
                nodeBodyUpdate = nodeBodyUpdate,
            ).entry,
        )
    }

    /**
     * executing api for moving the items (file or folder)
     */
    suspend fun moveNode(
        entryId: String,
        targetParentId: String,
    ): Entry {
        return Entry.with(service.moveNode(entryId, NodeBodyMove(targetParentId)).entry)
    }

    fun contentUri(entry: Entry): String {
        val baseUrl = SessionManager.currentSession?.baseUrl
        return "${baseUrl}alfresco/versions/1/nodes/${entry.id}/content?attachment=false&alf_ticket=${session.ticket}"
    }

    /**
     * returns the shared files into preferences
     */
    fun getExtensionDataList(): List<String> {
        if (sharedPref.contains(SHARE_MULTIPLE_URI) &&
            !sharedPref.getString(SHARE_MULTIPLE_URI, "").isNullOrEmpty()
        ) {
            return Gson().fromJson(sharedPref.getString(SHARE_MULTIPLE_URI, ""), Array<String>::class.java).toList()
        }
        return emptyList()
    }

    /**
     * clear the shared files from preferences
     */
    fun clearExtensionData() {
        val editor = sharedPref.edit()
        editor.remove(SHARE_MULTIPLE_URI)
        editor.apply()
    }

    companion object {
        const val LIB_DOCUMENTS_PATH = "documentLibrary"
        const val SHARE_MULTIPLE_URI = "SHARE_MULTIPLE_URI"
    }
}
