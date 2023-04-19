package com.alfresco.content.browse.processes.details

import com.airbnb.mvrx.MavericksState
import com.alfresco.content.data.ProcessEntry

/**
 * Marked as ProcessDetailViewState
 */
data class ProcessDetailViewState(
    val entry: ProcessEntry
) : MavericksState
