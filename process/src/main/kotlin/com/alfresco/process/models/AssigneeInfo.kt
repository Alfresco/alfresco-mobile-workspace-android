package com.alfresco.process.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * @property id
 * @property firstName
 * @property lastName
 * @property email
 */
@JsonClass(generateAdapter = true)
data class AssigneeInfo(
    @Json(name = "id") @field:Json(name = "id") var id: Int? = null,
    @Json(name = "firstName") @field:Json(name = "firstName") var firstName: String? = null,
    @Json(name = "lastName") @field:Json(name = "lastName") var lastName: String? = null,
    @Json(name = "email") @field:Json(name = "email") var email: String? = null
)
