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
import com.airbnb.mvrx.withState
import com.alfresco.content.actions.MoveResultContract
import com.alfresco.content.browse.BrowseArgs
import com.alfresco.content.browse.BrowseViewModel
import com.alfresco.content.browse.BrowseViewState
import com.alfresco.content.data.Entry
import com.alfresco.content.fragmentViewModelWithArgs
import com.alfresco.content.listview.ListFragment
import com.alfresco.content.listview.NavigateFolderData
import com.alfresco.content.navigateToContextualSearch
import com.alfresco.content.navigateToFolder
import com.alfresco.events.on

/**
 * Mark as BrowseMoveFragment
 */
class BrowseMoveFragment : ListFragment<BrowseViewModel, BrowseViewState>(R.layout.fragment_move_list) {

    private lateinit var args: BrowseArgs
    override val viewModel: BrowseViewModel by fragmentViewModelWithArgs { args }
    private var isNavigate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        args = BrowseArgs.with(requireArguments())

        // Contextual search only in folders/sites
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        moveHereButton?.setOnClickListener {
            withState(viewModel) { state ->
                val activity = requireActivity()
                val intent = Intent().apply {
                    putExtra(MoveResultContract.OUTPUT_KEY, state.nodeId)
                }
                activity.setResult(Activity.RESULT_OK, intent)
                activity.finish()
            }
        }

        cancelButton?.setOnClickListener {
            requireActivity().finish()
        }

        lifecycleScope.on<NavigateFolderData> {
            if (!isNavigate) {
                println("BrowseMoveFragment.onViewCreated navigate")
                isNavigate = true
                onItemClicked(it.entry)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_browse_extension, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search -> {
                withState(viewModel) { state ->
                    findNavController().navigateToContextualSearch(args.id ?: "", args.title ?: "", true, state.moveId)
                }
                true
            }
            R.id.new_folder -> {
                withState(viewModel) {
                    isNavigate = false
                    viewModel.createFolder(it)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun invalidate() = withState(viewModel) { state ->
        if (state.path == getString(R.string.nav_path_move))
            super.disableRefreshLayout()
        super.invalidate()
    }

    /**
     * return callback for list item
     */
    override fun onItemClicked(entry: Entry) {
        if (!entry.isFolder) return
        withState(viewModel) { state ->
            findNavController().navigateToFolder(entry, state.moveId)
        }
    }
}
