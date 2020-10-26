package com.alfresco.content.actions

import com.airbnb.mvrx.MvRxState
import com.alfresco.content.data.Entry

data class ActionListState(
    val entry: Entry,
    val actions: List<Action> = emptyList()
) : MvRxState {
    constructor(target: Entry) : this(entry = target)
}
