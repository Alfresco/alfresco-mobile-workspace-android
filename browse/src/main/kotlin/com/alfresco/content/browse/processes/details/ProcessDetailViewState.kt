package com.alfresco.content.browse.processes.details

import com.airbnb.mvrx.MavericksState
import com.alfresco.content.data.ProcessDefinitionDataEntry

data class ProcessDetailViewState(
    val entry: ProcessDefinitionDataEntry
) : MavericksState
