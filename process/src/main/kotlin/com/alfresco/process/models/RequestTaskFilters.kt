package com.alfresco.process.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RequestTaskFilters(
    @Json(name = "appDefinitionId") @field:Json(name = "appDefinitionId") var appDefinitionId: String? = null,
    @Json(name = "assignment") @field:Json(name = "assignment") var assignment: String? = null,
    @Json(name = "page") @field:Json(name = "page") var page: String? = null,
    @Json(name = "processDefinitionId") @field:Json(name = "processDefinitionId") var processDefinitionId: String? = null,
    @Json(name = "processInstanceId") @field:Json(name = "processInstanceId") var processInstanceId: String? = null,
    @Json(name = "size") @field:Json(name = "size") var size: String? = null,
    @Json(name = "sort") @field:Json(name = "sort") var sort: String? = null,
    @Json(name = "start") @field:Json(name = "sort") var start: Int? = null,
    @Json(name = "state") @field:Json(name = "state") var state: String? = null,
    @Json(name = "text") @field:Json(name = "text") var text: String? = null
)
