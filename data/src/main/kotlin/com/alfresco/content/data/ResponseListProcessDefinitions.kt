package com.alfresco.content.data

import com.alfresco.process.models.ResultListProcessDefinitions

/**
 * Marked as ResponseList class
 */
data class ResponseListProcessDefinitions(
    val size: Int,
    val total: Int,
    val start: Int,
    val listProcessDefinitions: List<ProcessDefinitionDataEntry> = emptyList()
) {
    companion object {

        /**
         * return the ResponseList obj using ResultList
         */
        fun with(raw: ResultListProcessDefinitions): ResponseListProcessDefinitions {
            return ResponseListProcessDefinitions(
                size = raw.size ?: 0,
                total = raw.total ?: 0,
                start = raw.start ?: 0,
                listProcessDefinitions = raw.data?.filter { it.deploymentId != null }?.map { ProcessDefinitionDataEntry.with(it) } ?: emptyList()
            )
        }
    }
}
