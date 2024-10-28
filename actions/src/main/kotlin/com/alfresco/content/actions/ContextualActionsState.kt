package com.alfresco.content.actions

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.Uninitialized
import com.alfresco.content.data.AppMenu
import com.alfresco.content.data.ContextualActionData
import com.alfresco.content.data.Entry

data class ContextualActionsState(
    val entries: List<Entry> = emptyList(),
    val isMultiSelection: Boolean = false,
    val actions: List<Action>? = null,
    val topActions: List<Action> = emptyList(),
    val fetch: Async<Entry> = Uninitialized,
    val appMenu: List<AppMenu>? = null,
) : MavericksState {
    constructor(target: ContextualActionData) : this(
        entries = target.entries,
        isMultiSelection = target.isMultiSelection,
        appMenu = target.appMenu,
    )
}
