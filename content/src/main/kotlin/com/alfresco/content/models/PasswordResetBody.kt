/**
 * NOTE: This class is auto generated by the Swagger Gradle Codegen for the following API: Alfresco Content Services REST API
 *
 * More info on this tool is available on https://github.com/Yelp/swagger-gradle-codegen
 */

package com.alfresco.content.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * @property password the new password
 * @property id the workflow id provided in the reset password email
 * @property key the workflow key provided in the reset password email
 */
@JsonClass(generateAdapter = true)
data class PasswordResetBody(
    @Json(name = "password") @field:Json(name = "password") var password: String,
    @Json(name = "id") @field:Json(name = "id") var id: String,
    @Json(name = "key") @field:Json(name = "key") var key: String
)
