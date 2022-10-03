package com.alfresco.content.browse.tasks

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.alfresco.content.browse.R
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.ContentEntry
import com.alfresco.content.data.EventName
import com.alfresco.content.mimetype.MimeType
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
    fun deleteContentPrompt(contentEntry: ContentEntry) {
        AnalyticsManager().taskEvent(EventName.DeleteTaskAttachment)
        val oldDialog = deleteContentDialog.get()
        if (oldDialog != null && oldDialog.isShowing) return
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.dialog_title_delete_content))
            .setMessage(contentEntry.name)
            .setIcon(
                ResourcesCompat.getDrawable(
                    requireContext().resources,
                    MimeType.with(contentEntry.mimeType).icon,
                    requireContext().theme
                )
            )
            .setNegativeButton(getString(R.string.dialog_negative_button_task), null)
            .setPositiveButton(getString(R.string.dialog_positive_button_task)) { _, _ ->
                listener.onConfirmDelete(contentEntry.id.toString())
            }
            .show()
        deleteContentDialog = WeakReference(dialog)
    }
}

/**
 * Marked as DeleteContentListener interface
 */
interface DeleteContentListener {
    fun onConfirmDelete(contentId: String)
}
