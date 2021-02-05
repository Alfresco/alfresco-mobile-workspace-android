package com.alfresco.content.offline

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
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
import com.airbnb.mvrx.withState
import com.alfresco.content.actions.ActionRemoveOffline
import com.alfresco.content.actions.on
import com.alfresco.content.data.Entry
import com.alfresco.content.data.OfflineRepository
import com.alfresco.content.data.ResponsePaging
import com.alfresco.content.data.SyncWorker
import com.alfresco.content.fragmentViewModelWithArgs
import com.alfresco.content.listview.ListFragment
import com.alfresco.content.listview.ListViewModel
import com.alfresco.content.listview.ListViewState
import com.alfresco.content.navigateTo
import com.alfresco.content.network.ConnectivityTracker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

data class OfflineViewState(
    val parentId: String? = null,
    override val entries: List<Entry> = emptyList(),
    override val hasMoreItems: Boolean = false,
    override val request: Async<ResponsePaging> = Uninitialized,
    val syncNowEnabled: Boolean = false
) : ListViewState {

    constructor(args: OfflineBrowseArgs) : this(parentId = args.id)

    override val isCompact: Boolean
        get() = parentId != null

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

        ConnectivityTracker.startTracking(context)
        viewModelScope.launch {
            SyncWorker
                .observe(context)
                .map { it == WorkInfo.State.RUNNING }
                .combine(ConnectivityTracker.networkAvailable) { running, connected ->
                    !running && connected
                }
                .execute {
                    copy(syncNowEnabled = it() ?: false)
                }
        }
    }

    override fun refresh() = fetch()

    override fun fetchNextPage() = fetch(true)

    private fun fetch(nextPage: Boolean = false) = withState { state ->
        val skipCount = if (nextPage) state.entries.count() else 0

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

    override fun emptyMessageArgs(state: ListViewState): Triple<Int, Int, Int> =
        Triple(R.drawable.ic_empty_offline, R.string.offline_empty_title, R.string.offline_empty_message)

    fun canSyncOverCurrentNetwork() = !ConnectivityTracker.isActiveNetworkMetered(context)

    companion object : MvRxViewModelFactory<OfflineViewModel, OfflineViewState> {

        override fun create(viewModelContext: ViewModelContext, state: OfflineViewState): OfflineViewModel? {
            return OfflineViewModel(state, viewModelContext.app())
        }
    }
}

@Parcelize
data class OfflineBrowseArgs(
    val id: String?,
    val title: String?
) : Parcelable {
    companion object {
        private const val ID_KEY = "id"
        private const val TITLE_KEY = "title"

        fun with(args: Bundle?): OfflineBrowseArgs? {
            if (args == null) return null

            return OfflineBrowseArgs(
                args.getString(ID_KEY, null),
                args.getString(TITLE_KEY, null)
            )
        }
    }
}

class OfflineFragment : ListFragment<OfflineViewModel, OfflineViewState>() {

    override val viewModel: OfflineViewModel by fragmentViewModelWithArgs { OfflineBrowseArgs.with(arguments) }
    private var fab: ExtendedFloatingActionButton? = null

    override fun onDestroyView() {
        super.onDestroyView()

        fab = null
    }

    override fun invalidate() {
        super.invalidate()

        withState(viewModel) { state ->
            // Add fab only to root folder
            if (state.parentId == null && fab == null) {
                fab = makeFab(requireContext()).apply {
                    visibility = View.INVISIBLE // required for animation
                }
                (view as ViewGroup).addView(fab)
            }

            fab?.apply {
                if (state.entries.count() > 0) {
                    show()
                } else {
                    hide()
                }

                isEnabled = state.syncNowEnabled
            }
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
        if (viewModel.canSyncOverCurrentNetwork()) {
            SyncWorker.syncNow(requireContext())
        } else {
            makeSyncUnavailablePrompt().show()
        }
    }

    private fun makeSyncUnavailablePrompt() =
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.offline_sync_unavailable_title))
            .setMessage(resources.getString(R.string.offline_sync_unavailable_message))
            .setPositiveButton(resources.getString(R.string.offline_sync_unavailable_button), null)

    override fun onItemClicked(entry: Entry) {
        if (entry.isFolder || entry.isSynced) {
            findNavController().navigateTo(entry)
        }
    }
}
