package com.alfresco.content.apis

import com.alfresco.content.models.NodeEntry
import com.alfresco.content.tools.CSV
import okhttp3.RequestBody
import retrofit2.http.POST

interface NodesApiExt {

    /**
     * Multipart definition for [NodesApi.createNode].
     */
    @retrofit2.http.Multipart
    @POST("alfresco/versions/1/nodes/{nodeId}/children")
    suspend fun createNode(
        @retrofit2.http.Path("nodeId") nodeId: String,
        @retrofit2.http.Part("filedata\"; filename=\"filedata") fileData: RequestBody,
        @retrofit2.http.Part("name") name: String?,
        @retrofit2.http.Part("nodeType") nodeType: String? = "cm:content",
        @retrofit2.http.PartMap properties: Map<String, @JvmSuppressWildcards RequestBody>? = null,
        @retrofit2.http.Part("overwrite") overwrite: Boolean? = null,
        @retrofit2.http.Part("comment") comment: String? = null,
        @retrofit2.http.Part("relativepath") relativePath: String? = null,
        @retrofit2.http.Part("renditions") renditions: String? = null,
        @retrofit2.http.Part("autoRename") autoRename: Boolean? = null,
        @retrofit2.http.Part("majorVersion") majorVersion: Boolean? = null,
        @retrofit2.http.Part("versioningEnabled") versioningEnabled: Boolean? = null,
        @retrofit2.http.Part("include") @CSV include: List<String>? = null,
        @retrofit2.http.Part("fields") @CSV fields: List<String>? = null
    ): NodeEntry
}
