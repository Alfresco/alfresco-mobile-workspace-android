package com.alfresco.content.search

import com.airbnb.mvrx.fragmentViewModel
import com.alfresco.content.data.Entry
import com.alfresco.content.listview.ListFragment

class SearchResultsFragment : ListFragment<SearchResultsViewModel>() {

    override val viewModel: SearchResultsViewModel by fragmentViewModel()

    fun setSearchQuery(query: String) {
        viewModel.setSearchQuery(query)
    }

    fun setFilters(filters: SearchFilters) {
        viewModel.setFilters(filters)
    }

    override fun onItemClicked(entry: Entry) {
        viewModel.saveSearch(requireContext())

        // TODO: missing implementation
    }
}
