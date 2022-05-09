package com.alfresco.content.move

import android.content.Context
import android.net.Uri
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.actions.Action
import com.alfresco.content.actions.ActionCreateFolder
import com.alfresco.content.actions.ActionExtension
import com.alfresco.content.actions.ActionUploadExtensionFiles
import com.alfresco.content.data.BrowseRepository
import com.alfresco.content.data.Entry
import com.alfresco.content.data.OfflineRepository
import com.alfresco.content.data.ResponsePaging
import com.alfresco.content.listview.ListViewModel
import com.alfresco.content.listview.ListViewState
import com.alfresco.coroutines.asFlow
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch

class BrowseMoveViewModel(
    state: BrowseMoveViewState,
    val context: Context
) : ListViewModel<BrowseMoveViewState>(state) {

    private var observeUploadsJob: Job? = null
    private var observeTransferUploadsJob: Job? = null
    private val browseRepository = BrowseRepository()
    private val offlineRepository = OfflineRepository()

    init {
        fetchInitial()
        withState {
            if (it.sortOrder == BrowseMoveViewState.SortOrder.ByModifiedDate) {
                BrowseMoveViewState.ModifiedGroup.prepare(context)
            }
        }
    }

    @Suppress("ControlFlowWithEmptyBody")
    private fun Entry.ifType(
        types: Set<Entry.Type>,
        block: (entry: Entry) -> Unit
    ) = if (types.contains(type)) {
        block(this)
    } else {
        // TODO
    }

    /**
     * refresh totalTransferSize count
     */
    fun refreshTransfersSize() = setState { copy(totalTransfersSize = offlineRepository.getTotalTransfersSize()) }

    @Suppress("UNUSED_PARAMETER")
    private fun refresh(ignored: Entry) = refresh() // TODO: why?

    override fun refresh() = fetchInitial()

    override fun fetchNextPage() = withState { state ->
        val path = state.path
        val nodeId = state.nodeId
        val skipCount = state.baseEntries.count()

        viewModelScope.launch {
            loadResults(
                path,
                nodeId,
                skipCount,
                ITEMS_PER_PAGE
            ).execute {
                when (it) {
                    is Loading -> copy(request = Loading())
                    is Fail -> copy(request = Fail(it.error))
                    is Success -> {
                        update(it()).copy(request = Success(it()))
                    }
                    else -> {
                        this
                    }
                }
            }
        }
    }

    private fun fetchInitial() = withState { state ->
        viewModelScope.launch {
            // Fetch children and folder information
            loadResults(
                state.path,
                state.nodeId,
                0,
                ITEMS_PER_PAGE
            ).zip(
                fetchNode(state.path, state.nodeId)
            ) { paging, parent ->
                Pair(paging, parent)
            }.execute {
                when (it) {
                    is Loading -> copy(request = Loading())
                    is Fail -> copy(request = Fail(it.error))
                    is Success -> {
                        observeUploads(it().second?.id)
                        update(it().first).copy(parent = it().second, request = Success(it().first))
                    }
                    else -> {
                        this
                    }
                }
            }
        }
    }

    private suspend fun fetchNode(path: String, item: String?): Flow<Entry?> = if (item == null) {
        flowOf(null)
    } else {
        BrowseRepository()::fetchEntry.asFlow(item)
    }

    private suspend fun loadResults(path: String, item: String?, skipCount: Int, maxItems: Int): Flow<ResponsePaging> {
        return when (path) {
            context.getString(R.string.nav_path_move), context.getString(R.string.nav_path_extension) ->
                BrowseRepository()::fetchExtensionFolderItems.asFlow(requireNotNull(item), skipCount, maxItems)

            else -> throw IllegalStateException()
        }
    }

    private fun observeUploads(nodeId: String?) {
        if (nodeId == null) return

        val repo = OfflineRepository()

        // On refresh clean completed uploads
        repo.removeCompletedUploads(nodeId)

        observeUploadsJob?.cancel()
        observeUploadsJob = repo.observeUploads(nodeId)
            .execute {
                if (it is Success) {
                    updateUploads(it())
                } else {
                    this
                }
            }
    }

    /**
     * observer for transfer uploads
     */
    fun observeTransferUploads() {

        observeTransferUploadsJob?.cancel()
        observeTransferUploadsJob = OfflineRepository().observeTransferUploads()
            .execute {
                if (it is Success) {
                    updateTransferUploads(it())
                } else {
                    this
                }
            }
    }

    /**
     * reset local files after uploading to server
     */
    fun resetTransferData() {
        offlineRepository.removeCompletedUploads()
        offlineRepository.updateTransferSize(0)
    }

    override fun emptyMessageArgs(state: ListViewState) =
        Triple(R.drawable.ic_empty_folder, R.string.folder_empty_title, R.string.folder_empty_message)

    /**
     * Upload files on the current node
     */
    fun uploadFiles(state: BrowseMoveViewState) {
        val list = browseRepository.getExtensionDataList().map { Uri.parse(it) }
        if (!list.isNullOrEmpty() && state.parent != null) {
            execute(ActionUploadExtensionFiles(state.parent), list)
            browseRepository.clearExtensionData()
        }
    }

    /**
     * It will create the new folder on the current node
     */
    fun createFolder(state: BrowseMoveViewState) {
        if (state.parent != null)
            execute(ActionCreateFolder(state.parent))
    }

    private fun execute(action: ActionExtension, list: List<Uri>) =
        action.execute(context, GlobalScope, list)

    private fun execute(action: Action) =
        action.execute(context, GlobalScope)

    companion object : MavericksViewModelFactory<BrowseMoveViewModel, BrowseMoveViewState> {

        override fun create(
            viewModelContext: ViewModelContext,
            state: BrowseMoveViewState
        ) = BrowseMoveViewModel(state, viewModelContext.activity)
    }
}
