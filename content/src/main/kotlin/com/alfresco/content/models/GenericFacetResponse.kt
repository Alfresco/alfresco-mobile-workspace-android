/**
 * NOTE: This class is auto generated by the Swagger Gradle Codegen for the following API: Alfresco Content Services REST API
 *
 * More info on this tool is available on https://github.com/Yelp/swagger-gradle-codegen
 */

package com.alfresco.content.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * @property type The facet type, eg. interval, range, pivot, stats
 * @property label The field name or its explicit label, if provided on the request
 * @property buckets An array of buckets and values
 */
@JsonClass(generateAdapter = true)
data class GenericFacetResponse(
    @Json(name = "type") @field:Json(name = "type") var type: String? = null,
    @Json(name = "label") @field:Json(name = "label") var label: String? = null,
    @Json(name = "buckets") @field:Json(name = "buckets") var buckets: List<GenericBucket>? = null
)
