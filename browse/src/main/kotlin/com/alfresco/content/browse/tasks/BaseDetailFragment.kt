package com.alfresco.content.browse.tasks

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.alfresco.content.REMOTE
import com.alfresco.content.actions.CreateActionsSheet
import com.alfresco.content.browse.R
import com.alfresco.content.browse.preview.LocalPreviewActivity
import com.alfresco.content.browse.processes.details.ProcessDetailViewState
import com.alfresco.content.browse.tasks.detail.TaskDetailViewState
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.Entry
import com.alfresco.content.data.EventName
import com.alfresco.content.process.ui.fragments.ProcessBaseFragment.Companion.KEY_ENTRY_OBJ
import com.alfresco.content.viewer.ViewerActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
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
        val dialog =
            MaterialAlertDialogBuilder(requireContext())
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
        AnalyticsManager().taskEvent(EventName.UploadTaskAttachment)
        CreateActionsSheet.with(Entry.defaultAPSEntry(state.parent?.id)).show(childFragmentManager, null)
    }

    internal fun showCreateSheet(
        state: ProcessDetailViewState,
        observerID: String,
    ) {
        AnalyticsManager().taskEvent(EventName.UploadProcessAttachment)
        CreateActionsSheet.with(Entry.defaultWorkflowEntry(observerID)).show(childFragmentManager, null)
    }

    /**
     * return the stable id of uploading contents
     */
    fun stableId(entry: Entry): String =
        if (entry.isUpload) {
            entry.boxId.toString()
        } else {
            entry.id
        }

    /**
     * This intent will open the remote file
     */
    fun remoteViewerIntent(entry: Entry) =
        startActivity(
            Intent(requireActivity(), ViewerActivity::class.java)
                .putExtra(ViewerActivity.KEY_ID, entry.id)
                .putExtra(ViewerActivity.KEY_TITLE, entry.name)
                .putExtra(ViewerActivity.KEY_MODE, REMOTE),
        )

    /**
     * This intent will open the local file
     */
    fun localViewerIntent(contentEntry: Entry) =
        startActivity(
            Intent(requireActivity(), LocalPreviewActivity::class.java)
                .putExtra(KEY_ENTRY_OBJ, contentEntry),
        )

    /**
     * showing Snackbar
     */
    fun showSnackar(
        snackView: View,
        message: String,
    ) = Snackbar.make(
        snackView,
        message,
        Snackbar.LENGTH_SHORT,
    ).show()
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
