/**
 * NOTE: This class is auto generated by the Swagger Gradle Codegen for the following API: Alfresco Content Services REST API
 *
 * More info on this tool is available on https://github.com/Yelp/swagger-gradle-codegen
 */

package com.alfresco.content.apis

import com.alfresco.content.models.ActionBodyExec
import com.alfresco.content.models.ActionDefinitionEntry
import com.alfresco.content.models.ActionDefinitionList
import com.alfresco.content.models.ActionExecResultEntry
import com.alfresco.content.tools.CSV
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

@JvmSuppressWildcards
interface ActionsApi {
    /**
     * Retrieve the details of an action definition
     * **Note:** this endpoint is available in Alfresco 5.2 and newer versions.  Retrieve the details of the action denoted by **actionDefinitionId**.
     * The endpoint is owned by defaultname service owner
     * @param actionDefinitionId The identifier of an action definition. (required)
     */
    @Headers(
        "Content-Type: application/json"
    )
    @GET("alfresco/versions/1/action-definitions/{actionDefinitionId}")
    suspend fun actionDetails(
        @retrofit2.http.Path("actionDefinitionId") actionDefinitionId: String
    ): ActionDefinitionEntry
    /**
     * Execute an action
     * **Note:** this endpoint is available in Alfresco 5.2 and newer versions.  Executes an action  An action may be executed against a node specified by **targetId**. For example:  ``` {   \"actionDefinitionId\": \"copy\",   \"targetId\": \"4c4b3c43-f18b-43ff-af84-751f16f1ddfd\",   \"params\": {     \"destination-folder\": \"34219f79-66fa-4ebf-b371-118598af898c\"   } } ```  Performing a POST with the request body shown above will result in the node identified by ```targetId``` being copied to the destination folder specified in the ```params``` object by the key ```destination-folder```.  **targetId** is optional, however, currently **targetId** must be a valid node ID. In the future, actions may be executed against different entity types or executed without the need for the context of an entity.   Parameters supplied to the action within the ```params``` object will be converted to the expected type, where possible using the DefaultTypeConverter class. In addition:  * Node IDs may be supplied in their short form (implicit workspace://SpacesStore prefix) * Aspect names may be supplied using their short form, e.g. cm:versionable or cm:auditable  In this example, we add the aspect ```cm:versionable``` to a node using the QName resolution mentioned above:  ``` {   \"actionDefinitionId\": \"add-features\",   \"targetId\": \"16349e3f-2977-44d1-93f2-73c12b8083b5\",   \"params\": {     \"aspect-name\": \"cm:versionable\"   } } ```  The ```actionDefinitionId``` is the ```id``` of an action definition as returned by the _list actions_ operations (e.g. GET /action-definitions).  The action will be executed **asynchronously** with a `202` HTTP response signifying that the request has been accepted successfully. The response body contains the unique ID of the action pending execution. The ID may be used, for example to correlate an execution with output in the server logs.
     * The endpoint is owned by defaultname service owner
     * @param actionBodyExec Action execution details (required)
     */
    @Headers(
        "Content-Type: application/json"
    )
    @POST("alfresco/versions/1/action-executions")
    suspend fun actionExec(
        @retrofit2.http.Body actionBodyExec: ActionBodyExec
    ): ActionExecResultEntry
    /**
     * Retrieve list of available actions
     * **Note:** this endpoint is available in Alfresco 5.2.2 and newer versions.  Gets a list of all available actions  The default sort order for the returned list is for actions to be sorted by ascending name. You can override the default by using the **orderBy** parameter.  You can use any of the following fields to order the results: * name * title
     * The endpoint is owned by defaultname service owner
     * @param skipCount The number of entities that exist in the collection before those included in this list. If not supplied then the default value is 0.  (optional, default to 0)
     * @param maxItems The maximum number of items to return in the list. If not supplied then the default value is 100.  (optional, default to 100)
     * @param orderBy A string to control the order of the entities returned in a list. You can use the **orderBy** parameter to sort the list by one or more fields.  Each field has a default sort order, which is normally ascending order. Read the API method implementation notes above to check if any fields used in this method have a descending default search order.  To sort the entities in a specific order, you can use the **ASC** and **DESC** keywords for any field.  (optional)
     * @param fields A list of field names.  You can use this parameter to restrict the fields returned within a response if, for example, you want to save on overall bandwidth.  The list applies to a returned individual entity or entries within a collection.  If the API method also supports the **include** parameter, then the fields specified in the **include** parameter are returned in addition to those specified in the **fields** parameter.  (optional)
     */
    @Headers(
        "Content-Type: application/json"
    )
    @GET("alfresco/versions/1/action-definitions")
    suspend fun listActions(
        @retrofit2.http.Query("skipCount") skipCount: Int? = null,
        @retrofit2.http.Query("maxItems") maxItems: Int? = null,
        @retrofit2.http.Query("orderBy") @CSV orderBy: List<String>? = null,
        @retrofit2.http.Query("fields") @CSV fields: List<String>? = null
    ): ActionDefinitionList
    /**
     * Retrieve actions for a node
     * **Note:** this endpoint is available in Alfresco 5.2 and newer versions.  Retrieve the list of actions that may be executed against the given **nodeId**.  The default sort order for the returned list is for actions to be sorted by ascending name. You can override the default by using the **orderBy** parameter.  You can use any of the following fields to order the results: * name * title
     * The endpoint is owned by defaultname service owner
     * @param nodeId The identifier of a node. (required)
     * @param skipCount The number of entities that exist in the collection before those included in this list. If not supplied then the default value is 0.  (optional, default to 0)
     * @param maxItems The maximum number of items to return in the list. If not supplied then the default value is 100.  (optional, default to 100)
     * @param orderBy A string to control the order of the entities returned in a list. You can use the **orderBy** parameter to sort the list by one or more fields.  Each field has a default sort order, which is normally ascending order. Read the API method implementation notes above to check if any fields used in this method have a descending default search order.  To sort the entities in a specific order, you can use the **ASC** and **DESC** keywords for any field.  (optional)
     * @param fields A list of field names.  You can use this parameter to restrict the fields returned within a response if, for example, you want to save on overall bandwidth.  The list applies to a returned individual entity or entries within a collection.  If the API method also supports the **include** parameter, then the fields specified in the **include** parameter are returned in addition to those specified in the **fields** parameter.  (optional)
     */
    @Headers(
        "Content-Type: application/json"
    )
    @GET("alfresco/versions/1/nodes/{nodeId}/action-definitions")
    suspend fun nodeActions(
        @retrofit2.http.Path("nodeId") nodeId: String,
        @retrofit2.http.Query("skipCount") skipCount: Int? = null,
        @retrofit2.http.Query("maxItems") maxItems: Int? = null,
        @retrofit2.http.Query("orderBy") @CSV orderBy: List<String>? = null,
        @retrofit2.http.Query("fields") @CSV fields: List<String>? = null
    ): ActionDefinitionList
}
