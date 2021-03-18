package com.alfresco.content.browse.offline

import android.content.Context
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.browse.R
import com.alfresco.content.data.OfflineRepository
import com.alfresco.content.data.Settings
import com.alfresco.content.data.SyncService
import com.alfresco.content.listview.ListViewModel
import com.alfresco.content.listview.ListViewState
import com.alfresco.content.network.ConnectivityTracker
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class OfflineViewModel(
    state: OfflineViewState,
    val context: Context
) : ListViewModel<OfflineViewState>(state) {

    init {
        viewModelScope.launch {
            SyncService
                .observe(context)
                .map { it == SyncService.SyncState.Running }
                .combine(ConnectivityTracker.networkAvailable) { running, connected ->
                    !running && connected
                }
                .execute {
                    copy(syncNowEnabled = it() ?: false)
                }
        }

        observeDataChanges(state)
    }

    private fun observeDataChanges(state: OfflineViewState) {
        viewModelScope.launch {
            OfflineRepository()
                .observeOfflineEntries(state.parentId)
                .execute {
                    if (it is Loading) {
                        copy(request = it)
                    } else {
                        update(it()).copy(request = it)
                    }
                }
        }
    }

    override fun refresh() = fetch()

    override fun fetchNextPage() = fetch(true)

    private fun fetch(nextPage: Boolean = false) {
        setState {
            copy(request = Loading())
        }
        setState {
            val result = OfflineRepository().fetchOfflineEntries(parentId)
            update(result).copy(request = Success(result))
        }
    }

    override fun emptyMessageArgs(state: ListViewState): Triple<Int, Int, Int> =
        Triple(R.drawable.ic_empty_offline, R.string.offline_empty_title, R.string.offline_empty_message)

    fun canSyncOverCurrentNetwork() =
        Settings(context).canSyncOverMeteredNetwork ||
            !ConnectivityTracker.isActiveNetworkMetered(context)

    companion object : MavericksViewModelFactory<OfflineViewModel, OfflineViewState> {

        override fun create(
            viewModelContext: ViewModelContext,
            state: OfflineViewState
        ) = OfflineViewModel(state, viewModelContext.app())
    }
}
