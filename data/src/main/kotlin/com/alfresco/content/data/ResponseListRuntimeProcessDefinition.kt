package com.alfresco.content.data

import com.alfresco.process.models.ResultListRuntimeProcessDefinitions

/**
 * Marked as ResponseListProcessDefinitions class
 */
data class ResponseListRuntimeProcessDefinition(
    val size: Int,
    val total: Int,
    val start: Int,
    val listRuntimeProcessDefinitions: List<RuntimeProcessDefinitionDataEntry> = emptyList(),
) {
    companion object {
        /**
         * return the ResponseListProcessDefinitions obj using ResultListRuntimeProcessDefinitions
         */
        fun with(raw: ResultListRuntimeProcessDefinitions): ResponseListRuntimeProcessDefinition {
            return ResponseListRuntimeProcessDefinition(
                size = raw.size ?: 0,
                total = raw.total ?: 0,
                start = raw.start ?: 0,
                listRuntimeProcessDefinitions =
                    raw.data?.filter {
                        it.deploymentId != null
                    }?.map { RuntimeProcessDefinitionDataEntry.with(it) } ?: emptyList(),
            )
        }
    }
}
