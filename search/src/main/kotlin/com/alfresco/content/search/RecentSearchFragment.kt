package com.alfresco.content.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.search.databinding.FragmentRecentSearchBinding

class RecentSearchFragment : Fragment(), MavericksView {

    private val viewModel: RecentSearchViewModel by fragmentViewModel()
    private lateinit var binding: FragmentRecentSearchBinding
    var onEntrySelected: ((String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRecentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun scrollToTop() {
        if (isResumed) {
            binding.recyclerView.layoutManager?.scrollToPosition(0)
        }
    }

    override fun invalidate() = withState(viewModel) {
        binding.recyclerView.withModels {
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
