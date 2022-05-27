/**
 * NOTE: This class is auto generated by the Swagger Gradle Codegen for the following API: Alfresco Content Services REST API
 *
 * More info on this tool is available on https://github.com/Yelp/swagger-gradle-codegen
 */

package com.alfresco.content.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * @property name The name must not contain spaces or the following special characters: * \&quot; &lt; &gt; \\ / ? : and |. The character . must not be used at the end of the name.
 * @property nodeType
 * @property aspectNames
 * @property properties
 * @property permissions
 * @property definition
 * @property relativePath
 * @property association
 * @property secondaryChildren
 * @property targets
 */
@JsonClass(generateAdapter = true)
data class NodeBodyCreate(
    @Json(name = "name") @field:Json(name = "name") var name: String,
    @Json(name = "nodeType") @field:Json(name = "nodeType") var nodeType: String,
    @Json(name = "aspectNames") @field:Json(name = "aspectNames") var aspectNames: List<String>? = null,
    @Json(name = "properties") @field:Json(name = "properties") var properties: Map<String, Any?>? = null,
    @Json(name = "permissions") @field:Json(name = "permissions") var permissions: PermissionsBody? = null,
    @Json(name = "definition") @field:Json(name = "definition") var definition: Definition? = null,
    @Json(name = "relativePath") @field:Json(name = "relativePath") var relativePath: String? = null,
    @Json(name = "association") @field:Json(name = "association") var association: NodeBodyCreateAssociation? = null,
    @Json(name = "secondaryChildren") @field:Json(name = "secondaryChildren") var secondaryChildren: List<ChildAssociationBody>? = null,
    @Json(name = "targets") @field:Json(name = "targets") var targets: List<AssociationBody>? = null
)
