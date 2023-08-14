package com.alfresco.content.actions.sheet

import com.airbnb.mvrx.MavericksState
import com.alfresco.content.data.Entry
import com.alfresco.content.data.RuntimeProcessDefinitionDataEntry

/**
 * Marked as ProcessDefinitionsState
 */
data class ProcessDefinitionsState(
    val entries: List<Entry> = emptyList(),
    val listProcessDefinitions: List<RuntimeProcessDefinitionDataEntry>? = null,
) : MavericksState {
    constructor(target: List<Entry>) : this(entries = target)
}
