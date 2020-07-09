package com.alfresco.content.listview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.withState
import com.alfresco.content.data.Entry
import kotlinx.android.synthetic.main.fragment_list.loading_animation
import kotlinx.android.synthetic.main.fragment_list.recycler_view
import kotlinx.android.synthetic.main.fragment_list.refresh_layout

abstract class ListFragment<VM : ListViewModel> : BaseMvRxFragment() {
    abstract val viewModel: VM

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        refresh_layout.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    override fun invalidate() = withState(viewModel) { state ->
        loading_animation.isVisible =
            state.req is Loading && state.entries.isEmpty() && !refresh_layout.isRefreshing

        if (state.req.complete) {
            refresh_layout.isRefreshing = false
        }

        recycler_view.withModels {
            if (state.entries.isEmpty() && state.req.complete) {
                listViewMessage {
                    id("empty_message")
                    iconRes(R.drawable.file_ic_folder)
                    title("Nothing to see here.")
                }
            } else if (state.entries.isNotEmpty()) {
                state.entries.forEach() {
                    listViewRow {
                        id(it.id)
                        data(it)
                        clickListener { _ -> onItemClicked(it) }
                    }
                }

                if (state.req()?.pagination?.hasMoreItems == true) {
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