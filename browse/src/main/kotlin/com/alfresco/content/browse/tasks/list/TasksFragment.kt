package com.alfresco.content.browse.tasks.list

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.setMargins
import androidx.lifecycle.lifecycleScope
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.mvrx.Mavericks
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.actions.ActionCreateTask
import com.alfresco.content.browse.R
import com.alfresco.content.browse.processes.ProcessDetailActivity
import com.alfresco.content.component.ComponentBuilder
import com.alfresco.content.component.ComponentData
import com.alfresco.content.component.ComponentMetaData
import com.alfresco.content.component.FilterChip
import com.alfresco.content.component.listViewSortChips
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.EventName
import com.alfresco.content.data.PageView
import com.alfresco.content.data.ParentEntry
import com.alfresco.content.data.ProcessEntry
import com.alfresco.content.data.TaskEntry
import com.alfresco.content.data.TaskFilterData
import com.alfresco.content.hideSoftInput
import com.alfresco.content.listview.tasks.TaskListFragment
import com.alfresco.content.simpleController
import com.alfresco.ui.getDrawableForAttribute
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Marked as TasksFragment
 */
class TasksFragment : TaskListFragment<TasksViewModel, TasksViewState>() {

    override val viewModel: TasksViewModel by fragmentViewModel()
    private val epoxyControllerFilters: AsyncEpoxyController by lazy { epoxyControllerFilters() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        actionReset.setOnClickListener {
            AnalyticsManager().taskFiltersEvent(EventName.TaskFilterReset.value)
            resetAllFilters()
        }
        if (requireActivity() is ProcessDetailActivity) {
            (requireActivity() as ProcessDetailActivity).setSupportActionBar(toolbar)
            toolbar.apply {
                navigationContentDescription = getString(R.string.label_navigation_back)
                navigationIcon = requireContext().getDrawableForAttribute(R.attr.homeAsUpIndicator)
                setNavigationOnClickListener {
                    withState(viewModel) { _ ->
                        requireActivity().onBackPressed()
                    }
                }
            }
        }
        recyclerViewFilters.setController(epoxyControllerFilters)
    }

    private fun resetAllFilters() = withState(viewModel) { state ->
        val listReset = viewModel.resetChips(state)
        viewModel.applyFilters(listReset)
    }

    override fun onResume() {
        super.onResume()
        AnalyticsManager().screenViewEvent(PageView.Tasks)
    }

    override fun onEntryCreated(entry: ParentEntry) {
        if (isAdded) {
            onItemClicked(entry as TaskEntry)
            resetAllFilters()
        }
    }

    private fun scrollToTop() {
        if (isResumed && viewModel.scrollToTop) {
            recyclerView.layoutManager?.scrollToPosition(0)
            viewModel.scrollToTop = false
        }
    }

    override fun invalidate() = withState(viewModel) { state ->
        super.invalidate()
        epoxyControllerFilters.requestModelBuild()
        scrollToTop()

        if (state.request is Success && !viewModel.isWorkflowTask) {
            clParent.addView(makeFab(requireContext()))
        }
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
        taskFilterData: TaskFilterData,
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
            contentDescription = context.getString(R.string.text_create_task)
            setImageResource(R.drawable.ic_add_fab)
            setOnClickListener {
                val action = ActionCreateTask(TaskEntry())
                viewModel.execute(requireContext(), action)
            }
        }

    private fun executeContinuation(
        continuation: Continuation<ComponentMetaData?>,
        name: String,
        query: String,
        queryMap: Map<String, String>,
    ) = continuation.resume(ComponentMetaData(name = name, query = query, queryMap = queryMap))

    override fun onItemClicked(entry: TaskEntry) {
        val intent = if (entry.processInstanceId != null) {
            Intent(
                requireActivity(),
                Class.forName("com.alfresco.content.app.activity.ProcessActivity"),
            ).apply {
                putExtra(Mavericks.KEY_ARG, ProcessEntry.with(entry))
            }
        } else {
            Intent(
                requireActivity(),
                Class.forName("com.alfresco.content.app.activity.TaskViewerActivity"),
            ).apply {
                putExtra(Mavericks.KEY_ARG, entry)
            }
        }
        startActivity(intent)
    }
}
