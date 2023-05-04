package com.alfresco.content.data

import com.alfresco.process.models.ResultListProcessDefinitions

/**
 * Marked as ResponseListProcessDefinitions class
 */
data class ResponseListProcessDefinition(
    val size: Int,
    val total: Int,
    val start: Int,
    val listProcessDefinitions: List<ProcessDefinitionDataEntry> = emptyList()
) {
    companion object {

        /**
         * return the ResponseListProcessDefinition obj using ResultListProcessDefinitions
         */
        fun with(raw: ResultListProcessDefinitions): ResponseListProcessDefinition {
            return ResponseListProcessDefinition(
                size = raw.size ?: 0,
                total = raw.total ?: 0,
                start = raw.start ?: 0,
                listProcessDefinitions = raw.data?.map { ProcessDefinitionDataEntry.with(it) } ?: emptyList()
            )
        }
    }
}
