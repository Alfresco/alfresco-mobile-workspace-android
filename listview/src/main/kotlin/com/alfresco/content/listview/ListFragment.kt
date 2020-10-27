package com.alfresco.content.listview

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.epoxy.EpoxyRecyclerView
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.withState
import com.alfresco.content.MvRxViewModel
import com.alfresco.content.actions.Action
import com.alfresco.content.actions.ActionDelete
import com.alfresco.content.actions.ActionListSheet
import com.alfresco.content.actions.on
import com.alfresco.content.data.Entry
import com.alfresco.content.data.Pagination
import com.alfresco.content.data.ResponsePaging
import com.alfresco.content.simpleController

interface ListViewState : MvRxState {
    val entries: List<Entry>
    val lastPage: Pagination
    val request: Async<ResponsePaging>

    fun copy(_entries: List<Entry>): ListViewState
}

abstract class ListViewModel<S : ListViewState>(
    initialState: S
) : MvRxViewModel<S>(initialState) {

    init {
        viewModelScope.on<ActionDelete> { action ->
            setState { copy(entries.filter { it.id != action.entry.id }) as S }
        }

        viewModelScope.on<Action.AddFavorite> { action ->
            setState {
                copy(entries.replace(action.entry) {
                    it.id == action.entry.id
                }) as S
            }
        }

        viewModelScope.on<Action.RemoveFavorite> { action ->
            setState {
                copy(entries.replace(action.entry) {
                    it.id == action.entry.id
                }) as S
            }
        }
    }

    fun <T> List<T>.replace(newValue: T, block: (T) -> Boolean): List<T> {
        return map {
            if (block(it)) newValue else it
        }
    }

    abstract fun refresh()
    abstract fun fetchNextPage()

    companion object {
        const val ITEMS_PER_PAGE = 25
    }
}

abstract class ListFragment<VM : ListViewModel<S>, S : ListViewState> : BaseMvRxFragment(R.layout.fragment_list) {
    abstract val viewModel: VM

    lateinit var loadingAnimation: View
    lateinit var recyclerView: EpoxyRecyclerView
    lateinit var refreshLayout: SwipeRefreshLayout
    private val epoxyController: AsyncEpoxyController by lazy { epoxyController() }
    private var delayedBoundary: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingAnimation = view.findViewById(R.id.loading_animation)
        recyclerView = view.findViewById(R.id.recycler_view)
        refreshLayout = view.findViewById(R.id.refresh_layout)

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

        Action.showActionToasts(lifecycleScope, view)
    }

    override fun invalidate() = withState(viewModel) { state ->
        loadingAnimation.isVisible =
            state.request is Loading && state.entries.isEmpty() && !refreshLayout.isRefreshing

        if (state.request.complete) {
            refreshLayout.isRefreshing = false
        }

        epoxyController.requestModelBuild()
    }

    private fun epoxyController() = simpleController(viewModel) { state ->
        if (state.entries.isEmpty() && state.request.complete) {
            listViewMessage {
                id("empty_message")
                iconRes(R.drawable.file_ic_folder)
                title("Nothing to see here.")
            }
        } else if (state.entries.isNotEmpty()) {
            state.entries.forEach() {
                if (it.type == Entry.Type.Group) {
                    listViewGroupHeader {
                        id(it.title)
                        title(it.title)
                    }
                } else {
                    listViewRow {
                        id(it.stableId)
                        data(it)
                        clickListener { _ -> onItemClicked(it) }
                        moreClickListener { _ -> onItemMoreClicked(it) }
                    }
                }
            }

            if (state.lastPage.hasMoreItems) {
                if (state.request is Loading) {
                    listViewPageLoading {
                        id("loading at ${state.lastPage.count}")
                    }
                } else {
                    // On failure delay creating the boundary so that the list scrolls up
                    // and the user has to scroll back down to activate a retry
                    val isFail = state.request is Fail
                    if (isFail && !delayedBoundary) {
                        delayedBoundary = true
                        Handler().postDelayed({ this.requestModelBuild() }, 300)
                    } else {
                        if (isFail) {
                            delayedBoundary = false
                        }
                        listViewPageBoundary {
                            id("boundary at ${state.lastPage.count}")
                            onBind { _, _, _ -> viewModel.fetchNextPage() }
                        }
                    }
                }
            }
        }
    }

    abstract fun onItemClicked(entry: Entry)

    open fun onItemMoreClicked(entry: Entry) {
        ActionListSheet(entry).show(childFragmentManager, null)
    }
}
