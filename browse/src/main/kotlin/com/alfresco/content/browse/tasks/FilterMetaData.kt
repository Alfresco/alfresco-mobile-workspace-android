package com.alfresco.content.browse.tasks

/**
 * Component Meta data for the return result
 */
data class FilterMetaData(
    val name: String? = "",
    val query: String? = "",
    val queryMap: Map<String, String>? = mapOf()
)
