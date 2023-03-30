package com.alfresco.content.browse.processes.list

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.content.ContextCompat
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.browse.R
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.PageView
import com.alfresco.content.data.ProcessEntry
import com.alfresco.content.getLocalizedName
import com.alfresco.content.listview.processes.ProcessListFragment

/**
 * Marked as ProcessesFragment
 */
class ProcessesFragment : ProcessListFragment<ProcessesViewModel, ProcessesViewState>() {

    override val viewModel: ProcessesViewModel by fragmentViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDropDown()
    }

    override fun onResume() {
        super.onResume()
        AnalyticsManager().screenViewEvent(PageView.Workflows)
    }

    override fun invalidate() = withState(viewModel) { state ->
        super.invalidate()
        filterTitle.text = viewModel.filterName
    }

    private fun setupDropDown() {
        val filterPopup = ListPopupWindow(requireContext(), null, R.attr.listPopupWindowStyle)

        filterPopup.anchorView = rlFilters
        filterPopup.setListSelector(ContextCompat.getDrawable(requireContext(), R.drawable.bg_pop_up_window))
        filterPopup.isModal = true

        val items = mutableListOf<String?>()
        viewModel.listProcesses.forEach { item ->
            items.add(requireContext().getLocalizedName(item.key))
        }
        val adapter = ArrayAdapter(requireContext(), R.layout.list_filter_pop_up, items)
        filterPopup.setAdapter(adapter)

        filterPopup.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            withState(viewModel) { state ->
                viewModel.applyFilters(items[position])
                filterPopup.dismiss()
            }
        }

        rlFilters.setOnClickListener { filterPopup.show() }
    }

    override fun onItemClicked(entry: ProcessEntry) {
    }
}
