package com.alfresco.content.listview

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.epoxy.EpoxyRecyclerView
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.withState
import com.alfresco.content.actions.ActionAddFavorite
import com.alfresco.content.actions.ActionAddOffline
import com.alfresco.content.actions.ActionCreateFolder
import com.alfresco.content.actions.ActionDelete
import com.alfresco.content.actions.ActionMoveFilesFolders
import com.alfresco.content.actions.ActionRemoveFavorite
import com.alfresco.content.actions.ActionRemoveOffline
import com.alfresco.content.actions.ActionStartProcess
import com.alfresco.content.actions.ActionUpdateFileFolder
import com.alfresco.content.actions.ContextualActionsSheet
import com.alfresco.content.data.ContextualActionData
import com.alfresco.content.data.Entry
import com.alfresco.content.data.ResponsePaging
import com.alfresco.content.simpleController
import com.alfresco.events.on
import com.alfresco.list.replace
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

interface ListViewState : MavericksState {
    val entries: List<Entry>
    val selectedEntries: List<Entry>
    val hasMoreItems: Boolean
    val request: Async<ResponsePaging>
    val isCompact: Boolean

    fun copy(_entries: List<Entry>): ListViewState

    fun copyRemoving(entry: Entry) =
        copy(
            entries.filter {
                it.id != entry.id
            },
        )

    fun copyUpdating(entry: Entry) =
        copy(
            entries.replace(entry) {
                it.id == entry.id
            },
        )
}

abstract class ListViewModel<S : ListViewState>(
    initialState: S,
) : MavericksViewModel<S>(initialState) {

    private val _sharedFlow = MutableSharedFlow<Entry>()
    val sharedFlow = _sharedFlow.asSharedFlow()
    private var folderListener: EntryListener? = null

    init {
        viewModelScope.on<ActionCreateFolder> { onCreateFolder(it.entry) }
        viewModelScope.on<ActionDelete> { onDelete(it.entry) }
        viewModelScope.on<ActionUpdateFileFolder> { refresh() }
        viewModelScope.on<ActionAddFavorite> { updateEntry(it.entry) }
        viewModelScope.on<ActionRemoveFavorite> { updateEntry(it.entry) }
        viewModelScope.on<ActionAddOffline> { updateEntry(it.entry) }
        viewModelScope.on<ActionRemoveOffline> { updateEntry(it.entry) }
        viewModelScope.on<ActionMoveFilesFolders> { onMove(it.entry) }
        viewModelScope.on<ActionStartProcess> { onStartProcess(it.entry) }
    }

    private fun onStartProcess(entry: Entry) = entry.run {
        if (entry.isFile) {
            folderListener?.onProcessStart(entry)
        }
    }

    private fun onDelete(entry: Entry) = entry.run {
        if (isFile) {
            removeEntry(entry)
        } else {
            refresh()
        }
    }

    private fun onCreateFolder(entry: Entry) = entry.run {
        refresh()
        if (entry.isFolder) {
            folderListener?.onEntryCreated(entry)
        }
    }

    private fun onMove(entry: Entry) = entry.run {
        refresh()
    }

    @Suppress("UNCHECKED_CAST")
    fun removeEntry(entry: Entry) =
        setState { copyRemoving(entry) as S }

    @Suppress("UNCHECKED_CAST")
    private fun updateEntry(entry: Entry) =
        setState { copyUpdating(entry) as S }

    private fun <T> List<T>.replace(newValue: T, block: (T) -> Boolean): List<T> {
        return map {
            if (block(it)) newValue else it
        }
    }

    /**
     * Set the listener to be notified when a new folder created and move to created folder screen
     */
    fun setListener(listener: EntryListener) {
        folderListener = listener
    }

    abstract fun refresh()
    abstract fun fetchNextPage()
    abstract fun emptyMessageArgs(state: ListViewState): Triple<Int, Int, Int>

    companion object {
        const val ITEMS_PER_PAGE = 25
        const val MULTI_SELECTION_LIMIT = 25
    }
}

/**
 * Mark as ListFragment
 */
abstract class ListFragment<VM : ListViewModel<S>, S : ListViewState>(layoutID: Int = R.layout.fragment_list) :
    Fragment(layoutID), MavericksView, EntryListener {
    abstract val viewModel: VM

    lateinit var loadingAnimation: View
    lateinit var recyclerView: EpoxyRecyclerView
    lateinit var refreshLayout: SwipeRefreshLayout
    lateinit var loadingMessage: TextView
    var tvUploadingFiles: TextView? = null
    var tvPercentage: TextView? = null
    var bannerTransferData: FrameLayout? = null
    var uploadButton: MaterialButton? = null
    var moveHereButton: MaterialButton? = null
    var cancelButton: MaterialButton? = null
    var percentageFiles: LinearProgressIndicator? = null
    private val epoxyController: AsyncEpoxyController by lazy { epoxyController() }
    private var delayedBoundary: Boolean = false
    private var longPressHandled = false
    private var isViewRequiredMultiSelection = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingAnimation = view.findViewById(R.id.loading_animation)
        recyclerView = view.findViewById(R.id.recycler_view)
        refreshLayout = view.findViewById(R.id.refresh_layout)
        loadingMessage = view.findViewById(R.id.loading_message)
        bannerTransferData = view.findViewById(R.id.banner_parent)

        uploadButton = view.findViewById(R.id.upload_button)
        moveHereButton = view.findViewById(R.id.move_here_button)
        cancelButton = view.findViewById(R.id.cancel_button)
        tvUploadingFiles = view.findViewById(R.id.tv_uploading_files)
        tvPercentage = view.findViewById(R.id.tv_percentage)
        percentageFiles = view.findViewById(R.id.percentage_files)

        refreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }

        recyclerView.setController(epoxyController)

        viewModel.setListener(this)

        epoxyController.adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0) {
                    // @see: https://github.com/airbnb/epoxy/issues/224
                    recyclerView.layoutManager?.scrollToPosition(0)
                }
            }
        })
    }

    override fun invalidate() = withState(viewModel) { state ->

        if (longPressHandled) {
            if (state.selectedEntries.isEmpty()) {
                MultiSelection.multiSelectionChangedFlow.tryEmit(
                    MultiSelectionData(
                        selectedEntries = state.selectedEntries,
                        isMultiSelectionEnabled = false,
                    ),
                )
            } else {
                MultiSelection.multiSelectionChangedFlow.tryEmit(
                    MultiSelectionData(
                        selectedEntries = state.selectedEntries,
                        isMultiSelectionEnabled = true,
                    ),
                )
            }
        }

        loadingAnimation.isVisible =
            state.request is Loading && state.entries.isEmpty() && !refreshLayout.isRefreshing

        uploadButton?.isEnabled = state.request is Success

        if (state.request.complete) {
            refreshLayout.isRefreshing = false
        }
        epoxyController.requestModelBuild()
    }

    /**
     * Disable refresh layout while sharing files
     */
    fun disableRefreshLayout() {
        refreshLayout.isEnabled = false
    }

    private fun enableLongPress() {
        longPressHandled = true
        refreshLayout.isEnabled = false
    }

    fun disableLongPress() {
        longPressHandled = false
        refreshLayout.isEnabled = true
    }

    fun setViewRequiredMultiSelection(isViewRequiredMultiSelection: Boolean) {
        this.isViewRequiredMultiSelection = isViewRequiredMultiSelection
    }

    private fun epoxyController() = simpleController(viewModel) { state ->
        if (state.entries.isEmpty() && state.request.complete) {
            val args = viewModel.emptyMessageArgs(state)
            listViewMessage {
                id("empty_message")
                iconRes(args.first)
                title(args.second)
                message(args.third)
            }
        } else if (state.entries.isNotEmpty()) {
            val selectedEntries = state.entries.find { obj -> obj.isSelectedForMultiSelection }
            if (selectedEntries == null) {
                disableLongPress()
            }

            state.entries.forEach {
                if (it.type == Entry.Type.GROUP) {
                    listViewGroupHeader {
                        id(it.name)
                        title(it.name)
                    }
                } else {
                    listViewRow {
                        id(stableId(it))
                        data(it)
                        compact(state.isCompact)
                        multiSelection(state.selectedEntries.isNotEmpty())
                        clickListener { model, _, _, _ ->
                            if (!longPressHandled) {
                                onItemClicked(model.data())
                            } else {
                                onItemLongClicked(model.data())
                            }
                        }
                        if (isViewRequiredMultiSelection) {
                            longClickListener { model, _, _, _ ->
                                if (!longPressHandled) {
                                    enableLongPress()
                                    onItemLongClicked(model.data())
                                    true
                                } else {
                                    false
                                }
                            }
                        }
                        moreClickListener { model, _, _, _ -> onItemMoreClicked(model.data()) }
                    }
                }
            }

            if (state.hasMoreItems) {
                if (state.request is Loading) {
                    listViewPageLoading {
                        id("loading at ${state.entries.size}")
                    }
                } else {
                    // On failure delay creating the boundary so that the list scrolls up
                    // and the user has to scroll back down to activate a retry
                    val isFail = state.request is Fail
                    if (isFail && !delayedBoundary) {
                        delayedBoundary = true
                        Handler(Looper.getMainLooper()).postDelayed({ this.requestModelBuild() }, 300)
                    } else {
                        if (isFail) {
                            delayedBoundary = false
                        }
                        listViewPageBoundary {
                            id("boundary at ${state.entries.size}")
                            onBind { _, _, _ -> viewModel.fetchNextPage() }
                        }
                    }
                }
            }
        }
    }

    private fun stableId(entry: Entry): String =
        if (entry.isUpload) {
            entry.boxId.toString()
        } else entry.id

    abstract fun onItemClicked(entry: Entry)
    open fun onItemLongClicked(entry: Entry) {}

    open fun onItemMoreClicked(entry: Entry) {
        ContextualActionsSheet.with(ContextualActionData.withEntries(listOf(entry))).show(childFragmentManager, null)
    }
}
