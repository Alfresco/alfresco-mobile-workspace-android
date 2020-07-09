package com.alfresco.content.listview

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.Uninitialized
import com.alfresco.content.data.Entry
import com.alfresco.content.data.ResponsePaging

data class ListViewState(
    val entries: List<Entry> = emptyList(),
    val req: Async<ResponsePaging> = Uninitialized
) : MvRxState
