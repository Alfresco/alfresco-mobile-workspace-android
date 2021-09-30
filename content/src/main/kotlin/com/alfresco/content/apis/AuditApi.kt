/**
 * NOTE: This class is auto generated by the Swagger Gradle Codegen for the following API: Alfresco Content Services REST API
 *
 * More info on this tool is available on https://github.com/Yelp/swagger-gradle-codegen
 */

package com.alfresco.content.apis

import com.alfresco.content.models.AuditApp
import com.alfresco.content.models.AuditAppPaging
import com.alfresco.content.models.AuditBodyUpdate
import com.alfresco.content.models.AuditEntryEntry
import com.alfresco.content.models.AuditEntryPaging
import com.alfresco.content.tools.CSV
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PUT

@JvmSuppressWildcards
interface AuditApi {
    /**
     * Permanently delete audit entries for an audit application
     * **Note:** this endpoint is available in Alfresco 5.2.2 and newer versions.  Permanently delete audit entries for an audit application **auditApplicationId**.  The **where** clause must be specified, either with an inclusive time period or for an inclusive range of ids. The delete is within the context of the given audit application.  For example:  *   ```where=(createdAt BETWEEN ('2017-06-02T12:13:51.593+01:00' , '2017-06-04T10:05:16.536+01:00')``` *   ```where=(id BETWEEN ('1234', '4321')```  You must have admin rights to delete audit information.
     * The endpoint is owned by defaultname service owner
     * @param auditApplicationId The identifier of an audit application. (required)
     * @param where Audit entries to permanently delete for an audit application, given an inclusive time period or range of ids. For example:  *   &#x60;&#x60;&#x60;where&#x3D;(createdAt BETWEEN (&#39;2017-06-02T12:13:51.593+01:00&#39; , &#39;2017-06-04T10:05:16.536+01:00&#39;)&#x60;&#x60;&#x60; *   &#x60;&#x60;&#x60;where&#x3D;(id BETWEEN (&#39;1234&#39;, &#39;4321&#39;)&#x60;&#x60;&#x60;  (required)
     */
    @Headers(
        "Content-Type: application/json"
    )
    @DELETE("alfresco/versions/1/audit-applications/{auditApplicationId}/audit-entries")
    suspend fun deleteAuditEntriesForAuditApp(
        @retrofit2.http.Path("auditApplicationId") auditApplicationId: String,
        @retrofit2.http.Query("where") where: String
    ): Unit
    /**
     * Permanently delete an audit entry
     * **Note:** this endpoint is available in Alfresco 5.2.2 and newer versions.  Permanently delete a single audit entry **auditEntryId**.  You must have admin rights to delete audit information.
     * The endpoint is owned by defaultname service owner
     * @param auditApplicationId The identifier of an audit application. (required)
     * @param auditEntryId The identifier of an audit entry. (required)
     */
    @Headers(
        "Content-Type: application/json"
    )
    @DELETE("alfresco/versions/1/audit-applications/{auditApplicationId}/audit-entries/{auditEntryId}")
    suspend fun deleteAuditEntry(
        @retrofit2.http.Path("auditApplicationId") auditApplicationId: String,
        @retrofit2.http.Path("auditEntryId") auditEntryId: String
    ): Unit
    /**
     * Get audit application info
     * **Note:** this endpoint is available in Alfresco 5.2.2 and newer versions.  Get status of an audit application **auditApplicationId**.  You must have admin rights to retrieve audit information.  You can use the **include** parameter to return the minimum and/or maximum audit record id for the application.
     * The endpoint is owned by defaultname service owner
     * @param auditApplicationId The identifier of an audit application. (required)
     * @param fields A list of field names.  You can use this parameter to restrict the fields returned within a response if, for example, you want to save on overall bandwidth.  The list applies to a returned individual entity or entries within a collection.  If the API method also supports the **include** parameter, then the fields specified in the **include** parameter are returned in addition to those specified in the **fields** parameter.  (optional)
     * @param include Also include the current minimum and/or maximum audit entry ids for the application. The following optional fields can be requested: * max * min  (optional)
     */
    @Headers(
        "Content-Type: application/json"
    )
    @GET("alfresco/versions/1/audit-applications/{auditApplicationId}")
    suspend fun getAuditApp(
        @retrofit2.http.Path("auditApplicationId") auditApplicationId: String,
        @retrofit2.http.Query("fields") @CSV fields: List<String>? = null,
        @retrofit2.http.Query("include") @CSV include: List<String>? = null
    ): AuditApp
    /**
     * Get audit entry
     * **Note:** this endpoint is available in Alfresco 5.2.2 and newer versions.  Gets audit entry **auditEntryId**.  You must have admin rights to access audit information.
     * The endpoint is owned by defaultname service owner
     * @param auditApplicationId The identifier of an audit application. (required)
     * @param auditEntryId The identifier of an audit entry. (required)
     * @param fields A list of field names.  You can use this parameter to restrict the fields returned within a response if, for example, you want to save on overall bandwidth.  The list applies to a returned individual entity or entries within a collection.  If the API method also supports the **include** parameter, then the fields specified in the **include** parameter are returned in addition to those specified in the **fields** parameter.  (optional)
     */
    @Headers(
        "Content-Type: application/json"
    )
    @GET("alfresco/versions/1/audit-applications/{auditApplicationId}/audit-entries/{auditEntryId}")
    suspend fun getAuditEntry(
        @retrofit2.http.Path("auditApplicationId") auditApplicationId: String,
        @retrofit2.http.Path("auditEntryId") auditEntryId: String,
        @retrofit2.http.Query("fields") @CSV fields: List<String>? = null
    ): AuditEntryEntry
    /**
     * List audit applications
     * **Note:** this endpoint is available in Alfresco 5.2.2 and newer versions.  Gets a list of audit applications in this repository.  This list may include pre-configured audit applications, if enabled, such as:  * alfresco-access * CMISChangeLog * Alfresco Tagging Service * Alfresco Sync Service (used by Enterprise Cloud Sync)  You must have admin rights to retrieve audit information.
     * The endpoint is owned by defaultname service owner
     * @param skipCount The number of entities that exist in the collection before those included in this list. If not supplied then the default value is 0.  (optional, default to 0)
     * @param maxItems The maximum number of items to return in the list. If not supplied then the default value is 100.  (optional, default to 100)
     * @param fields A list of field names.  You can use this parameter to restrict the fields returned within a response if, for example, you want to save on overall bandwidth.  The list applies to a returned individual entity or entries within a collection.  If the API method also supports the **include** parameter, then the fields specified in the **include** parameter are returned in addition to those specified in the **fields** parameter.  (optional)
     */
    @Headers(
        "Content-Type: application/json"
    )
    @GET("alfresco/versions/1/audit-applications")
    suspend fun listAuditApps(
        @retrofit2.http.Query("skipCount") skipCount: Int? = null,
        @retrofit2.http.Query("maxItems") maxItems: Int? = null,
        @retrofit2.http.Query("fields") @CSV fields: List<String>? = null
    ): AuditAppPaging
    /**
     * List audit entries for an audit application
     * **Note:** this endpoint is available in Alfresco 5.2.2 and newer versions.  Gets a list of audit entries for audit application **auditApplicationId**.  You can use the **include** parameter to return additional **values** information.  The list can be filtered by one or more of: * **createdByUser** person id * **createdAt** inclusive time period * **id** inclusive range of ids * **valuesKey** audit entry values contains the exact matching key * **valuesValue** audit entry values contains the exact matching value  The default sort order is **createdAt** ascending, but you can use an optional **ASC** or **DESC** modifier to specify an ascending or descending sort order.  For example, specifying ```orderBy=createdAt DESC``` returns audit entries in descending **createdAt** order.  You must have admin rights to retrieve audit information.
     * The endpoint is owned by defaultname service owner
     * @param auditApplicationId The identifier of an audit application. (required)
     * @param skipCount The number of entities that exist in the collection before those included in this list. If not supplied then the default value is 0.  (optional, default to 0)
     * @param omitTotalItems A boolean to control if the response provides the total numbers of items in the collection. If not supplied then the default value is false.  (optional, default to false)
     * @param orderBy A string to control the order of the entities returned in a list. You can use the **orderBy** parameter to sort the list by one or more fields.  Each field has a default sort order, which is normally ascending order. Read the API method implementation notes above to check if any fields used in this method have a descending default search order.  To sort the entities in a specific order, you can use the **ASC** and **DESC** keywords for any field.  (optional)
     * @param maxItems The maximum number of items to return in the list. If not supplied then the default value is 100.  (optional, default to 100)
     * @param where Optionally filter the list. Here are some examples:  *   &#x60;&#x60;&#x60;where&#x3D;(createdByUser&#x3D;&#39;jbloggs&#39;)&#x60;&#x60;&#x60;  *   &#x60;&#x60;&#x60;where&#x3D;(id BETWEEN (&#39;1234&#39;, &#39;4321&#39;)&#x60;&#x60;&#x60;  *   &#x60;&#x60;&#x60;where&#x3D;(createdAt BETWEEN (&#39;2017-06-02T12:13:51.593+01:00&#39; , &#39;2017-06-04T10:05:16.536+01:00&#39;)&#x60;&#x60;&#x60;  *   &#x60;&#x60;&#x60;where&#x3D;(createdByUser&#x3D;&#39;jbloggs&#39; and createdAt BETWEEN (&#39;2017-06-02T12:13:51.593+01:00&#39; , &#39;2017-06-04T10:05:16.536+01:00&#39;)&#x60;&#x60;&#x60;  *   &#x60;&#x60;&#x60;where&#x3D;(valuesKey&#x3D;&#39;/alfresco-access/login/user&#39;)&#x60;&#x60;&#x60;  *   &#x60;&#x60;&#x60;where&#x3D;(valuesKey&#x3D;&#39;/alfresco-access/transaction/action&#39; and valuesValue&#x3D;&#39;DELETE&#39;)&#x60;&#x60;&#x60;  (optional)
     * @param include Returns additional information about the audit entry. The following optional fields can be requested: * values  (optional)
     * @param fields A list of field names.  You can use this parameter to restrict the fields returned within a response if, for example, you want to save on overall bandwidth.  The list applies to a returned individual entity or entries within a collection.  If the API method also supports the **include** parameter, then the fields specified in the **include** parameter are returned in addition to those specified in the **fields** parameter.  (optional)
     */
    @Headers(
        "Content-Type: application/json"
    )
    @GET("alfresco/versions/1/audit-applications/{auditApplicationId}/audit-entries")
    suspend fun listAuditEntriesForAuditApp(
        @retrofit2.http.Path("auditApplicationId") auditApplicationId: String,
        @retrofit2.http.Query("skipCount") skipCount: Int? = null,
        @retrofit2.http.Query("omitTotalItems") omitTotalItems: Boolean? = null,
        @retrofit2.http.Query("orderBy") @CSV orderBy: List<String>? = null,
        @retrofit2.http.Query("maxItems") maxItems: Int? = null,
        @retrofit2.http.Query("where") where: String? = null,
        @retrofit2.http.Query("include") @CSV include: List<String>? = null,
        @retrofit2.http.Query("fields") @CSV fields: List<String>? = null
    ): AuditEntryPaging
    /**
     * List audit entries for a node
     * **Note:** this endpoint is available in Alfresco 5.2.2 and newer versions.  Gets a list of audit entries for node **nodeId**.  The list can be filtered by **createdByUser** and for a given inclusive time period.  The default sort order is **createdAt** ascending, but you can use an optional **ASC** or **DESC** modifier to specify an ascending or descending sort order.  For example, specifying ```orderBy=createdAt DESC``` returns audit entries in descending **createdAt** order.  This relies on the pre-configured 'alfresco-access' audit application.
     * The endpoint is owned by defaultname service owner
     * @param nodeId The identifier of a node. (required)
     * @param skipCount The number of entities that exist in the collection before those included in this list. If not supplied then the default value is 0.  (optional, default to 0)
     * @param orderBy A string to control the order of the entities returned in a list. You can use the **orderBy** parameter to sort the list by one or more fields.  Each field has a default sort order, which is normally ascending order. Read the API method implementation notes above to check if any fields used in this method have a descending default search order.  To sort the entities in a specific order, you can use the **ASC** and **DESC** keywords for any field.  (optional)
     * @param maxItems The maximum number of items to return in the list. If not supplied then the default value is 100.  (optional, default to 100)
     * @param where Optionally filter the list. Here are some examples:  *   &#x60;&#x60;&#x60;where&#x3D;(createdByUser&#x3D;&#39;-me-&#39;)&#x60;&#x60;&#x60;  *   &#x60;&#x60;&#x60;where&#x3D;(createdAt BETWEEN (&#39;2017-06-02T12:13:51.593+01:00&#39; , &#39;2017-06-04T10:05:16.536+01:00&#39;)&#x60;&#x60;&#x60;  *   &#x60;&#x60;&#x60;where&#x3D;(createdByUser&#x3D;&#39;jbloggs&#39; and createdAt BETWEEN (&#39;2017-06-02T12:13:51.593+01:00&#39; , &#39;2017-06-04T10:05:16.536+01:00&#39;)&#x60;&#x60;&#x60;  (optional)
     * @param include Returns additional information about the audit entry. The following optional fields can be requested: * values  (optional)
     * @param fields A list of field names.  You can use this parameter to restrict the fields returned within a response if, for example, you want to save on overall bandwidth.  The list applies to a returned individual entity or entries within a collection.  If the API method also supports the **include** parameter, then the fields specified in the **include** parameter are returned in addition to those specified in the **fields** parameter.  (optional)
     */
    @Headers(
        "Content-Type: application/json"
    )
    @GET("alfresco/versions/1/nodes/{nodeId}/audit-entries")
    suspend fun listAuditEntriesForNode(
        @retrofit2.http.Path("nodeId") nodeId: String,
        @retrofit2.http.Query("skipCount") skipCount: Int? = null,
        @retrofit2.http.Query("orderBy") @CSV orderBy: List<String>? = null,
        @retrofit2.http.Query("maxItems") maxItems: Int? = null,
        @retrofit2.http.Query("where") where: String? = null,
        @retrofit2.http.Query("include") @CSV include: List<String>? = null,
        @retrofit2.http.Query("fields") @CSV fields: List<String>? = null
    ): AuditEntryPaging
    /**
     * Update audit application info
     * **Note:** this endpoint is available in Alfresco 5.2.2 and newer versions.  Disable or re-enable the audit application **auditApplicationId**.  New audit entries will not be created for a disabled audit application until it is re-enabled (and system-wide auditing is also enabled).  Note, it is still possible to query &/or delete any existing audit entries even if auditing is disabled for the audit application.  You must have admin rights to update audit application.
     * The endpoint is owned by defaultname service owner
     * @param auditApplicationId The identifier of an audit application. (required)
     * @param auditAppBodyUpdate The audit application to update. (required)
     * @param fields A list of field names.  You can use this parameter to restrict the fields returned within a response if, for example, you want to save on overall bandwidth.  The list applies to a returned individual entity or entries within a collection.  If the API method also supports the **include** parameter, then the fields specified in the **include** parameter are returned in addition to those specified in the **fields** parameter.  (optional)
     */
    @Headers(
        "Content-Type: application/json"
    )
    @PUT("alfresco/versions/1/audit-applications/{auditApplicationId}")
    suspend fun updateAuditApp(
        @retrofit2.http.Path("auditApplicationId") auditApplicationId: String,
        @retrofit2.http.Body auditAppBodyUpdate: AuditBodyUpdate,
        @retrofit2.http.Query("fields") @CSV fields: List<String>? = null
    ): AuditApp
}
