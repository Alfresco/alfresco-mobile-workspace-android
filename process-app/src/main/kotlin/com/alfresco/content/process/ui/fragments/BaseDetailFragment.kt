package com.alfresco.content.process.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.alfresco.content.REMOTE
import com.alfresco.content.actions.CreateActionsSheet
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.Entry
import com.alfresco.content.data.EventName
import com.alfresco.content.viewer.ViewerActivity
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

    internal fun showCreateSheet(state: FormViewState, observerID: String) {
        AnalyticsManager().taskEvent(EventName.UploadProcessAttachment)
        CreateActionsSheet.with(Entry.defaultWorkflowEntry(observerID)).show(childFragmentManager, null)
    }

    /**
     * return the stable id of uploading contents
     */
    fun stableId(entry: Entry): String =
        if (entry.isUpload) {
            entry.boxId.toString()
        } else entry.id

    /**
     * This intent will open the remote file
     */
    fun remoteViewerIntent(entry: Entry) = startActivity(
        Intent(requireActivity(), ViewerActivity::class.java)
            .putExtra(ViewerActivity.KEY_ID, entry.id)
            .putExtra(ViewerActivity.KEY_TITLE, entry.name)
            .putExtra(ViewerActivity.KEY_MODE, REMOTE),
    )

    /**
     * This intent will open the local file
     */
    fun localViewerIntent(contentEntry: Entry) {
        val intent = Intent(
            requireActivity(),
            Class.forName("com.alfresco.content.browse.preview.LocalPreviewActivity"),
        )
        intent.putExtra(KEY_ENTRY_OBJ, contentEntry)
        startActivity(intent)
    }

    /**
     * showing Snackbar
     */
    fun showSnackar(snackView: View, message: String) = Snackbar.make(
        snackView,
        message,
        Snackbar.LENGTH_SHORT,
    ).show()

    companion object {
        const val KEY_ENTRY_OBJ = "entryObj"
    }
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
