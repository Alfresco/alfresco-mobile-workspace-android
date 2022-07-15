package com.alfresco.content.listview.tasks

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.epoxy.EpoxyRecyclerView
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.data.ResponseList
import com.alfresco.content.data.TaskEntry
import com.alfresco.content.listview.R
import com.alfresco.content.listview.listViewGroupHeader
import com.alfresco.content.listview.listViewMessage
import com.alfresco.content.listview.listViewPageBoundary
import com.alfresco.content.listview.listViewPageLoading
import com.alfresco.content.simpleController


/**
 * Mark as TaskListViewState interface
 */
interface TaskListViewState : MavericksState {
    val taskEntries: List<TaskEntry>
    val hasMoreItems: Boolean
    val request: Async<ResponseList>
    val isCompact: Boolean

    /**
     * copy the task entries and update the state
     */
    fun copy(_entries: List<TaskEntry>): TaskListViewState
}

/**
 * Mark as TaskListViewModel class
 */
abstract class TaskListViewModel<S : TaskListViewState>(
    initialState: S
) : MavericksViewModel<S>(initialState) {

    /**
     * it executes on pull to refresh
     */
    abstract fun refresh()

    /**
     * it executes when loads pagination data
     */
    abstract fun fetchNextPage()

    /**
     * it executes when no data found and api returns failure
     */
    abstract fun emptyMessageArgs(state: TaskListViewState): Triple<Int, Int, Int>
}

/**
 * Mark as TaskListFragment class
 */
abstract class TaskListFragment<VM : TaskListViewModel<S>, S : TaskListViewState>(layoutID: Int = R.layout.fragment_task_list) :
    Fragment(layoutID), MavericksView {
    abstract val viewModel: VM

    lateinit var loadingAnimation: View
    lateinit var recyclerView: EpoxyRecyclerView
    lateinit var refreshLayout: SwipeRefreshLayout
    lateinit var loadingMessage: TextView
    private val epoxyController: AsyncEpoxyController by lazy { epoxyController() }
    private var delayedBoundary: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingAnimation = view.findViewById(R.id.loading_animation)
        recyclerView = view.findViewById(R.id.recycler_view)
        refreshLayout = view.findViewById(R.id.refresh_layout)
        loadingMessage = view.findViewById(R.id.loading_message)

        refreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }
        recyclerView.setController(epoxyController)

        epoxyController.adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0) {
                    // @see: https://github.com/airbnb/epoxy/issues/224
                    recyclerView.layoutManager?.scrollToPosition(0)
                }
            }
        })
    }

    override fun invalidate() = withState(viewModel) { state ->

        loadingAnimation.isVisible =
            state.request is Loading && state.taskEntries.isEmpty() && !refreshLayout.isRefreshing

        if (state.request.complete) {
            refreshLayout.isRefreshing = false
        }
        epoxyController.requestModelBuild()
    }

    private fun epoxyController() = simpleController(viewModel) { state ->
        if (state.taskEntries.isEmpty() && state.request.complete) {
            val args = viewModel.emptyMessageArgs(state)
            listViewMessage {
                id("empty_message")
                iconRes(args.first)
                title(args.second)
                message(args.third)
            }
        } else if (state.taskEntries.isNotEmpty()) {
            state.taskEntries.forEach {
                if (it.type == TaskEntry.Type.GROUP) {
                    listViewGroupHeader {
                        id(it.name)
                        title(it.name)
                    }
                } else {
                    listViewTaskRow {
                        id(it.id)
                        data(it)
                        compact(state.isCompact)
                    }
                }
            }
        }

        if (state.hasMoreItems) {
            if (state.request is Loading) {
                listViewPageLoading {
                    id("loading at ${state.taskEntries.size}")
                }
            } else {
                // On failure delay creating the boundary so that the list scrolls up
                // and the user has to scroll back down to activate a retry
                val isFail = state.request is Fail
                if (isFail && !delayedBoundary) {
                    delayedBoundary = true
                    Handler(Looper.getMainLooper()).postDelayed({ this.requestModelBuild() }, 300)
                } else {
                    if (isFail) {
                        delayedBoundary = false
                    }
                    listViewPageBoundary {
                        id("boundary at ${state.taskEntries.size}")
                        onBind { _, _, _ -> viewModel.fetchNextPage() }
                    }
                }
            }
        }
    }
}
