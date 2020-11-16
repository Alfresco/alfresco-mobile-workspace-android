package com.alfresco.content.actions

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.Uninitialized
import com.alfresco.content.data.Entry

data class ActionListState(
    val entry: Entry,
    val actions: List<Action> = emptyList(),
    val topActions: List<Action> = emptyList(),
    val fetch: Async<Entry> = Uninitialized
) : MvRxState {
    constructor(target: Entry) : this(entry = target)
}
