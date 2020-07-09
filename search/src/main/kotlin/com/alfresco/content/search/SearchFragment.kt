package com.alfresco.content.search

import com.airbnb.mvrx.fragmentViewModel
import com.alfresco.content.data.Entry
import com.alfresco.content.listview.ListFragment

class SearchFragment : ListFragment<SearchViewModel>() {

    override val viewModel: SearchViewModel by fragmentViewModel()

    fun setSearchQuery(query: String) {
        viewModel.setSearchQuery(query)
    }

    override fun onItemClicked(entry: Entry) {
        TODO("Not yet implemented")
    }
}
