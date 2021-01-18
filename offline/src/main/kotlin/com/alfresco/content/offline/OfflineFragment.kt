package com.alfresco.content.offline

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.work.WorkInfo
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MvRxViewModelFactory
import com.airbnb.mvrx.Uninitialized
import com.airbnb.mvrx.ViewModelContext
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.actions.ActionRemoveOffline
import com.alfresco.content.actions.on
import com.alfresco.content.data.Entry
import com.alfresco.content.data.OfflineRepository
import com.alfresco.content.data.OfflineStatus
import com.alfresco.content.data.ResponsePaging
import com.alfresco.content.data.SyncWorker
import com.alfresco.content.listview.ListFragment
import com.alfresco.content.listview.ListViewModel
import com.alfresco.content.listview.ListViewState
import com.alfresco.content.navigateTo
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.coroutines.launch

data class OfflineViewState(
    override val entries: List<Entry> = emptyList(),
    override val hasMoreItems: Boolean = false,
    override val request: Async<ResponsePaging> = Uninitialized,
    override val isCompact: Boolean = false,
    val status: WorkInfo.State? = null
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

        viewModelScope.launch {
            SyncWorker
                .observe(context)
                .execute {
                    copy(status = it())
                }
        }
    }

    override fun refresh() = fetch()

    override fun fetchNextPage() = fetch(true)

    private fun fetch(nextPage: Boolean = false) = withState { state ->
        val skipCount = if (nextPage) state.entries.count() else 0

        viewModelScope.launch {
            OfflineRepository()
                .observeOfflineEntries()
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
    lateinit var fab: ExtendedFloatingActionButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fab = makeFab(view.context)
        fab.visibility = View.INVISIBLE // required for animation
        (view as ViewGroup).addView(fab)
    }

    override fun invalidate() {
        super.invalidate()

        withState(viewModel) { state ->
            if (state.entries.count() > 0) {
                fab.show()
            } else {
                fab.hide()
            }

            fab.isEnabled = state.status != WorkInfo.State.RUNNING &&
                state.status != WorkInfo.State.BLOCKED
        }
    }

    private fun makeFab(context: Context) =
        ExtendedFloatingActionButton(context).apply {
            layoutParams = CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.WRAP_CONTENT,
                CoordinatorLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                setMargins(0, 0, 0, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics).toInt())
            }
            text = context.getText(R.string.offline_sync_button_title)
            gravity = Gravity.CENTER
            setOnClickListener {
                onSyncButtonClick()
            }
        }

    private fun onSyncButtonClick() {
        SyncWorker.syncNow(requireContext())
    }

    override fun onItemClicked(entry: Entry) {
        if (entry.offlineStatus == OfflineStatus.Synced) {
            findNavController().navigateTo(entry)
        }
    }
}
