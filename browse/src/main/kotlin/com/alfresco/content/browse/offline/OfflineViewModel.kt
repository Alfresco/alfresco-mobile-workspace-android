package com.alfresco.content.browse.offline

import android.content.Context
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.browse.R
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.Entry
import com.alfresco.content.data.OfflineRepository
import com.alfresco.content.data.PageView
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
    val context: Context,
) : ListViewModel<OfflineViewState>(state) {
    init {
        AnalyticsManager().screenViewEvent(PageView.Offline)
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
                .offlineEntries(state.parentId)
                .execute {
                    if (it is Loading) {
                        copy(request = it)
                    } else {
                        update(it()).copy(request = it)
                    }
                }
        }
    }

    override fun refresh() =
        withState {
            // Faking a refresh since changes are updated via [observeDataChanges]
            setState { copy(request = Loading()) }
            setState { copy(request = it.request) }
        }

    override fun fetchNextPage() = Unit

    override fun emptyMessageArgs(state: ListViewState): Triple<Int, Int, Int> =
        Triple(R.drawable.ic_empty_offline, R.string.offline_empty_title, R.string.offline_empty_message)

    fun canSyncOverCurrentNetwork() =
        Settings(context).canSyncOverMeteredNetwork ||
            !ConnectivityTracker.isActiveNetworkMetered(context)

    fun toggleSelection(entry: Entry) =
        setState {
            val hasReachedLimit = selectedEntries.size == MULTI_SELECTION_LIMIT
            if (!entry.isSelectedForMultiSelection && hasReachedLimit) {
                copy(maxLimitReachedForMultiSelection = true)
            } else {
                val updatedEntries =
                    entries.map {
                        if (it.id == entry.id && it.type != Entry.Type.GROUP) {
                            it.copy(isSelectedForMultiSelection = !it.isSelectedForMultiSelection)
                        } else {
                            it
                        }
                    }
                copy(
                    entries = updatedEntries,
                    selectedEntries = updatedEntries.filter { it.isSelectedForMultiSelection },
                    maxLimitReachedForMultiSelection = false,
                )
            }
        }

    fun resetMultiSelection() =
        setState {
            val resetMultiEntries =
                entries.map {
                    it.copy(isSelectedForMultiSelection = false)
                }
            copy(
                entries = resetMultiEntries,
                selectedEntries = emptyList(),
                maxLimitReachedForMultiSelection = false,
            )
        }

    override fun resetMaxLimitError() = setState { copy(maxLimitReachedForMultiSelection = false) }

    companion object : MavericksViewModelFactory<OfflineViewModel, OfflineViewState> {
        override fun create(
            viewModelContext: ViewModelContext,
            state: OfflineViewState,
        ) = OfflineViewModel(state, viewModelContext.app())
    }
}
