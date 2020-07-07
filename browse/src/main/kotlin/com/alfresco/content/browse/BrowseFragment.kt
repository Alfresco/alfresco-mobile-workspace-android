package com.alfresco.content.browse

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.BaseMvRxFragment
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MvRx
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

    override fun invalidate() = withState(viewModel) { state ->
        loading_animation.isVisible = state.req is Loading && state.entries.isEmpty()

        recycler_view.withModels {
            if (state.entries.isEmpty() && state.req.complete) {
                browseListMessageView {
                    id("empty_message")
                    iconRes(R.drawable.ic_personal)
                    title("Nothing to see here.")
                }
            } else if (state.entries.isNotEmpty()) {
                state.entries.forEach() {
                    browseListRow {
                        id(it.id)
                        data(it)
                        clickListener { _ -> onItemClicked(it) }
                    }
                }

                if (state.req()?.pagination?.hasMoreItems == true) {
                    browseListLoadingRow {
                        // Changing the ID will force it to rebind when new data is loaded even if it is
                        // still on screen which will ensure that we trigger loading again.
                        id("loading${state.entries.count()}")
                        onBind { _, _, _ -> viewModel.fetchNextPage() }
                    }
                }
            }
        }
    }

    private fun onItemClicked(entry: Entry) {
        if (entry.type == Entry.Type.Folder || entry.type == Entry.Type.Site) {
            navigateTo(Uri.parse("alfresco-content://folder/${entry.id}?title=${Uri.encode(entry.title)}"))
        }
    }

    private fun navigateTo(uri: Uri) {
        findNavController().navigate(uri)
    }

    companion object {
        fun arg(path: String) = bundleOf(MvRx.KEY_ARG to path)

        fun withArg(path: String): BrowseFragment {
            val fragment = BrowseFragment()
            fragment.arguments = arg(path)
            return fragment
        }
    }
}
