/**
 * NOTE: This class is auto generated by the Swagger Gradle Codegen for the following API: Alfresco Content Services REST API
 *
 * More info on this tool is available on https://github.com/Yelp/swagger-gradle-codegen
 */

package com.alfresco.content.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * @property entry
 */
@JsonClass(generateAdapter = true)
data class ActionDefinitionEntry(
    @Json(name = "entry") @field:Json(name = "entry") var entry: ActionDefinition
)
