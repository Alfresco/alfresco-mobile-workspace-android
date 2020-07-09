package com.alfresco.content.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_search.recents_fragment
import kotlinx.android.synthetic.main.fragment_search.results_fragment

class SearchFragment : Fragment() {

    private val recentsFragment by lazy {
        childFragmentManager.findFragmentById(R.id.recents_fragment) as RecentSearchFragment
    }
    private val resultsFragment by lazy {
        childFragmentManager.findFragmentById(R.id.results_fragment) as SearchResultsFragment
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    fun setSearchQuery(query: String) {
        if (query.trim().length > 2) {
            recents_fragment.visibility = View.GONE
            results_fragment.visibility = View.VISIBLE
            resultsFragment.setSearchQuery(query)
        } else {
            recents_fragment.visibility = View.VISIBLE
            results_fragment.visibility = View.GONE
        }
    }
}
