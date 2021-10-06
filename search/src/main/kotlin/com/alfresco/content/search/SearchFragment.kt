package com.alfresco.content.search

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.ListPopupWindow
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.withState
import com.alfresco.content.data.SearchFilter
import com.alfresco.content.data.and
import com.alfresco.content.data.emptyFilters
import com.alfresco.content.fragmentViewModelWithArgs
import com.alfresco.content.hideSoftInput
import com.alfresco.content.search.databinding.FragmentSearchBinding
import kotlinx.parcelize.Parcelize

@Parcelize
data class ContextualSearchArgs(
    val id: String?,
    val title: String?
) : Parcelable {
    companion object {
        private const val ID_KEY = "id"
        private const val TITLE_KEY = "title"

        fun with(args: Bundle?): ContextualSearchArgs? {
            if (args == null) return null
            return ContextualSearchArgs(
                args.getString(ID_KEY, null),
                args.getString(TITLE_KEY, null)
            )
        }
    }
}

class SearchFragment : Fragment(), MavericksView {

    private val viewModel: SearchViewModel by fragmentViewModelWithArgs {
        ContextualSearchArgs.with(arguments)
    }

    private lateinit var binding: FragmentSearchBinding

    private lateinit var searchView: SearchView

    private val recentsFragment by lazy {
        childFragmentManager.findFragmentById(R.id.recents_fragment) as RecentSearchFragment
    }
    private val resultsFragment by lazy {
        childFragmentManager.findFragmentById(R.id.results_fragment) as SearchResultsFragment
    }

    private lateinit var filterContextual: FilterChip
    private lateinit var filterFiles: FilterChip
    private lateinit var filterFolders: FilterChip
    private lateinit var filterLibraries: FilterChip

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setAdvanceSearchFiltersData()

        setupChips()

        resultsFragment.topLoadingIndicator = view.findViewById(R.id.loading)
        recentsFragment.onEntrySelected = { searchView.setQuery(it, false) }
    }

    private fun setAdvanceSearchFiltersData() {
        withState(viewModel) {
            if (viewModel.isShowAdvanceFilterView(it.listSearchFilters)) {
                binding.rlDropDownSearch.visibility = View.VISIBLE
                binding.chipFolders.visibility = View.GONE
                setupDropDown()
            } else {
                binding.rlDropDownSearch.visibility = View.GONE
                binding.chipFolders.visibility = View.VISIBLE
            }
        }
    }

    override fun invalidate() {
        // No-op.
        // State is read only once on screen setup.
        // This does not include results which are updated in their fragment.
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search, menu)

        val searchItem: MenuItem = menu.findItem(R.id.search)
        searchView = searchItem.actionView as SearchView
        searchView.queryHint = resources.getString(R.string.search_hint)

        // Initial State
        searchItem.expandActionView()
        searchView.setQuery(viewModel.getSearchQuery(), false)
        updateFragmentVisibility(viewModel.getSearchQuery())

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                if (isResumed) {
                    setSearchQuery(newText ?: "")
                }
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                resultsFragment.saveCurrentSearch()
                hideSoftInput()
                return true
            }
        })

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                this@SearchFragment.findNavController().navigateUp()
                return true
            }
        })
    }

    fun setSearchQuery(query: String) {
        val terms = cleanupSearchQuery(query)
        // Always update search query in internal state.
        // This avoids extra network requests caused by modifying filters after input clear.
        resultsFragment.setSearchQuery(terms)

        updateFragmentVisibility(terms)
    }

    private fun updateFragmentVisibility(terms: String) {
        if (terms.length >= SearchViewModel.MIN_QUERY_LENGTH) {
            binding.recentsFragment.visibility = View.GONE
            binding.resultsFragment.visibility = View.VISIBLE
        } else {
            binding.recentsFragment.visibility = View.VISIBLE
            binding.resultsFragment.visibility = View.GONE
            recentsFragment.scrollToTop()
        }
    }

    private fun cleanupSearchQuery(query: String): String {
        return query.replace("\\s+".toRegex(), " ").trim()
    }

    private fun setupDropDown() {
        val searchFilterPopup = ListPopupWindow(requireContext(), null, R.attr.listPopupWindowStyle)

        searchFilterPopup.anchorView = binding.rlDropDownSearch
        searchFilterPopup.setListSelector(ContextCompat.getDrawable(requireContext(), R.drawable.bg_pop_up_window))

        val items = mutableListOf<String?>()
        val searchFilters = viewModel.getSearchFilterList()
        searchFilters?.forEach { item ->
            items.add(item.name)
        }
        val adapter = ArrayAdapter(requireContext(), R.layout.list_search_filter_pop_up, items)
        searchFilterPopup.setAdapter(adapter)

        withState(viewModel) {
            viewModel.getDefaultSearchFilterName(it.listSearchFilters)?.let { name ->
                binding.dropDownAdvanceSearch.text = name
            }
        }

        searchFilterPopup.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            setSelectedFilterData(position)
            searchFilterPopup.dismiss()
        }

        binding.rlDropDownSearch.setOnClickListener { _: View? -> searchFilterPopup.show() }
    }

    private fun setSelectedFilterData(position: Int) {
        withState(viewModel) {
            viewModel.getSelectedFilter(position, it)?.let { searchItem ->
                binding.dropDownAdvanceSearch.text = searchItem.name
            }
        }
    }

    private fun setupChips() {
        filterContextual = requireView().findViewById(R.id.chip_contextual)
        filterFiles = requireView().findViewById(R.id.chip_files)
        filterFolders = requireView().findViewById(R.id.chip_folders)
        filterLibraries = requireView().findViewById(R.id.chip_libraries)

        // Initial State
        withState(viewModel) { state ->
            filterContextual.text = getString(R.string.search_chip_contextual, state.contextTitle)

            if (state.filters.contains(SearchFilter.Contextual)) {
                filterContextual.visibility = View.VISIBLE
                filterLibraries.visibility = View.GONE
                filterContextual.isChecked = true
            }

            if (state.filters.contains(SearchFilter.Files)) {
                filterFiles.isChecked = true
            }

            if (state.filters.contains(SearchFilter.Folders)) {
                filterFolders.isChecked = true
            }

            if (state.filters.contains(SearchFilter.Libraries)) {
                filterLibraries.isChecked = true
            }
        }

        // Bind state change listeners
        filterContextual.setOnCheckedChangeListener { _, _ ->
            applyFilters()
        }

        filterFiles.setOnCheckedChangeListener { _, _ ->
            filterLibraries.uncheck(false)
            applyFilters()
        }

        filterFolders.setOnCheckedChangeListener { _, _ ->
            filterLibraries.uncheck(false)
            applyFilters()
        }

        filterLibraries.setOnCheckedChangeListener { _, _ ->
            filterFiles.uncheck(false)
            filterFolders.uncheck(false)
            applyFilters()
        }
    }

    private fun applyFilters() {
        var filter = emptyFilters()
        if (filterContextual.isChecked) {
            filter = filter and SearchFilter.Contextual
        }
        if (filterFiles.isChecked) {
            filter = filter and SearchFilter.Files
        }
        if (filterFolders.isChecked) {
            filter = filter and SearchFilter.Folders
        }
        if (filterLibraries.isChecked) {
            filter = filter and SearchFilter.Libraries
        }
        resultsFragment.setFilters(filter)
    }
}
