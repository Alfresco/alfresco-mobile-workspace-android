package com.alfresco.content.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_search.chip_files
import kotlinx.android.synthetic.main.fragment_search.chip_folders
import kotlinx.android.synthetic.main.fragment_search.chip_libraries
import kotlinx.android.synthetic.main.fragment_search.recents_fragment
import kotlinx.android.synthetic.main.fragment_search.results_fragment

class SearchFragment : Fragment() {

    private val recentsFragment by lazy {
        childFragmentManager.findFragmentById(R.id.recents_fragment) as RecentSearchFragment
    }
    private val resultsFragment by lazy {
        childFragmentManager.findFragmentById(R.id.results_fragment) as SearchResultsFragment
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupChips()
    }

    fun setSearchQuery(query: String) {
        if (query.trim().length > 2) {
            recents_fragment.visibility = View.GONE
            results_fragment.visibility = View.VISIBLE
            resultsFragment.setSearchQuery(query)
        } else {
            recents_fragment.visibility = View.VISIBLE
            results_fragment.visibility = View.GONE
        }
    }

    private fun setupChips() {
        // Initial State
        chip_files.isChecked = true
        chip_folders.isChecked = true

        listOf(chip_files, chip_folders, chip_libraries).forEach { it.setOnCheckedChangeListener { _, _ ->
            updateFilter()
        } }
    }

    private fun updateFilter() {
        var filter = emptyFilters()
        if (chip_files.isChecked) filter = filter and SearchFilter.Files
        if (chip_folders.isChecked) filter = filter and SearchFilter.Folders
        if (chip_libraries.isChecked) filter = filter and SearchFilter.Libraries
        resultsFragment.setFilters(filter)
    }
}
