package com.alfresco.content.browse.tasks

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.alfresco.content.actions.CreateActionsSheet
import com.alfresco.content.browse.R
import com.alfresco.content.browse.tasks.detail.TaskDetailViewState
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.Entry
import com.alfresco.content.data.EventName
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.ref.WeakReference

/**
 * Marked as BaseDetailFragment class
 */
abstract class BaseDetailFragment : Fragment(), DeleteContentListener {

    private var deleteContentDialog = WeakReference<AlertDialog>(null)
    lateinit var listener: DeleteContentListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listener = this
    }

    /**
     * confirmation dialog before deleting the content related to task.
     */
    fun deleteContentPrompt(contentEntry: Entry) {
        AnalyticsManager().taskEvent(EventName.DeleteTaskAttachment)
        val oldDialog = deleteContentDialog.get()
        if (oldDialog != null && oldDialog.isShowing) return
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setCancelable(false)
            .setTitle(getString(R.string.dialog_title_delete_content))
            .setMessage(contentEntry.name)
            .setNegativeButton(getString(R.string.dialog_negative_button_task), null)
            .setPositiveButton(getString(R.string.dialog_positive_button_task)) { _, _ ->
                listener.onConfirmDelete(contentEntry.id.toString())
            }
            .show()
        deleteContentDialog = WeakReference(dialog)
    }

    internal fun showCreateSheet(state: TaskDetailViewState) {
        CreateActionsSheet.with(Entry.defaultAPSEntry(state.parent?.id)).show(childFragmentManager, null)
    }

    /**
     * return the stable id of uploading contents
     */
    fun stableId(entry: Entry): String =
        if (entry.isUpload) entry.boxId.toString()
        else entry.id
}

/**
 * Marked as DeleteContentListener interface
 */
interface DeleteContentListener {

    /**
     * It will get call on confirm delete.
     */
    fun onConfirmDelete(contentId: String)
}
