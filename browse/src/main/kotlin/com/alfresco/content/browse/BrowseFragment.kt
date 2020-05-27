package com.alfresco.content.browse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import kotlinx.android.synthetic.main.fragment_browse.loading_animation
import kotlinx.android.synthetic.main.fragment_browse.recycler_view

class BrowseFragment : BaseMvRxFragment() {

    private val viewModel: BrowseViewModel by fragmentViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_browse, container, false)
    }

    override fun invalidate() = withState(viewModel) {
        loading_animation.isVisible = it.nodes is Loading
        recycler_view.withModels {
            it.nodes()?.forEach() {
                browseListRow {
                    id(it.entry.id)
                    node(it)
                }
            }
        }
    }
}
