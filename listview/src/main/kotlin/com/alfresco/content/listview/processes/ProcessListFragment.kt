package com.alfresco.content.listview.processes

import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
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
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.withState
import com.alfresco.content.data.ProcessEntry
import com.alfresco.content.data.ResponseList
import com.alfresco.content.listview.EntryListener
import com.alfresco.content.listview.R
import com.alfresco.content.listview.listViewMessage
import com.alfresco.content.simpleController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Mark as ProcessListViewState interface
 */
interface ProcessListViewState : MavericksState {
    val processEntries: List<ProcessEntry>
    val hasMoreItems: Boolean
    val request: Async<ResponseList>
    val isCompact: Boolean

    /**
     * copy the task entries and update the state
     */
    fun copy(_entries: List<ProcessEntry>): ProcessListViewState
}

/**
 * Mark as TaskListViewModel class
 */
abstract class ProcessListViewModel<S : ProcessListViewState>(
    initialState: S
) : MavericksViewModel<S>(initialState) {

    private var folderListener: EntryListener? = null

    /**
     * Set the listener to be notified when a new task created and move to task detail screen
     */
    fun setListener(listener: EntryListener) {
        folderListener = listener
    }

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
    abstract fun emptyMessageArgs(state: ProcessListViewState): Triple<Int, Int, Int>
}

/**
 * Mark as TaskListFragment class
 */
abstract class ProcessListFragment<VM : ProcessListViewModel<S>, S : ProcessListViewState>(layoutID: Int = R.layout.fragment_process_list) :
    Fragment(layoutID), MavericksView, EntryListener {
    abstract val viewModel: VM

    lateinit var loadingAnimation: View
    lateinit var recyclerView: EpoxyRecyclerView
    lateinit var refreshLayout: SwipeRefreshLayout
    lateinit var loadingMessage: TextView
    private val epoxyController: AsyncEpoxyController by lazy { epoxyController() }
    private var delayedBoundary: Boolean = false
    lateinit var clParent: CoordinatorLayout
    lateinit var rlFilters: RelativeLayout
    lateinit var filterTitle: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingAnimation = view.findViewById(R.id.loading_animation)
        recyclerView = view.findViewById(R.id.recycler_view)
        loadingMessage = view.findViewById(R.id.loading_message)
        refreshLayout = view.findViewById(R.id.refresh_layout)
        clParent = view.findViewById(R.id.cl_parent)
        rlFilters = view.findViewById(R.id.rl_drop_down_search)
        filterTitle = view.findViewById(R.id.text_filter_title)

        refreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }
        recyclerView.setController(epoxyController)
        viewModel.setListener(this)

        epoxyController.adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0) {
                    // @see: https://github.com/airbnb/epoxy/issues/224
                    recyclerView.layoutManager?.scrollToPosition(0)
                }
            }
        })
    }

    /**
     * It will get executed if user tap on any process.
     */
    abstract fun onItemClicked(entry: ProcessEntry)

    override fun invalidate() = withState(viewModel) { state ->

        loadingAnimation.isVisible =
            state.request is Loading && state.processEntries.isEmpty() && !refreshLayout.isRefreshing

        if (state.request.complete) {
            refreshLayout.isRefreshing = false
        }
        epoxyController.requestModelBuild()
    }

    private fun epoxyController() = simpleController(viewModel) { state ->
        if (state.request.complete && state.request is Fail) {
            visibleFilters(false)
        }
        if (state.processEntries.isEmpty() && state.request.complete) {
            if (state.request is Success) visibleFilters(true)
            val args = viewModel.emptyMessageArgs(state)
            listViewMessage {
                id("empty_message")
                iconRes(args.first)
                title(args.second)
                message(args.third)
            }
        } else if (state.processEntries.isNotEmpty()) {
            visibleFilters(true)
            state.processEntries.forEach {
                listViewProcessRow {
                    id(it.id)
                    data(it)
                    clickListener { model, _, _, _ -> }
                    compact(state.isCompact)
                }
            }
        }
    }

    private fun visibleFilters(isVisible: Boolean) {
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                rlFilters.visibility = if (isVisible) View.VISIBLE else View.GONE
            }
        }
    }
}
