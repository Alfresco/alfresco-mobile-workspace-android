package com.alfresco.content.apis

import com.alfresco.content.models.NodeEntry
import com.alfresco.content.tools.CSV
import retrofit2.http.Headers
import retrofit2.http.POST

interface TrashcanApiExt {

    /**
     * Overload of [restoreDeletedNode] without [retrofit2.http.Body].
     * Ref: https://github.com/square/retrofit/issues/1488
     */
    @Headers(
        "Content-Type: application/json"
    )
    @POST("alfresco/versions/1/deleted-nodes/{nodeId}/restore")
    suspend fun restoreDeletedNode(
        @retrofit2.http.Path("nodeId") nodeId: String,
        @retrofit2.http.Query("fields") @CSV fields: List<String>? = null
    ): NodeEntry
}
