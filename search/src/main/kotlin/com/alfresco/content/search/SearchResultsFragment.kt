package com.alfresco.content.search

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.parentFragmentViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.HideSoftInputOnScrollListener
import com.alfresco.content.data.AdvanceSearchFilters
import com.alfresco.content.data.Entry
import com.alfresco.content.data.ParentEntry
import com.alfresco.content.data.SearchFacetData
import com.alfresco.content.data.SearchFilters
import com.alfresco.content.listview.ListFragment
import com.alfresco.content.navigateTo
import com.alfresco.content.navigateToExtensionFolder
import com.alfresco.content.navigateToFolder

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

    /**
     * set the advance filters on the viewModel
     */
    fun setFilters(filters: AdvanceSearchFilters, facetData: SearchFacetData) {
        scrollToTop()
        viewModel.setFilters(filters, facetData)
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
        withState(viewModel) { state ->
            if (!state.isExtension) {
                findNavController().navigateTo(entry)
            } else if (entry.isFolder) {
                if (state.moveId.isNotEmpty()) {
                    val parentId = entry.parentPaths.find { it == state.moveId }
                    if (parentId.isNullOrEmpty())
                        findNavController().navigateToFolder(entry, state.moveId)
                    else Toast.makeText(requireContext(), getString(R.string.search_move_warning), Toast.LENGTH_SHORT).show()
                } else findNavController().navigateToExtensionFolder(entry)
            }
        }
    }

    override fun onEntryCreated(entry: ParentEntry) {
        TODO("Not yet implemented")
    }

    override fun invalidate() {
        super.invalidate()

        withState(viewModel) { state ->
            // Shown only when refining a search
            if (state.isExtension) super.disableRefreshLayout()
            topLoadingIndicator?.isVisible =
                state.request is Loading && state.entries.isNotEmpty() && !refreshLayout.isRefreshing
        }
    }
}
