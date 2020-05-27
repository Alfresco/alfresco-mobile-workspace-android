package com.alfresco.content.search

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.Uninitialized
import com.alfresco.content.models.ResultSetRowEntry

data class SearchViewState(
    val results: Async<List<ResultSetRowEntry>> = Uninitialized
) : MvRxState
