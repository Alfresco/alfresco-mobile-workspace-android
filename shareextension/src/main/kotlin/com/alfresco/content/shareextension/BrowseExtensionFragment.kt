package com.alfresco.content.shareextension

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import com.alfresco.content.browse.BrowseArgs
import com.alfresco.content.browse.BrowseViewModel
import com.alfresco.content.browse.BrowseViewState
import com.alfresco.content.browse.R
import com.alfresco.content.data.Entry
import com.alfresco.content.fragmentViewModelWithArgs
import com.alfresco.content.listview.ListFragment

/**
 * Mark as BrowseExtensionFragment
 */
class BrowseExtensionFragment : ListFragment<BrowseViewModel, BrowseViewState>(R.layout.fragment_extension_list) {

    private lateinit var args: BrowseArgs
    override val viewModel: BrowseViewModel by fragmentViewModelWithArgs { args }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        args = BrowseArgs.with(requireArguments())

        // Contextual search only in folders/sites
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_browse, menu)
    }

    override fun invalidate() {
        super.invalidate()
    }

    /**
     * return callback for list item
     */
    override fun onItemClicked(entry: Entry) {
        println("BrowseExtensionFragment.onItemClicked")
    }
}
