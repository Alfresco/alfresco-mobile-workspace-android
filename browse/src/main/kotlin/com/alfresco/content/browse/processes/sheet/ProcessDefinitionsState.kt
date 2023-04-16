package com.alfresco.content.browse.processes.sheet

import com.airbnb.mvrx.MavericksState
import com.alfresco.content.data.Entry
import com.alfresco.content.data.ProcessDefinitionDataEntry

/**
 * Marked as ProcessDefinitionsState
 */
data class ProcessDefinitionsState(
    val entry: Entry,
    val listProcessDefinitions: List<ProcessDefinitionDataEntry>? = null
) : MavericksState {
    constructor(target: Entry) : this(entry = target)
}
