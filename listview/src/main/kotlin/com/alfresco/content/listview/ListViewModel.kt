package com.alfresco.content.listview

import androidx.lifecycle.viewModelScope
import com.alfresco.content.MvRxViewModel
import com.alfresco.content.data.ResponsePaging
import kotlin.reflect.KSuspendFunction2
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

abstract class ListViewModel(
    val state: ListViewState
) : MvRxViewModel<ListViewState>(state) {

    fun refresh() = fetch()

    fun fetchNextPage() = fetch(true)

    private fun fetch(nextPage: Boolean = false) = withState { state ->
        val req = fetchRequest()
        val skipCount = if (nextPage) state.entries.count() else 0

        viewModelScope.launch {
            req.invoke(
                skipCount,
                ITEMS_PER_PAGE
            ).execute {
                val newEntries = it()?.entries ?: emptyList()
                copy(
                    entries = if (nextPage) {
                        entries + newEntries
                    } else {
                        newEntries
                    },
                    req = it
                )
            }
        }
    }

    abstract fun fetchRequest(): KSuspendFunction2<Int, Int, Flow<ResponsePaging>>

    companion object {
        const val ITEMS_PER_PAGE = 25
    }
}
