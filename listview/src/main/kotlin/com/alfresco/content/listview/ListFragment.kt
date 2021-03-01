package com.alfresco.content.listview

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
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
import com.alfresco.content.actions.ActionAddFavorite
import com.alfresco.content.actions.ActionAddOffline
import com.alfresco.content.actions.ActionDelete
import com.alfresco.content.actions.ActionListSheet
import com.alfresco.content.actions.ActionRemoveFavorite
import com.alfresco.content.actions.ActionRemoveOffline
import com.alfresco.content.data.Entry
import com.alfresco.content.data.ResponsePaging
import com.alfresco.content.simpleController
import com.alfresco.events.on
import com.alfresco.list.replace

interface ListViewState : MvRxState {
    val entries: List<Entry>
    val hasMoreItems: Boolean
    val request: Async<ResponsePaging>
    val isCompact: Boolean

    fun copy(_entries: List<Entry>): ListViewState

    fun copyRemoving(entry: Entry) =
        copy(entries.filter {
            it.id != entry.id
        })

    fun copyUpdating(entry: Entry) =
        copy(entries.replace(entry) {
            it.id == entry.id
        })
}

abstract class ListViewModel<S : ListViewState>(
    initialState: S
) : MvRxViewModel<S>(initialState) {

    init {
        viewModelScope.on<ActionDelete> { onDelete(it.entry) }
        viewModelScope.on<ActionAddFavorite> { updateEntry(it.entry) }
        viewModelScope.on<ActionRemoveFavorite> { updateEntry(it.entry) }
        viewModelScope.on<ActionAddOffline> { updateEntry(it.entry) }
        viewModelScope.on<ActionRemoveOffline> { updateEntry(it.entry) }
    }

    private fun onDelete(entry: Entry) = entry.run {
        if (isFile) {
            removeEntry(entry)
        } else {
            refresh()
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun removeEntry(entry: Entry) =
        setState { copyRemoving(entry) as S }

    @Suppress("UNCHECKED_CAST")
    private fun updateEntry(entry: Entry) =
        setState { copyUpdating(entry) as S }

    private fun <T> List<T>.replace(newValue: T, block: (T) -> Boolean): List<T> {
        return map {
            if (block(it)) newValue else it
        }
    }

    abstract fun refresh()
    abstract fun fetchNextPage()
    abstract fun emptyMessageArgs(state: ListViewState): Triple<Int, Int, Int>

    companion object {
        const val ITEMS_PER_PAGE = 25
    }
}

abstract class ListFragment<VM : ListViewModel<S>, S : ListViewState> : BaseMvRxFragment(R.layout.fragment_list) {
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
            state.request is Loading && state.entries.isEmpty() && !refreshLayout.isRefreshing

        if (state.request.complete) {
            refreshLayout.isRefreshing = false
        }

        epoxyController.requestModelBuild()
    }

    private fun epoxyController() = simpleController(viewModel) { state ->
        if (state.entries.isEmpty() && state.request.complete) {
            val args = viewModel.emptyMessageArgs(state)
            listViewMessage {
                id("empty_message")
                iconRes(args.first)
                title(args.second)
                message(args.third)
            }
        } else if (state.entries.isNotEmpty()) {
            state.entries.forEach {
                if (it.type == Entry.Type.GROUP) {
                    listViewGroupHeader {
                        id(it.name)
                        title(it.name)
                    }
                } else {
                    listViewRow {
                        id(it.id)
                        data(it)
                        compact(state.isCompact)
                        clickListener { model, _, _, _ -> onItemClicked(model.data()) }
                        moreClickListener { model, _, _, _ -> onItemMoreClicked(model.data()) }
                    }
                }
            }

            if (state.hasMoreItems) {
                if (state.request is Loading) {
                    listViewPageLoading {
                        id("loading at ${state.entries.size}")
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
                            id("boundary at ${state.entries.size}")
                            onBind { _, _, _ -> viewModel.fetchNextPage() }
                        }
                    }
                }
            }
        }
    }

    abstract fun onItemClicked(entry: Entry)

    open fun onItemMoreClicked(entry: Entry) {
        ActionListSheet.with(entry).show(childFragmentManager, null)
    }
}
