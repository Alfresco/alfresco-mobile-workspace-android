/**
 * NOTE: This class is auto generated by the Swagger Gradle Codegen for the following API: Alfresco Content Services REST API
 *
 * More info on this tool is available on https://github.com/Yelp/swagger-gradle-codegen
 */

package com.alfresco.content.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * @property list
 */
@JsonClass(generateAdapter = true)
data class SiteMemberPaging(
    @Json(name = "list") @field:Json(name = "list") var list: SiteMemberPagingList
)
