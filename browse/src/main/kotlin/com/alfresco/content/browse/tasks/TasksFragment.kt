package com.alfresco.content.browse.tasks

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.browse.R
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.PageView
import com.alfresco.content.listview.tasks.TaskListFragment
import com.alfresco.content.simpleController

/**
 * Marked as TasksFragment
 */
class TasksFragment : TaskListFragment<TasksViewModel, TasksViewState>() {

    override val viewModel: TasksViewModel by fragmentViewModel()
    private val epoxyControllerFilters: AsyncEpoxyController by lazy { epoxyControllerFilters() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        visibleFilters(true)
        setupDropDown()
        recyclerViewSort.setController(epoxyControllerFilters)
    }

    override fun onResume() {
        super.onResume()
        AnalyticsManager().screenViewEvent(PageView.Tasks)
    }

    override fun invalidate() = withState(viewModel) { state ->
        super.invalidate()
        textStateFilter.text = state.displayTask.name
        epoxyControllerFilters.requestModelBuild()
    }

    private fun setupDropDown() {
        val statePopup = ListPopupWindow(requireContext(), null, R.attr.listPopupWindowStyle)

        statePopup.anchorView = rlStateFilters
        statePopup.setListSelector(ContextCompat.getDrawable(requireContext(), R.drawable.bg_pop_up_window))
        statePopup.isModal = true

        withState(viewModel) { state ->
            val items = state.listTaskState.map { it.name }
            val adapter = ArrayAdapter(requireContext(), R.layout.list_task_state_pop_up, items)
            statePopup.setAdapter(adapter)
        }

        statePopup.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            viewModel.updateState(position)
            statePopup.dismiss()
        }
        rlStateFilters.setOnClickListener { statePopup.show() }
    }

    private fun epoxyControllerFilters() = simpleController(viewModel) { state ->
        state.listSortDataChips.forEach { sortDataObj ->
            listViewSortChips {
                id(sortDataObj.title)
                data(sortDataObj)
                clickListener { model, _, chipView, _ ->
                    withState(viewModel) { state ->
                        viewModel.updateSortSelection(state, model.data())
                    }
                }
            }
        }
    }
}
