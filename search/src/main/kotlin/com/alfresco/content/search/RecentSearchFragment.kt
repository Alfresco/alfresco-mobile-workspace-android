package com.alfresco.content.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import kotlinx.android.synthetic.main.fragment_recent_search.recycler_view

class RecentSearchFragment : BaseMvRxFragment() {

    private val viewModel: RecentSearchViewModel by fragmentViewModel()
    var onEntrySelected: ((String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recent_search, container, false)
    }

    fun scrollToTop() {
        if (isResumed) {
            recycler_view.layoutManager?.scrollToPosition(0)
        }
    }

    override fun invalidate() = withState(viewModel) {
        recycler_view.withModels {
            it.entries.forEach {
                recentSearchRow {
                    id(it)
                    title(it)
                    clickListener { _ -> onEntrySelected?.invoke(it) }
                }
            }
        }
    }
}
