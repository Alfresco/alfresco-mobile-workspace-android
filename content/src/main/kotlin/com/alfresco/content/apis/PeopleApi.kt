/**
 * NOTE: This class is auto generated by the Swagger Gradle Codegen for the following API: Alfresco Content Services REST API
 *
 * More info on this tool is available on https://github.com/Yelp/swagger-gradle-codegen
 */

package com.alfresco.content.apis

import com.alfresco.content.models.ClientBody
import com.alfresco.content.models.PasswordResetBody
import com.alfresco.content.models.PersonBodyCreate
import com.alfresco.content.models.PersonBodyUpdate
import com.alfresco.content.models.PersonEntry
import com.alfresco.content.models.PersonPaging
import com.alfresco.content.tools.CSV
import java.time.ZonedDateTime
import okhttp3.ResponseBody
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT

@JvmSuppressWildcards
interface PeopleApi {
    /**
     * Create person
     * **Note:** this endpoint is available in Alfresco 5.2 and newer versions.  Create a person.  If applicable, the given person's login access can also be optionally disabled.  You must have admin rights to create a person.  You can set custom properties when you create a person: ```JSON {   \"id\": \"abeecher\",   \"firstName\": \"Alice\",   \"lastName\": \"Beecher\",   \"displayName\": \"Alice Beecher\",   \"email\": \"abeecher@example.com\",   \"password\": \"secret\",   \"properties\":   {     \"my:property\": \"The value\"   } } ``` **Note:** setting properties of type d:content and d:category are not supported.
     * The endpoint is owned by defaultname service owner
     * @param personBodyCreate The person details. (required)
     * @param fields A list of field names.  You can use this parameter to restrict the fields returned within a response if, for example, you want to save on overall bandwidth.  The list applies to a returned individual entity or entries within a collection.  If the API method also supports the **include** parameter, then the fields specified in the **include** parameter are returned in addition to those specified in the **fields** parameter.  (optional)
     */
    @Headers(
        "Content-Type: application/json"
    )
    @POST("alfresco/versions/1/people")
    suspend fun createPerson(
        @retrofit2.http.Body personBodyCreate: PersonBodyCreate,
        @retrofit2.http.Query("fields") @CSV fields: List<String>? = null
    ): PersonEntry
    /**
     * Delete avatar image
     * **Note:** this endpoint is available in Alfresco 5.2.2 and newer versions.  Deletes the avatar image related to person **personId**.  You must be the person or have admin rights to update a person's avatar.  You can use the `-me-` string in place of `<personId>` to specify the currently authenticated user.
     * The endpoint is owned by defaultname service owner
     * @param personId The identifier of a person. (required)
     */
    @Headers(
        "Content-Type: application/json"
    )
    @DELETE("alfresco/versions/1/people/{personId}/avatar")
    suspend fun deleteAvatarImage(
        @retrofit2.http.Path("personId") personId: String
    ): Unit
    /**
     * Get avatar image
     * **Note:** this endpoint is available in Alfresco 5.2.2 and newer versions.  Gets the avatar image related to the person **personId**. If the person has no related avatar then the **placeholder** query parameter can be optionally used to request a placeholder image to be returned.  You can use the `-me-` string in place of `<personId>` to specify the currently authenticated user.
     * The endpoint is owned by defaultname service owner
     * @param personId The identifier of a person. (required)
     * @param attachment **true** enables a web browser to download the file as an attachment. **false** means a web browser may preview the file in a new tab or window, but not download the file.  You can only set this parameter to **false** if the content type of the file is in the supported list; for example, certain image files and PDF files.  If the content type is not supported for preview, then a value of **false**  is ignored, and the attachment will be returned in the response.  (optional, default to true)
     * @param ifModifiedSince Only returns the content if it has been modified since the date provided. Use the date format defined by HTTP. For example, &#x60;Wed, 09 Mar 2016 16:56:34 GMT&#x60;.  (optional)
     * @param placeholder If **true** and there is no avatar for this **personId** then the placeholder image is returned, rather than a 404 response.  (optional, default to true)
     */
    @Headers(
        "Content-Type: application/json"
    )
    @GET("alfresco/versions/1/people/{personId}/avatar")
    suspend fun getAvatarImage(
        @retrofit2.http.Path("personId") personId: String,
        @retrofit2.http.Query("attachment") attachment: Boolean? = null,
        @retrofit2.http.Header("If-Modified-Since") ifModifiedSince: ZonedDateTime?,
        @retrofit2.http.Query("placeholder") placeholder: Boolean? = null
    ): ResponseBody
    /**
     * Get a person
     * Gets information for the person **personId**.  You can use the `-me-` string in place of `<personId>` to specify the currently authenticated user.
     * The endpoint is owned by defaultname service owner
     * @param personId The identifier of a person. (required)
     * @param fields A list of field names.  You can use this parameter to restrict the fields returned within a response if, for example, you want to save on overall bandwidth.  The list applies to a returned individual entity or entries within a collection.  If the API method also supports the **include** parameter, then the fields specified in the **include** parameter are returned in addition to those specified in the **fields** parameter.  (optional)
     */
    @Headers(
        "Content-Type: application/json"
    )
    @GET("alfresco/versions/1/people/{personId}")
    suspend fun getPerson(
        @retrofit2.http.Path("personId") personId: String,
        @retrofit2.http.Query("fields") @CSV fields: List<String>? = null
    ): PersonEntry
    /**
     * List people
     * **Note:** this endpoint is available in Alfresco 5.2 and newer versions.  List people.  You can use the **include** parameter to return any additional information.  The default sort order for the returned list is for people to be sorted by ascending id. You can override the default by using the **orderBy** parameter.  You can use any of the following fields to order the results: * id * firstName * lastName
     * The endpoint is owned by defaultname service owner
     * @param skipCount The number of entities that exist in the collection before those included in this list. If not supplied then the default value is 0.  (optional, default to 0)
     * @param maxItems The maximum number of items to return in the list. If not supplied then the default value is 100.  (optional, default to 100)
     * @param orderBy A string to control the order of the entities returned in a list. You can use the **orderBy** parameter to sort the list by one or more fields.  Each field has a default sort order, which is normally ascending order. Read the API method implementation notes above to check if any fields used in this method have a descending default search order.  To sort the entities in a specific order, you can use the **ASC** and **DESC** keywords for any field.  (optional)
     * @param include Returns additional information about the person. The following optional fields can be requested: * properties * aspectNames * capabilities  (optional)
     * @param fields A list of field names.  You can use this parameter to restrict the fields returned within a response if, for example, you want to save on overall bandwidth.  The list applies to a returned individual entity or entries within a collection.  If the API method also supports the **include** parameter, then the fields specified in the **include** parameter are returned in addition to those specified in the **fields** parameter.  (optional)
     */
    @Headers(
        "Content-Type: application/json"
    )
    @GET("alfresco/versions/1/people")
    suspend fun listPeople(
        @retrofit2.http.Query("skipCount") skipCount: Int? = null,
        @retrofit2.http.Query("maxItems") maxItems: Int? = null,
        @retrofit2.http.Query("orderBy") @CSV orderBy: List<String>? = null,
        @retrofit2.http.Query("include") @CSV include: List<String>? = null,
        @retrofit2.http.Query("fields") @CSV fields: List<String>? = null
    ): PersonPaging
    /**
     * Request password reset
     * **Note:** this endpoint is available in Alfresco 5.2.1 and newer versions.  Initiates the reset password workflow to send an email with reset password instruction to the user's registered email.  The client is mandatory in the request body. For example: ```JSON {   \"client\": \"myClient\" } ``` **Note:** The client must be registered before this API can send an email. See [server documentation]. However, out-of-the-box share is registered as a default client, so you could pass **share** as the client name: ```JSON {   \"client\": \"share\" } ``` **Note:** No authentication is required to call this endpoint.
     * The endpoint is owned by defaultname service owner
     * @param personId The identifier of a person. (required)
     * @param clientBody The client name to send email with app-specific url. (required)
     */
    @Headers(
        "Content-Type: application/json"
    )
    @POST("alfresco/versions/1/people/{personId}/request-password-reset")
    suspend fun requestPasswordReset(
        @retrofit2.http.Path("personId") personId: String,
        @retrofit2.http.Body clientBody: ClientBody
    ): Unit
    /**
     * Reset password
     * **Note:** this endpoint is available in Alfresco 5.2.1 and newer versions.  Resets user's password  The password, id and key properties are mandatory in the request body. For example: ```JSON {   \"password\":\"newPassword\",   \"id\":\"activiti$10\",   \"key\":\"4dad6d00-0daf-413a-b200-f64af4e12345\" } ``` **Note:** No authentication is required to call this endpoint.
     * The endpoint is owned by defaultname service owner
     * @param personId The identifier of a person. (required)
     * @param passwordResetBody The reset password details (required)
     */
    @Headers(
        "Content-Type: application/json"
    )
    @POST("alfresco/versions/1/people/{personId}/reset-password")
    suspend fun resetPassword(
        @retrofit2.http.Path("personId") personId: String,
        @retrofit2.http.Body passwordResetBody: PasswordResetBody
    ): Unit
    /**
     * Update avatar image
     * **Note:** this endpoint is available in Alfresco 5.2.2 and newer versions.  Updates the avatar image related to the person **personId**.  The request body should be the binary stream for the avatar image. The content type of the file should be an image file. This will be used to generate an \"avatar\" thumbnail rendition.  You must be the person or have admin rights to update a person's avatar.  You can use the `-me-` string in place of `<personId>` to specify the currently authenticated user.
     * The endpoint is owned by defaultname service owner
     * @param personId The identifier of a person. (required)
     * @param contentBodyUpdate The binary content (required)
     */
    @Headers(
        "Content-Type: application/octet-stream"
    )
    @PUT("alfresco/versions/1/people/{personId}/avatar")
    suspend fun updateAvatarImage(
        @retrofit2.http.Path("personId") personId: String,
        @retrofit2.http.Body contentBodyUpdate: List<Byte>
    ): Unit
    /**
     * Update person
     * **Note:** this endpoint is available in Alfresco 5.2 and newer versions.  Update the given person's details.  You can use the `-me-` string in place of `<personId>` to specify the currently authenticated user.  If applicable, the given person's login access can also be optionally disabled or re-enabled.  You must have admin rights to update a person — unless updating your own details.  If you are changing your password, as a non-admin user, then the existing password must also be supplied (using the oldPassword field in addition to the new password value).  Admin users cannot be disabled by setting enabled to false.  Non-admin users may not disable themselves.  You can set custom properties when you update a person: ```JSON {   \"firstName\": \"Alice\",   \"properties\":   {     \"my:property\": \"The value\"   } } ``` **Note:** setting properties of type d:content and d:category are not supported.
     * The endpoint is owned by defaultname service owner
     * @param personId The identifier of a person. (required)
     * @param personBodyUpdate The person details. (required)
     * @param fields A list of field names.  You can use this parameter to restrict the fields returned within a response if, for example, you want to save on overall bandwidth.  The list applies to a returned individual entity or entries within a collection.  If the API method also supports the **include** parameter, then the fields specified in the **include** parameter are returned in addition to those specified in the **fields** parameter.  (optional)
     */
    @Headers(
        "Content-Type: application/json"
    )
    @PUT("alfresco/versions/1/people/{personId}")
    suspend fun updatePerson(
        @retrofit2.http.Path("personId") personId: String,
        @retrofit2.http.Body personBodyUpdate: PersonBodyUpdate,
        @retrofit2.http.Query("fields") @CSV fields: List<String>? = null
    ): PersonEntry
}
