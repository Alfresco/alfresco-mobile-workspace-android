package com.alfresco.content.shareextension

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.withState
import com.alfresco.content.browse.BrowseArgs
import com.alfresco.content.browse.BrowseViewModel
import com.alfresco.content.browse.BrowseViewState
import com.alfresco.content.data.Entry
import com.alfresco.content.fragmentViewModelWithArgs
import com.alfresco.content.listview.ListFragment
import com.alfresco.content.navigateTo
import com.alfresco.content.navigateToContextualSearch
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.ref.WeakReference

/**
 * Mark as BrowseExtensionFragment
 */
class BrowseExtensionFragment : ListFragment<BrowseViewModel, BrowseViewState>(R.layout.fragment_extension_list) {

    private lateinit var args: BrowseArgs
    override val viewModel: BrowseViewModel by fragmentViewModelWithArgs { args }
    private var uploadQueueDialog = WeakReference<AlertDialog>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        args = BrowseArgs.with(requireArguments())

        // Contextual search only in folders/sites
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uploadButton?.setOnClickListener {
            withState(viewModel) { state ->
                viewModel.uploadFiles(state)
            }
        }

        viewModel.onUploadQueue = {
            showUploadQueuePrompt()
        }
    }

    private fun showUploadQueuePrompt() {
        val oldDialog = uploadQueueDialog.get()
        if (oldDialog != null && oldDialog.isShowing) return
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.upload_queue_title))
            .setMessage(resources.getString(R.string.upload_queue_subtitle))
            .setPositiveButton(resources.getString(R.string.upload_queue_ok_button)) { _, _ ->
                requireActivity().finish()
            }
            .show()
        uploadQueueDialog = WeakReference(dialog)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_browse, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search -> {
                findNavController().navigateToContextualSearch(args.id ?: "", args.title ?: "", true)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun invalidate() {
        super.invalidate()
    }

    /**
     * return callback for list item
     */
    override fun onItemClicked(entry: Entry) {
        if (!entry.isFolder) return

        findNavController().navigateTo(entry)
    }
}
