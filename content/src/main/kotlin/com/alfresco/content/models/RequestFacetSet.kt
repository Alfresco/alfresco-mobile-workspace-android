/**
 * NOTE: This class is auto generated by the Swagger Gradle Codegen for the following API: Alfresco Content Services REST API
 *
 * More info on this tool is available on https://github.com/Yelp/swagger-gradle-codegen
 */

package com.alfresco.content.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * The interval to Set
 * @property label A label to use to identify the set
 * @property start The start of the range
 * @property end The end of the range
 * @property startInclusive When true, the set will include values greater or equal to \&quot;start\&quot;
 * @property endInclusive When true, the set will include values less than or equal to \&quot;end\&quot;
 */
@JsonClass(generateAdapter = true)
data class RequestFacetSet(
    @Json(name = "label") @field:Json(name = "label") var label: String? = null,
    @Json(name = "start") @field:Json(name = "start") var start: String? = null,
    @Json(name = "end") @field:Json(name = "end") var end: String? = null,
    @Json(name = "startInclusive") @field:Json(name = "startInclusive") var startInclusive: Boolean? = null,
    @Json(name = "endInclusive") @field:Json(name = "endInclusive") var endInclusive: Boolean? = null
)
