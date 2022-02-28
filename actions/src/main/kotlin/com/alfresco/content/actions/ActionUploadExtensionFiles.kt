package com.alfresco.content.actions

import android.content.Context
import android.net.Uri
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.alfresco.content.data.Entry
import com.alfresco.content.data.OfflineRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Marked as ActionUploadFiles for uploading the files
 */
data class ActionUploadExtensionFiles(
    override var entry: Entry,
    override val title: Int = R.string.action_upload_files_title
) : ActionExtension {

    private val repository = OfflineRepository()

    override suspend fun execute(context: Context, list: List<Uri>): Entry {

        if (!list.isNullOrEmpty()) {
            withContext(Dispatchers.IO) {
                list.map {
                    repository.scheduleContentForUpload(context, it, entry.id)
                }
            }
        } else {
            throw CancellationException("User Cancellation")
        }
        return entry
    }

    override fun copy(_entry: Entry): ActionExtension = copy(entry = _entry)

    override fun showToast(view: View, anchorView: View?) {
        MaterialAlertDialogBuilder(view.context)
            .setTitle(view.resources.getString(R.string.action_upload_queue_title))
            .setMessage(view.resources.getString(R.string.action_upload_queue_subtitle))
            .setPositiveButton(view.resources.getString(R.string.action_upload_queue_ok_button)) { _, _ ->
                (view.context as AppCompatActivity).finish()
            }
            .show()
    }
}
