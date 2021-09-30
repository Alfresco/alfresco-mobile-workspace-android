/**
 * NOTE: This class is auto generated by the Swagger Gradle Codegen for the following API: Alfresco Content Services REST API
 *
 * More info on this tool is available on https://github.com/Yelp/swagger-gradle-codegen
 */

package com.alfresco.content.apis

import com.alfresco.content.models.PreferenceEntry
import com.alfresco.content.models.PreferencePaging
import com.alfresco.content.tools.CSV
import retrofit2.http.GET
import retrofit2.http.Headers

@JvmSuppressWildcards
interface PreferencesApi {
    /**
     * Get a preference
     * Gets a specific preference for person **personId**.  You can use the `-me-` string in place of `<personId>` to specify the currently authenticated user.
     * The endpoint is owned by defaultname service owner
     * @param personId The identifier of a person. (required)
     * @param preferenceName The name of the preference. (required)
     * @param fields A list of field names.  You can use this parameter to restrict the fields returned within a response if, for example, you want to save on overall bandwidth.  The list applies to a returned individual entity or entries within a collection.  If the API method also supports the **include** parameter, then the fields specified in the **include** parameter are returned in addition to those specified in the **fields** parameter.  (optional)
     */
    @Headers(
        "Content-Type: application/json"
    )
    @GET("alfresco/versions/1/people/{personId}/preferences/{preferenceName}")
    suspend fun getPreference(
        @retrofit2.http.Path("personId") personId: String,
        @retrofit2.http.Path("preferenceName") preferenceName: String,
        @retrofit2.http.Query("fields") @CSV fields: List<String>? = null
    ): PreferenceEntry
    /**
     * List preferences
     * Gets a list of preferences for person **personId**.  You can use the `-me-` string in place of `<personId>` to specify the currently authenticated user. Note that each preference consists of an **id** and a **value**.  The **value** can be of any JSON type.
     * The endpoint is owned by defaultname service owner
     * @param personId The identifier of a person. (required)
     * @param skipCount The number of entities that exist in the collection before those included in this list. If not supplied then the default value is 0.  (optional, default to 0)
     * @param maxItems The maximum number of items to return in the list. If not supplied then the default value is 100.  (optional, default to 100)
     * @param fields A list of field names.  You can use this parameter to restrict the fields returned within a response if, for example, you want to save on overall bandwidth.  The list applies to a returned individual entity or entries within a collection.  If the API method also supports the **include** parameter, then the fields specified in the **include** parameter are returned in addition to those specified in the **fields** parameter.  (optional)
     */
    @Headers(
        "Content-Type: application/json"
    )
    @GET("alfresco/versions/1/people/{personId}/preferences")
    suspend fun listPreferences(
        @retrofit2.http.Path("personId") personId: String,
        @retrofit2.http.Query("skipCount") skipCount: Int? = null,
        @retrofit2.http.Query("maxItems") maxItems: Int? = null,
        @retrofit2.http.Query("fields") @CSV fields: List<String>? = null
    ): PreferencePaging
}
