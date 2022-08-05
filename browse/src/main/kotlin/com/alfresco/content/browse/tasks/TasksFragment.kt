package com.alfresco.content.browse.tasks

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.browse.R
import com.alfresco.content.component.ComponentBuilder
import com.alfresco.content.component.ComponentData
import com.alfresco.content.component.ComponentMetaData
import com.alfresco.content.component.FilterChip
import com.alfresco.content.component.listViewSortChips
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.EventName
import com.alfresco.content.data.PageView
import com.alfresco.content.data.TaskFilterData
import com.alfresco.content.hideSoftInput
import com.alfresco.content.listview.tasks.TaskListFragment
import com.alfresco.content.simpleController
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Marked as TasksFragment
 */
class TasksFragment : TaskListFragment<TasksViewModel, TasksViewState>() {

    override val viewModel: TasksViewModel by fragmentViewModel()
    private val epoxyControllerFilters: AsyncEpoxyController by lazy { epoxyControllerFilters() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        visibleFilters(true)
        actionReset.setOnClickListener {
            AnalyticsManager().taskFiltersEvent(EventName.TaskFilterReset.value)
            resetAllFilters()
        }
        recyclerViewFilters.setController(epoxyControllerFilters)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_tasks, menu)
    }

    private fun resetAllFilters() = withState(viewModel) { state ->
        val listReset = viewModel.resetChips(state)
        viewModel.applyFilters(listReset)
    }

    override fun onResume() {
        super.onResume()
        AnalyticsManager().screenViewEvent(PageView.Tasks)
    }

    private fun scrollToTop() {
        if (isResumed) {
            recyclerView.layoutManager?.scrollToPosition(0)
        }
    }

    override fun invalidate() = withState(viewModel) { state ->
        super.invalidate()
        epoxyControllerFilters.requestModelBuild()
        if (state.page == 0)
            scrollToTop()
    }

    private fun epoxyControllerFilters() = simpleController(viewModel) { state ->
        state.listSortDataChips.forEach { sortDataObj ->
            listViewSortChips {
                id(sortDataObj.name)
                data(sortDataObj)
                clickListener { model, _, chipView, _ ->

                    onChipClicked(model.data(), chipView)
                }
            }
        }
    }

    private fun onChipClicked(data: TaskFilterData, chipView: View) {
        hideSoftInput()
        if (recyclerViewFilters.isEnabled) {
            AnalyticsManager().taskFiltersEvent(data.name ?: "")
            recyclerViewFilters.isEnabled = false
            withState(viewModel) { state ->
                viewModel.updateSelected(state, data, true)
                if (data.selectedName.isNotEmpty()) {
                    (chipView as FilterChip).isChecked = true
                }
                viewLifecycleOwner.lifecycleScope.launch {

                    val result = showFilterSheetDialog(requireContext(), data)
                    recyclerViewFilters.isEnabled = true
                    if (result != null) {
                        val list = viewModel.updateChipFilterResult(state, data, result)
                        viewModel.applyFilters(list)
                    } else {
                        val isSelected = data.selectedName.isNotEmpty()
                        viewModel.updateSelected(state, data, isSelected)
                    }
                }
            }
        } else (chipView as FilterChip).isChecked = false
    }

    private suspend fun showFilterSheetDialog(
        context: Context,
        taskFilterData: TaskFilterData
    ) = withContext(Dispatchers.Main) {
        suspendCoroutine {
            ComponentBuilder(context, ComponentData.with(taskFilterData))
                .onApply { name, query, queryMap ->
                    executeContinuation(it, name, query, queryMap)
                }
                .onReset { name, query, queryMap ->
                    executeContinuation(it, name, query, queryMap)
                }
                .onCancel {
                    it.resume(null)
                }
                .show()
        }
    }

    private fun executeContinuation(continuation: Continuation<ComponentMetaData?>, name: String, query: String, queryMap: Map<String, String>) {
        continuation.resume(ComponentMetaData(name = name, query = query, queryMap = queryMap))
    }
}
