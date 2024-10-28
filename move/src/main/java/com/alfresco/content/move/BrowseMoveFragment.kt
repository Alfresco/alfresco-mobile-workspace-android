package com.alfresco.content.move

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.InternalMavericksApi
import com.airbnb.mvrx.withState
import com.alfresco.content.actions.MoveResultContract
import com.alfresco.content.browse.BrowseArgs
import com.alfresco.content.browse.BrowseViewModel
import com.alfresco.content.browse.BrowseViewState
import com.alfresco.content.data.Entry
import com.alfresco.content.data.ParentEntry
import com.alfresco.content.fragmentViewModelWithArgs
import com.alfresco.content.listview.ListFragment
import com.alfresco.content.navigateToContextualSearch
import com.alfresco.content.navigateToFolder
import kotlinx.coroutines.flow.collectLatest

/**
 * Mark as BrowseMoveFragment
 */
class BrowseMoveFragment : ListFragment<BrowseViewModel, BrowseViewState>(R.layout.fragment_move_list) {
    private lateinit var args: BrowseArgs

    @OptIn(InternalMavericksApi::class)
    override val viewModel: BrowseViewModel by fragmentViewModelWithArgs { args }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        args = BrowseArgs.with(requireArguments())

        // Contextual search only in folders/sites
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        if (args.isProcess) {
            moveHereButton?.text = getString(R.string.select_button)
        } else {
            moveHereButton?.text = getString(R.string.move_button)
        }

        moveHereButton?.setOnClickListener {
            withState(viewModel) { state ->
                if (args.isProcess) {
                    state.parent?.let {
                        viewModel.setSearchResult(it)
                    }
                    requireActivity().finish()
                } else {
                    val activity = requireActivity()
                    val intent =
                        Intent().apply {
                            putExtra(MoveResultContract.OUTPUT_KEY, state.nodeId)
                        }
                    activity.setResult(Activity.RESULT_OK, intent)
                    activity.finish()
                }
            }
        }

        cancelButton?.setOnClickListener {
            requireActivity().finish()
        }

        lifecycleScope.launchWhenStarted {
            viewModel.sharedFlow.collectLatest { entry ->
                onItemClicked(entry)
            }
        }
    }

    override fun onCreateOptionsMenu(
        menu: Menu,
        inflater: MenuInflater,
    ) {
        if (args.isProcess) {
            inflater.inflate(R.menu.menu_browse_folder, menu)
        } else {
            inflater.inflate(R.menu.menu_browse_extension, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search -> {
                withState(viewModel) { state ->
                    if (args.isProcess) {
                        findNavController().navigateToContextualSearch(args.id ?: "", args.title ?: "", args.isProcess)
                    } else {
                        findNavController().navigateToContextualSearch(args.id ?: "", args.title ?: "", true, state.moveId)
                    }
                }
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

    override fun onEntryCreated(entry: ParentEntry) {
        if (isAdded) {
            onItemClicked(entry as Entry)
        }
    }

    override fun invalidate() =
        withState(viewModel) { state ->
            if (state.path == getString(R.string.nav_path_move)) {
                super.disableRefreshLayout()
            }

            if (state.title != null) {
                bottomMoveButtonLayout?.visibility = View.VISIBLE
            } else {
                bottomMoveButtonLayout?.visibility = View.INVISIBLE
            }
            super.invalidate()
        }

    /**
     * return callback for list item
     */
    override fun onItemClicked(entry: Entry) {
        if (!entry.isFolder) return
        withState(viewModel) { state ->
            findNavController().navigateToFolder(entry, state.moveId, isProcess = args.isProcess)
        }
    }
}
