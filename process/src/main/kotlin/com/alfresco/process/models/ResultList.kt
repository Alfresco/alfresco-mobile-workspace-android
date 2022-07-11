package com.alfresco.process.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Query results
 * @property size
 * @property total
 * @property start
 * @property data
 */
@JsonClass(generateAdapter = true)
data class ResultList(
    @Json(name = "size") @field:Json(name = "size") var size: Int? = null,
    @Json(name = "total") @field:Json(name = "total") var total: Int? = null,
    @Json(name = "start") @field:Json(name = "start") var start: Int? = null,
    @Json(name = "data") @field:Json(name = "data") var data: List<TaskEntry>? = null
)
