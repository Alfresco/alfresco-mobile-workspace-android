package com.alfresco.content.browse.transfer

import android.content.Context
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.browse.R
import com.alfresco.content.data.Entry
import com.alfresco.content.data.OfflineRepository
import com.alfresco.content.data.Settings
import com.alfresco.content.data.SyncService
import com.alfresco.content.network.ConnectivityTracker
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Mark as UploadFilesViewState
 */
data class TransferFilesViewState(
    val extension: Boolean,
    val entries: List<Entry> = emptyList(),
    val syncNowEnabled: Boolean = false,
) : MavericksState {
    constructor(args: TransferFilesArgs) : this(args.extension)

    /**
     * update the uploads list
     */
    fun updateTransferUploads(uploads: List<Entry>): TransferFilesViewState {
        return copy(entries = uploads)
    }
}

/**
 * Mark as TransferFilesViewModel
 */
class TransferFilesViewModel(
    state: TransferFilesViewState,
    val context: Context,
) : MavericksViewModel<TransferFilesViewState>(state) {

    private var observeExtensionUploadsJob: Job? = null

    init {
        viewModelScope.launch {
            SyncService
                .observeTransfer(context)
                .map { it == SyncService.SyncState.Running }
                .combine(ConnectivityTracker.networkAvailable) { running, connected ->
                    !running && connected
                }
                .execute {
                    copy(syncNowEnabled = it() ?: false)
                }
        }

        observeExtensionUploads()
        val list = OfflineRepository().buildTransferList()
        setState { copy(entries = list) }
    }

    /**
     * return empty message is transfers has no data
     */
    fun emptyMessageArgs() = Triple(R.drawable.ic_empty_folder, R.string.folder_empty_upload_title, R.string.folder_empty_upload_message)

    private fun observeExtensionUploads() {
        val repo = OfflineRepository()

        observeExtensionUploadsJob?.cancel()
        observeExtensionUploadsJob = repo.observeTransferUploads()
            .execute {
                if (it is Success) {
                    updateTransferUploads(it())
                } else {
                    this
                }
            }
    }

    /**
     * syncing network
     */
    fun canSyncOverCurrentNetwork() =
        Settings(context).canSyncOverMeteredNetwork ||
            !ConnectivityTracker.isActiveNetworkMetered(context)

    companion object : MavericksViewModelFactory<TransferFilesViewModel, TransferFilesViewState> {

        override fun create(
            viewModelContext: ViewModelContext,
            state: TransferFilesViewState,
        ) = TransferFilesViewModel(state, viewModelContext.app())
    }
}
