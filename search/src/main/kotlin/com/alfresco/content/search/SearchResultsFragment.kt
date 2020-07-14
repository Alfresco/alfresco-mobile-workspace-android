package com.alfresco.content.search

import com.airbnb.mvrx.fragmentViewModel
import com.alfresco.content.HideSoftInputOnScrollListener
import com.alfresco.content.data.Entry
import com.alfresco.content.data.SearchFilters
import com.alfresco.content.listview.ListFragment

class SearchResultsFragment : ListFragment<SearchResultsViewModel>() {

    override val viewModel: SearchResultsViewModel by fragmentViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.addOnScrollListener(HideSoftInputOnScrollListener())
    }

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
