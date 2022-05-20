package com.alfresco.content.shareextension

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.InternalMavericksApi
import com.airbnb.mvrx.withState
import com.alfresco.content.browse.BrowseArgs
import com.alfresco.content.browse.BrowseViewModel
import com.alfresco.content.browse.BrowseViewState
import com.alfresco.content.data.Entry
import com.alfresco.content.fragmentViewModelWithArgs
import com.alfresco.content.listview.ListFragment
import com.alfresco.content.navigateTo
import com.alfresco.content.navigateToContextualSearch

/**
 * Mark as BrowseExtensionFragment
 */
class BrowseExtensionFragment : ListFragment<BrowseViewModel, BrowseViewState>(R.layout.fragment_extension_list) {

    private lateinit var args: BrowseArgs
    @OptIn(InternalMavericksApi::class)
    override val viewModel: BrowseViewModel by fragmentViewModelWithArgs { args }

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

        cancelButton?.setOnClickListener {
            requireActivity().finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_browse_extension, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search -> {
                findNavController().navigateToContextualSearch(args.id ?: "", args.title ?: "", true)
                true
            }
            R.id.new_folder -> {
                withState(viewModel) {
                    viewModel.createFolder(it)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onFolderCreated(entry: Entry) {
        onItemClicked(entry)
    }

    override fun invalidate() = withState(viewModel) { state ->
        if (state.path == getString(com.alfresco.content.browse.R.string.nav_path_extension))
            super.disableRefreshLayout()
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
