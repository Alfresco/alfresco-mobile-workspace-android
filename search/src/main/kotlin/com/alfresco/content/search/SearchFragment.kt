package com.alfresco.content.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import kotlinx.android.synthetic.main.fragment_search.recycler_view

class SearchFragment : BaseMvRxFragment() {

    private val viewModel: SearchViewModel by fragmentViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun invalidate() = withState(viewModel) {
        recycler_view.withModels {
            it.results()?.forEach() {
                searchResultRow {
                    id(it.id)
                    node(it)
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        viewModel.setSearchQuery(query)
    }
}
