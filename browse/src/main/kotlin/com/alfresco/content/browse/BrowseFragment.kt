package com.alfresco.content.browse

import android.net.Uri
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.MvRx
import com.airbnb.mvrx.fragmentViewModel
import com.alfresco.content.data.Entry
import com.alfresco.content.listview.ListFragment

class BrowseFragment : ListFragment<BrowseViewModel, BrowseViewState>() {

    override val viewModel: BrowseViewModel by fragmentViewModel()

    override fun onItemClicked(entry: Entry) {
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
