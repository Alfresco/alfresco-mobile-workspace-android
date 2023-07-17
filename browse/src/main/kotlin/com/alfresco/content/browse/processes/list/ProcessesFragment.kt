package com.alfresco.content.browse.processes.list

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.ListPopupWindow
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.setMargins
import com.airbnb.mvrx.Mavericks
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.browse.R
import com.alfresco.content.browse.processes.ProcessDetailActivity
import com.alfresco.content.browse.processes.sheet.ProcessDefinitionsSheet
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.PageView
import com.alfresco.content.data.ProcessEntry
import com.alfresco.content.getLocalizedName
import com.alfresco.content.listview.processes.ProcessListFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

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
        filterTitle.text = requireContext().getLocalizedName(viewModel.filterName)
        rlFilters.contentDescription = getString(R.string.text_filter_option, viewModel.filterName)
        scrollToTop()
        if (state.request is Success) {
            clParent.addView(makeFab(requireContext()))
        }
    }

    private fun scrollToTop() {
        if (isResumed && viewModel.scrollToTop) {
            recyclerView.layoutManager?.scrollToPosition(0)
            viewModel.scrollToTop = false
        }
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

    private fun makeFab(context: Context) =
        FloatingActionButton(context).apply {
            layoutParams = CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.WRAP_CONTENT,
                CoordinatorLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                // TODO: define margins
                setMargins(
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics)
                        .toInt(),
                )
            }
            id = R.id.fab_create_task
            contentDescription = context.getString(R.string.title_start_workflow)
            setImageResource(R.drawable.ic_add_fab)
            setOnClickListener {
                ProcessDefinitionsSheet.with().show(parentFragmentManager, null)
            }
        }

    override fun onItemClicked(entry: ProcessEntry) {
        startActivity(
            Intent(requireActivity(), ProcessDetailActivity::class.java)
                .putExtra(Mavericks.KEY_ARG, entry),
        )
    }
}
