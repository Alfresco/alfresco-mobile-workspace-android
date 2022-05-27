/**
 * NOTE: This class is auto generated by the Swagger Gradle Codegen for the following API: Alfresco Content Services REST API
 *
 * More info on this tool is available on https://github.com/Yelp/swagger-gradle-codegen
 */

package com.alfresco.content.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * @property errorKey
 * @property statusCode
 * @property briefSummary
 * @property stackTrace
 * @property descriptionURL
 * @property logId
 */
@JsonClass(generateAdapter = true)
data class ErrorError(
    @Json(name = "statusCode") @field:Json(name = "statusCode") var statusCode: Int,
    @Json(name = "briefSummary") @field:Json(name = "briefSummary") var briefSummary: String,
    @Json(name = "stackTrace") @field:Json(name = "stackTrace") var stackTrace: String,
    @Json(name = "descriptionURL") @field:Json(name = "descriptionURL") var descriptionURL: String,
    @Json(name = "errorKey") @field:Json(name = "errorKey") var errorKey: String? = null,
    @Json(name = "logId") @field:Json(name = "logId") var logId: String? = null
)
