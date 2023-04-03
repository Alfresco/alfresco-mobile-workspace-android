package com.alfresco.content.actions

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.Uninitialized
import com.alfresco.content.data.Entry

data class ProcessDefinitionsState(
    val entry: Entry,
    val list: List<String> = emptyList(),
    val linkContent: Async<Entry> = Uninitialized
) : MavericksState {
    constructor(target: Entry) : this(entry = target)
}
