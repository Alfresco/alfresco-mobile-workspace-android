package com.alfresco.content.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * A value used in faceting
 * @property count The value count
 */
@JsonClass(generateAdapter = true)
data class GenericValue(
    @Json(name = "count") @field:Json(name = "count") var count: String? = null
)
