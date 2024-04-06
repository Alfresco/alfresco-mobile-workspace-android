package com.alfresco.content.actions

import android.content.Context
import android.view.View
import androidx.documentfile.provider.DocumentFile
import com.alfresco.content.ContentPickerFragment
import com.alfresco.content.GetMultipleContents
import com.alfresco.content.actions.Action.Companion.ERROR_FILE_SIZE_EXCEED
import com.alfresco.content.data.Entry
import com.alfresco.content.data.EventName
import com.alfresco.content.data.OfflineRepository
import com.alfresco.content.data.ParentEntry
import com.alfresco.content.data.UploadServerType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Marked as ActionUploadFiles for uploading the files
 */
data class ActionUploadFiles(
    override var entry: Entry,
    override val icon: Int = R.drawable.ic_action_upload,
    override val title: Int = R.string.action_upload_files_title,
    override val eventName: EventName = if (entry.uploadServer == UploadServerType.UPLOAD_TO_TASK) EventName.TaskUploadFiles else EventName.UploadFiles,
) : Action {

    private val repository = OfflineRepository()

    override suspend fun execute(context: Context): Entry {
        val result = ContentPickerFragment.pickItems(context, MIME_TYPES)
        if (result.isNotEmpty()) {
            when (entry.uploadServer) {
                UploadServerType.UPLOAD_TO_TASK, UploadServerType.UPLOAD_TO_PROCESS -> {
                    result.forEach {
                        val fileLength = DocumentFile.fromSingleUri(context, it)?.length() ?: 0L
                        if (GetMultipleContents.isFileSizeExceed(fileLength)) {
                            throw CancellationException(ERROR_FILE_SIZE_EXCEED)
                        }
                    }
                }

                else -> {}
            }
            withContext(Dispatchers.IO) {
                result.map {
                    repository.scheduleContentForUpload(
                        context,
                        it,
                        getParentId(entry),
                        uploadServerType = entry.uploadServer,
                        observerId = entry.observerID,
                    )
                }
                repository.setTotalTransferSize(result.size)
            }
        } else {
            throw CancellationException("User Cancellation")
        }
        return entry
    }

    override fun copy(_entry: ParentEntry): Action = copy(entry = _entry as Entry)

    override fun showToast(view: View, anchorView: View?) {
        Action.showToast(view, anchorView, R.string.action_upload_media_toast)
    }

    private companion object {
        val MIME_TYPES = arrayOf("*/*")
    }
}
