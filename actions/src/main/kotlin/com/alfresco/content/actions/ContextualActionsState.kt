package com.alfresco.content.actions

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.Uninitialized
import com.alfresco.content.data.ContextualActionData
import com.alfresco.content.data.Entry
import com.alfresco.content.data.MobileConfigDataEntry

data class ContextualActionsState(
    val entries: List<Entry> = emptyList(),
    val isMultiSelection: Boolean = false,
    val actions: List<Action> = emptyList(),
    val topActions: List<Action> = emptyList(),
    val fetch: Async<Entry> = Uninitialized,
    val mobileConfigDataEntry: MobileConfigDataEntry? = null,
) : MavericksState {
    constructor(target: ContextualActionData) : this(
        entries = target.entries,
        isMultiSelection = target.isMultiSelection,
        mobileConfigDataEntry = target.mobileConfigData,
    )
}
