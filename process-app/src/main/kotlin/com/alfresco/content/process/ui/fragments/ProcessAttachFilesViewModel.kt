package com.alfresco.content.process.ui.fragments

import android.content.Context
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.common.EntryListener
import com.alfresco.content.data.OfflineRepository
import com.alfresco.content.data.TaskRepository
import com.alfresco.content.data.UploadServerType
import com.alfresco.content.process.R
import kotlinx.coroutines.Job

class ProcessAttachFilesViewModel(
    val state: ProcessAttachFilesViewState,
    val context: Context,
    private val repository: TaskRepository,
) : MavericksViewModel<ProcessAttachFilesViewState>(state) {

    private var observeUploadsJob: Job? = null
    var observerID: String = ""
    private var entryListener: EntryListener? = null

    init {
        observeUploads(state)
    }

    /**
     * returns the current logged in APS user profile data
     */
    fun getAPSUser() = repository.getAPSUser()

    /**
     * delete content locally
     */
    fun deleteAttachment(contentId: String) = stateFlow.execute {
        deleteUploads(contentId, observerID)
    }

    private fun observeUploads(state: ProcessAttachFilesViewState) {
        val process = state.parent.process

        observerID = process.id

        val repo = OfflineRepository()

        observeUploadsJob?.cancel()
        observeUploadsJob = repo.observeProcessUploads(observerID, UploadServerType.UPLOAD_TO_PROCESS)
            .execute {
                if (it is Success) {
                    updateUploads(state.parent.field.id, it())
                } else {
                    this
                }
            }
    }

    fun setListener(listener: EntryListener) {
        entryListener = listener
    }

    fun emptyMessageArgs(state: ProcessAttachFilesViewState) =
        when {
            else ->
                Triple(R.drawable.ic_empty_files, R.string.no_attached_files, R.string.file_empty_message)
        }

    companion object : MavericksViewModelFactory<ProcessAttachFilesViewModel, ProcessAttachFilesViewState> {

        override fun create(
            viewModelContext: ViewModelContext,
            state: ProcessAttachFilesViewState,
        ) = ProcessAttachFilesViewModel(state, viewModelContext.activity(), TaskRepository())
    }
}
