package com.alfresco.content.browse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import kotlinx.android.synthetic.main.fragment_browse_menu.recycler_view

class BrowseMenuFragment : BaseMvRxFragment() {

    private val viewModel: BrowseMenuViewModel by fragmentViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_browse_menu, container, false)
    }

    override fun invalidate() = withState(viewModel) {
        recycler_view.withModels {
            it.entries.forEach {
                browseMenuRow {
                    id(it.title)
                    entry(it)
                }
            }
        }
    }
}
