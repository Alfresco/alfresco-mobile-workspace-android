package com.alfresco.content.browse

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.data.Entry
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
                    id(it.id)
                    data(it)
                    clickListener { _ -> onItemClicked(it) }
                }
            }
        }
    }

    private fun onItemClicked(entry: Entry) {
        if (entry.type == Entry.Type.Folder) {
            navigateTo(Uri.parse("alfresco-content://folder/${entry.id}?title=${Uri.encode(entry.title)}"))
        }
    }

    private fun navigateTo(uri: Uri) {
        findNavController().navigate(uri)
    }
}
