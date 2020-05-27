package com.alfresco.content.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import kotlinx.android.synthetic.main.fragment_search.recycler_view
import kotlinx.android.synthetic.main.fragment_search.search_view

class SearchFragment : BaseMvRxFragment() {

    private val viewModel: SearchViewModel by fragmentViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        search_view.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    viewModel.setSearchQuery(query)
                    // TODO: Hide keyboard
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    viewModel.setSearchQuery(newText)
                    return true
                }
            })
        }
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
}
