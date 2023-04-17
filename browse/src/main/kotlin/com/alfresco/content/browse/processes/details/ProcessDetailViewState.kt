package com.alfresco.content.browse.processes.details

import com.airbnb.mvrx.MavericksState
import com.alfresco.content.data.ProcessDefinitionDataEntry

/**
 * Marked as ProcessDetailViewState
 */
data class ProcessDetailViewState(
    val entry: ProcessDefinitionDataEntry
) : MavericksState
