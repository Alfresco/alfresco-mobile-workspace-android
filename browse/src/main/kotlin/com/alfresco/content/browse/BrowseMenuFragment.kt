package com.alfresco.content.browse

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
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
                if (it.path.isNotEmpty()) {
                    browseMenuRow {
                        id(it.title)
                        entry(it)
                        clickListener { _ -> navigateTo(it.path, it.title) }
                    }
                } else {
                    browseMenuSeparator {
                        id(it.title)
                    }
                }
            }
        }
    }

    private fun navigateTo(path: String, title: String) {
        navigateTo(Uri.parse("alfresco-content://folder/$path?title=${Uri.encode(title)}"))
    }

    private fun navigateTo(uri: Uri) {
        findNavController().navigate(uri)
    }
}
