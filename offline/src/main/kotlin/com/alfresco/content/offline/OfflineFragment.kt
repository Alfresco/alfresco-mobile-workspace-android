package com.alfresco.content.offline

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.ViewModelContext
import com.airbnb.mvrx.fragmentViewModel
import com.alfresco.content.actions.ActionRemoveOffline
import com.alfresco.content.actions.on
import com.alfresco.content.data.Entry
import com.alfresco.content.data.OfflineRepository
import com.alfresco.content.data.ResponsePaging
import com.alfresco.content.listview.ListFragment
import com.alfresco.content.listview.ListViewModel
import com.alfresco.content.listview.ListViewState
import com.alfresco.coroutines.asFlow
import kotlinx.coroutines.launch

data class OfflineViewState(
    override val entries: List<Entry> = emptyList(),
    override val hasMoreItems: Boolean = false,
    override val request: Async<ResponsePaging> = Uninitialized,
    override val isCompact: Boolean = false
) : ListViewState {

    fun update(response: ResponsePaging?): OfflineViewState {
        if (response == null) return this

        val nextPage = response.pagination.skipCount > 0
        val pageEntries = response.entries
        val newEntries = if (nextPage) { entries + pageEntries } else { pageEntries }

        return copy(entries = newEntries, hasMoreItems = response.pagination.hasMoreItems)
    }

    override fun copy(_entries: List<Entry>): ListViewState = copy(entries = _entries)
}

class OfflineViewModel(
    state: OfflineViewState,
    val context: Context
) : ListViewModel<OfflineViewState>(state) {

    init {
        refresh()

        viewModelScope.on<ActionRemoveOffline> { removeEntry(it.entry) }
    }

    override fun refresh() = fetch()

    override fun fetchNextPage() = fetch(true)

    private fun fetch(nextPage: Boolean = false) = withState { state ->
        val skipCount = if (nextPage) state.entries.count() else 0

        viewModelScope.launch {
            OfflineRepository()::fetchOfflineEntries.asFlow(skipCount, ITEMS_PER_PAGE)
                .execute {
                    if (it is Loading) {
                        copy(request = it)
                    } else {
                        update(it()).copy(request = it)
                    }
                }
        }
    }

    override fun emptyMessageArgs(state: ListViewState): Triple<Int, Int, Int> =
        Triple(R.drawable.ic_empty_offline, R.string.offline_empty_title, R.string.offline_empty_message)

    companion object : MvRxViewModelFactory<OfflineViewModel, OfflineViewState> {

        override fun create(viewModelContext: ViewModelContext, state: OfflineViewState): OfflineViewModel? {
            return OfflineViewModel(state, viewModelContext.app())
        }
    }
}

class OfflineFragment : ListFragment<OfflineViewModel, OfflineViewState>() {

    override val viewModel: OfflineViewModel by fragmentViewModel()

    override fun onItemClicked(entry: Entry) {
        // TODO("Not yet implemented")
    }
}
