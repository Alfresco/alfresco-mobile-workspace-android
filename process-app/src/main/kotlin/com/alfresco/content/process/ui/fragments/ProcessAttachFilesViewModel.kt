package com.alfresco.content.process.ui.fragments

import android.content.Context
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.ViewModelContext
import com.alfresco.content.GetMultipleContents
import com.alfresco.content.actions.Action
import com.alfresco.content.actions.ActionOpenWith
import com.alfresco.content.common.EntryListener
import com.alfresco.content.data.Entry
import com.alfresco.content.data.OfflineRepository
import com.alfresco.content.data.TaskRepository
import com.alfresco.content.data.UploadServerType
import com.alfresco.content.data.payloads.FieldType
import com.alfresco.content.process.R
import com.alfresco.events.on
import kotlinx.coroutines.Job
import java.io.File

class ProcessAttachFilesViewModel(
    val state: ProcessAttachFilesViewState,
    val context: Context,
    private val repository: TaskRepository,
) : MavericksViewModel<ProcessAttachFilesViewState>(state) {

    private var observeUploadsJob: Job? = null
    var parentId: String = ""
    private var entryListener: EntryListener? = null

    init {

        viewModelScope.on<ActionOpenWith> {
            if (!it.entry.path.isNullOrEmpty()) {
                entryListener?.onEntryCreated(it.entry)
            }
        }

        val field = state.parent.field

        when (field.type) {
            FieldType.READONLY.value(), FieldType.READONLY_TEXT.value() -> {
                state.parent.process
                setState { copy(listContents = field.getContentList(state.parent.process.processDefinitionId), baseEntries = field.getContentList(state.parent.process.processDefinitionId)) }
            }

            else -> {
//                setState { copy(listContents = field.getContentList(state.parent.search.processDefinitionId), baseEntries = field.getContentList(state.parent.search.processDefinitionId)) }
                observeUploads(state)
            }
        }
    }

    /**
     * returns the current logged in APS user profile data
     */
    fun getAPSUser() = repository.getAPSUser()

    /**
     * delete content locally
     */
    fun deleteAttachment(entry: Entry) = stateFlow.execute {
        OfflineRepository().remove(entry)
        deleteUploads(entry.id)
    }

    private fun observeUploads(state: ProcessAttachFilesViewState) {
        val process = state.parent.process

        parentId = process.id.ifEmpty { process.processDefinitionId } ?: ""

        val repo = OfflineRepository()

        observeUploadsJob?.cancel()
        observeUploadsJob = repo.observeProcessUploads(parentId, UploadServerType.UPLOAD_TO_PROCESS)
            .execute {
                if (it is Success) {
                    updateUploads(state.parent.field.id, it())
                } else {
                    this
                }
            }
    }

    fun emptyMessageArgs(state: ProcessAttachFilesViewState) =
        when {
            else ->
                Triple(R.drawable.ic_empty_files, R.string.no_attached_files, context.getString(R.string.file_empty_message, GetMultipleContents.MAX_FILE_SIZE_10))
        }

    /**
     * execute "open with" action to download the content data
     */
    fun executePreview(action: Action) {
        val entry = action.entry as Entry
        val file = File(repository.session.contentDir, entry.fileName)
        if (!entry.isDocFile && repository.session.isFileExists(file) && file.length() != 0L) {
            entryListener?.onEntryCreated(Entry.updateDownloadEntry(entry, file.path))
        } else action.execute(context, kotlinx.coroutines.GlobalScope)
    }

    fun setListener(listener: EntryListener) {
        this.entryListener = listener
    }

    companion object : MavericksViewModelFactory<ProcessAttachFilesViewModel, ProcessAttachFilesViewState> {

        override fun create(
            viewModelContext: ViewModelContext,
            state: ProcessAttachFilesViewState,
        ) = ProcessAttachFilesViewModel(state, viewModelContext.activity(), TaskRepository())
    }
}
