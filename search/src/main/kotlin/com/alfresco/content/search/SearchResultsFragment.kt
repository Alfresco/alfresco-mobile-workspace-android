package com.alfresco.content.search

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.parentFragmentViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.HideSoftInputOnScrollListener
import com.alfresco.content.data.AdvanceSearchFilters
import com.alfresco.content.data.Entry
import com.alfresco.content.data.SearchFilters
import com.alfresco.content.listview.ListFragment
import com.alfresco.content.navigateTo

class SearchResultsFragment : ListFragment<SearchViewModel, SearchResultsState>() {

    override val viewModel: SearchViewModel by parentFragmentViewModel()
    var topLoadingIndicator: View? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingMessage.setText(R.string.search_loading_message)

        recyclerView.addOnScrollListener(HideSoftInputOnScrollListener())
    }

    fun setSearchQuery(query: String) {
        scrollToTop()
        viewModel.setSearchQuery(query)
    }

    fun setFilters(filters: SearchFilters) {
        scrollToTop()
        viewModel.setFilters(filters)
    }
    fun setFilters(filters: AdvanceSearchFilters) {
        scrollToTop()
        viewModel.setFilters(filters)
    }

    private fun scrollToTop() {
        if (isResumed) {
            recyclerView.layoutManager?.scrollToPosition(0)
        }
    }

    fun saveCurrentSearch() {
        viewModel.saveSearch()
    }

    override fun onItemClicked(entry: Entry) {
        viewModel.saveSearch()

        findNavController().navigateTo(entry)
    }

    override fun invalidate() {
        super.invalidate()

        withState(viewModel) { state ->
            // Shown only when refining a search
            topLoadingIndicator?.isVisible =
                state.request is Loading &&
                state.entries.isNotEmpty() &&
                !refreshLayout.isRefreshing
        }
    }
}
