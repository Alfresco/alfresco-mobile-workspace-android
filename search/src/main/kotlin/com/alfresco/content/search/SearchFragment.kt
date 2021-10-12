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
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.epoxy.EpoxyRecyclerView
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.withState
import com.alfresco.content.data.SearchFilter
import com.alfresco.content.data.and
import com.alfresco.content.data.emptyFilters
import com.alfresco.content.fragmentViewModelWithArgs
import com.alfresco.content.hideSoftInput
import com.alfresco.content.models.CategoriesItem
import com.alfresco.content.search.databinding.FragmentSearchBinding
import com.alfresco.content.simpleController
import kotlinx.parcelize.Parcelize
import kotlin.random.Random

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
    private lateinit var recyclerView: EpoxyRecyclerView

    private val epoxyController: AsyncEpoxyController by lazy { epoxyController() }
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
        epoxyController.requestModelBuild()
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

        val adapter = ArrayAdapter(requireContext(), R.layout.list_search_filter_pop_up, viewModel.getSearchFilterNames())
        val items = mutableListOf<String?>()
        val searchFilters = viewModel.getSearchFilterList()
        searchFilters?.forEach { item ->
            item.name?.let { name ->
                items.add(getLocalizedName(name))
            }
        }
        val adapter = ArrayAdapter(requireContext(), R.layout.list_search_filter_pop_up, items)
        searchFilterPopup.setAdapter(adapter)

        withState(viewModel) {
            viewModel.getDefaultSearchFilterName(it.listSearchFilters)?.let { name ->
                binding.textSearchFilterTitle.text = getLocalizedName(name)
            }
        }

        searchFilterPopup.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            setSelectedFilterData(position)
            searchFilterPopup.dismiss()
        }

        binding.rlDropDownSearch.setOnClickListener { searchFilterPopup.show() }
    }

    private fun getLocalizedName(name: String): String {
        val stringResource = requireContext().resources.getIdentifier(name.lowercase(), "string", requireActivity().packageName)
        return if (stringResource != 0)
            getString(stringResource)
        else
            name
    }

    private fun setSelectedFilterData(position: Int) {
        withState(viewModel) {
            viewModel.getSelectedFilter(position, it)?.let { searchItem ->
                binding.textSearchFilterTitle.text = searchItem.name
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

    private fun epoxyController() = simpleController(viewModel) { state ->

        repeat(5) {
            listViewFilterChips {
                id(Random.nextInt())
                data("dummy")
            }
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
