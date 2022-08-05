package com.alfresco.content.component

/**
 * Component Meta data for the return result
 */
data class ComponentMetaData(
    val name: String? = "",
    val query: String? = "",
    val queryMap: Map<String, String>? = mapOf()
)
