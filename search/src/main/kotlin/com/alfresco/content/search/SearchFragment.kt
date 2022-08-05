package com.alfresco.content.search

import android.content.Context
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.mvrx.InternalMavericksApi
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.withState
import com.alfresco.content.component.ComponentBuilder
import com.alfresco.content.component.ComponentData
import com.alfresco.content.component.ComponentMetaData
import com.alfresco.content.component.FilterChip
import com.alfresco.content.component.listViewFilterChips
import com.alfresco.content.component.models.SearchChipCategory
import com.alfresco.content.data.AdvanceSearchFilter
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.PageView
import com.alfresco.content.data.SearchFacetData
import com.alfresco.content.data.SearchFilter
import com.alfresco.content.data.and
import com.alfresco.content.data.emptyAdvanceFilters
import com.alfresco.content.data.emptyFilters
import com.alfresco.content.fragmentViewModelWithArgs
import com.alfresco.content.getLocalizedName
import com.alfresco.content.hideSoftInput
import com.alfresco.content.search.databinding.FragmentSearchBinding
import com.alfresco.content.simpleController
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize

@Parcelize
data class ContextualSearchArgs(
    val id: String?,
    val title: String?,
    val moveId: String,
    val isExtension: Boolean
) : Parcelable {
    companion object {
        private const val ID_KEY = "id"
        private const val TITLE_KEY = "title"
        private const val EXTENSION_KEY = "extension"
        private const val MOVE_ID_KEY = "moveId"

        fun with(args: Bundle?): ContextualSearchArgs? {
            if (args == null) return null
            return ContextualSearchArgs(
                args.getString(ID_KEY, null),
                args.getString(TITLE_KEY, null),
                args.getString(MOVE_ID_KEY, ""),
                args.getBoolean(EXTENSION_KEY, false)
            )
        }
    }
}

class SearchFragment : Fragment(), MavericksView {

    @OptIn(InternalMavericksApi::class)
    private val viewModel: SearchViewModel by fragmentViewModelWithArgs {
        ContextualSearchArgs.with(arguments)
    }

    private lateinit var binding: FragmentSearchBinding
    private lateinit var searchView: SearchView
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main

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
        AnalyticsManager().screenViewEvent(PageView.Search)
        binding.recyclerViewChips.setController(epoxyController)

        withState(viewModel) { state ->
            if (!state.isExtension) {
                setAdvanceSearchFiltersData()
            } else {
                binding.parentAdvanceSearch.visibility = View.GONE
            }
        }

        resultsFragment.topLoadingIndicator = view.findViewById(R.id.loading)
        recentsFragment.onEntrySelected = { searchView.setQuery(it, false) }
    }

    private fun setAdvanceSearchFiltersData() {
        withState(viewModel) {
            if (viewModel.isShowAdvanceFilterView(it.listSearchFilters)) {
                binding.parentAdvanceSearch.visibility = View.VISIBLE
                binding.chipGroup.visibility = View.GONE
                setupDropDown()
            } else {
                binding.parentAdvanceSearch.visibility = View.GONE
                binding.chipGroup.visibility = View.VISIBLE
                setupChips()
            }
        }
    }

    override fun invalidate() = withState(viewModel) { state ->
        setSearchFilterLocalizedName(state)
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
        searchFilterPopup.isModal = true

        val items = mutableListOf<String?>()
        val searchFilters = viewModel.getSearchFilterList()
        searchFilters?.forEach { item ->
            item.name?.let { name ->
                items.add(requireContext().getLocalizedName(name))
            }
        }
        val adapter = ArrayAdapter(requireContext(), R.layout.list_search_filter_pop_up, items)
        searchFilterPopup.setAdapter(adapter)

        withState(viewModel) { state ->
            var index = state.selectedFilterIndex
            if (state.selectedFilterIndex == -1) {
                index = viewModel.getDefaultSearchFilterIndex(state.listSearchFilters)
                viewModel.copyFilterIndex(index)
            }
            when (searchFilters?.get(index)?.resetButton) {
                true -> binding.actionReset.visibility = View.VISIBLE
                false -> binding.actionReset.visibility = View.GONE
                else -> IllegalArgumentException("Invalid reset button state")
            }
        }

        searchFilterPopup.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            withState(viewModel) { state ->
                if (state.selectedFilterIndex != position) {
                    viewModel.isRefreshSearch = true
                    viewModel.copyFilterIndex(position)
                    setSelectedFilterData()

                    val listSearchChip = mutableListOf<SearchChipCategory>()
                    if (state.isContextual)
                        listSearchChip.add(
                            SearchChipCategory.withContextual(
                                getString(R.string.search_chip_contextual, state.contextTitle),
                                SearchFilter.Contextual
                            )
                        )
                    applyAdvanceFilters(position, listSearchChip)
                }
                searchFilterPopup.dismiss()
            }
        }
        binding.rlDropDownSearch.setOnClickListener { searchFilterPopup.show() }
        binding.actionReset.setOnClickListener { resetAllFilters() }
    }

    private fun resetAllFilters() = withState(viewModel) { state ->
        val listReset = viewModel.resetChips(state)
        scrollToTop()
        applyAdvanceFilters(state.selectedFilterIndex, listReset)
    }

    private fun scrollToTop() {
        if (isResumed) {
            binding.recyclerViewChips.layoutManager?.scrollToPosition(0)
        }
    }

    private fun setSearchFilterLocalizedName(state: SearchResultsState) {
        if (state.selectedFilterIndex != -1)
            viewModel.getDefaultSearchFilterName(state)?.let { name ->
                binding.textSearchFilterTitle.text = requireContext().getLocalizedName(name)
            }
    }

    private fun setSelectedFilterData() {
        withState(viewModel) {
            viewModel.getSelectedFilter(it.selectedFilterIndex, it)?.let { searchItem ->
                searchItem.name?.let { name ->
                    binding.textSearchFilterTitle.text = requireContext().getLocalizedName(name)
                }
            }
        }
    }

    private fun setupChips() {
        filterContextual = requireView().findViewById(R.id.chip)
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

        val filterIndex = state.selectedFilterIndex

        if (filterIndex != -1) {
            val searchChipCategory = state.listSearchCategoryChips?.toMutableList()

            if (!searchChipCategory.isNullOrEmpty()) {

                searchChipCategory.forEach { item ->
                    val modelID = filterIndex.toString() + item.category?.id
                    listViewFilterChips {
                        id(modelID)
                        data(item)
                        clickListener { model, _, chipView, _ ->
                            if (model.data().category?.component != null) {
                                val entry = model.data()
                                AnalyticsManager().searchFacets(entry.category?.component?.selector ?: "")
                                onChipClicked(model.data(), chipView)
                            } else {
                                onContextualChipClicked(model.data(), chipView)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun onContextualChipClicked(data: SearchChipCategory, chipView: View) {
        if (binding.recyclerViewChips.isEnabled) {
            binding.recyclerViewChips.isEnabled = false
            withState(viewModel) { state ->
                val result: ComponentMetaData = if (state.isContextual && (chipView as FilterChip).isChecked)
                    ComponentMetaData(requireContext().getString(R.string.search_chip_contextual, state.contextTitle), SearchFilter.Contextual.name)
                else
                    ComponentMetaData("", "")
                binding.recyclerViewChips.isEnabled = true
                val resultList = viewModel.updateChipComponentResult(state, data, result)
                applyAdvanceFilters(state.selectedFilterIndex, resultList)
            }
        } else (chipView as FilterChip).isChecked = false
    }

    private fun onChipClicked(data: SearchChipCategory, chipView: View) = withState(viewModel) {
        hideSoftInput()
        if (binding.recyclerViewChips.isEnabled) {
            binding.recyclerViewChips.isEnabled = false
            withState(viewModel) { state ->
                viewModel.updateSelected(state, data, true)
                if (data.selectedName.isNotEmpty()) {
                    (chipView as FilterChip).isChecked = true
                }
                viewLifecycleOwner.lifecycleScope.launch {

                    val result = showComponentSheetDialog(requireContext(), data)
                    binding.recyclerViewChips.isEnabled = true
                    if (result != null) {
                        val resultList = viewModel.updateChipComponentResult(state, data, result)
                        applyAdvanceFilters(state.selectedFilterIndex, resultList)
                    } else {
                        val isSelected = data.selectedName.isNotEmpty()
                        viewModel.updateSelected(state, data, isSelected)
                    }
                }
            }
        } else (chipView as FilterChip).isChecked = false
    }

    private suspend fun showComponentSheetDialog(
        context: Context,
        searchChipCategory: SearchChipCategory
    ) = withContext(dispatcher) {
        suspendCoroutine {
            val componentData = if (searchChipCategory.facets == null) ComponentData.with(
                searchChipCategory.category,
                searchChipCategory.selectedName, searchChipCategory.selectedQuery
            )
            else ComponentData.with(
                searchChipCategory.facets,
                searchChipCategory.selectedName, searchChipCategory.selectedQuery
            )
            ComponentBuilder(context, componentData)
                .onApply { name, query, queryMap ->
                    executeContinuation(it, name, query)
                }
                .onReset { name, query, queryMap ->
                    executeContinuation(it, name, query)
                }
                .onCancel {
                    it.resume(null)
                }
                .show()
        }
    }

    private fun executeContinuation(continuation: Continuation<ComponentMetaData?>, name: String, query: String) {
        continuation.resume(ComponentMetaData(name = name, query = query))
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

    private fun applyAdvanceFilters(position: Int, list: MutableList<SearchChipCategory>) {
        val advanceSearchFilter = emptyAdvanceFilters()
        val facetData = SearchFacetData(
            viewModel.defaultFacetFields(position),
            viewModel.defaultFacetQueries(position),
            viewModel.defaultFacetIntervals(position)
        )

        advanceSearchFilter.addAll(viewModel.initAdvanceFilters(position))

        list.forEach { if (it.isSelected) advanceSearchFilter.add(AdvanceSearchFilter(it.selectedQuery, it.selectedName)) }

        resultsFragment.setFilters(advanceSearchFilter, facetData)
    }
}
