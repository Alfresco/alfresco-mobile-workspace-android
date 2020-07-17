package com.alfresco.content.listview

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.airbnb.epoxy.EpoxyRecyclerView
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.withState
import com.alfresco.content.MvRxViewModel
import com.alfresco.content.data.Entry
import com.alfresco.content.data.ResponsePaging

interface ListViewState : MvRxState {
    val entries: List<Entry>
    val request: Async<ResponsePaging>
}

abstract class ListViewModel<S : ListViewState>(
    initialState: S
) : MvRxViewModel<S>(initialState) {

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingAnimation = view.findViewById(R.id.loading_animation)
        recyclerView = view.findViewById(R.id.recycler_view)
        refreshLayout = view.findViewById(R.id.refresh_layout)

        refreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    override fun invalidate() = withState(viewModel) { state ->
        loadingAnimation.isVisible =
            state.request is Loading && state.entries.isEmpty() && !refreshLayout.isRefreshing

        if (state.request.complete) {
            refreshLayout.isRefreshing = false
        }

        recyclerView.withModels {
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
                            id(it.id)
                            data(it)
                            clickListener { _ -> onItemClicked(it) }
                        }
                    }
                }

                if (state.request()?.pagination?.hasMoreItems == true) {
                    listViewRowLoading {
                        // Changing the ID will force it to rebind when new data is loaded even if it is
                        // still on screen which will ensure that we trigger loading again.
                        id("loading${state.entries.count()}")
                        onBind { _, _, _ -> viewModel.fetchNextPage() }
                    }
                }
            }
        }
    }

    abstract fun onItemClicked(entry: Entry)
}
