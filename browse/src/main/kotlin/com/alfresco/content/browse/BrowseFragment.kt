package com.alfresco.content.browse

import android.net.Uri
import android.os.Parcelable
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.alfresco.content.data.Entry
import com.alfresco.content.fragmentViewModelWithArgs
import com.alfresco.content.listview.ListFragment
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BrowseArgs(
    val path: String,
    val id: String?,
    val title: String?
) : Parcelable {
    companion object {
        const val PATH_KEY = "path"
        const val ID_KEY = "id"
        const val TITLE_KEY = "title"
    }
}

class BrowseFragment : ListFragment<BrowseViewModel, BrowseViewState>() {

    override val viewModel: BrowseViewModel by fragmentViewModelWithArgs {
        val args = requireArguments()
        BrowseArgs(
            args.getString(BrowseArgs.PATH_KEY, ""),
            args.getString(BrowseArgs.ID_KEY, null),
            args.getString(BrowseArgs.TITLE_KEY, null)
        )
    }

    override fun onItemClicked(entry: Entry) {
        when (entry.type) {
            Entry.Type.Folder -> navigateToFolder(entry)
            Entry.Type.Site -> navigateToSite(entry)
            else -> { } // no-op for now
        }
    }

    private fun navigateToFolder(entry: Entry) {
        navigateTo(Uri.parse("alfresco://content/folder/${entry.id}?title=${Uri.encode(entry.title)}"))
    }

    private fun navigateToSite(entry: Entry) {
        navigateTo(Uri.parse("alfresco://content/site/${entry.id}?title=${Uri.encode(entry.title)}"))
    }

    private fun navigateTo(uri: Uri) {
        findNavController().navigate(uri)
    }

    companion object {
        fun arg(path: String) = bundleOf(BrowseArgs.PATH_KEY to path)

        fun withArg(path: String): BrowseFragment {
            val fragment = BrowseFragment()
            fragment.arguments = arg(path)
            return fragment
        }
    }
}
