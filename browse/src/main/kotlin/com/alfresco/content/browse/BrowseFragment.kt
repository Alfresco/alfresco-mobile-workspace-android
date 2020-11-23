package com.alfresco.content.browse

import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.alfresco.content.data.Entry
import com.alfresco.content.fragmentViewModelWithArgs
import com.alfresco.content.listview.ListFragment
import com.alfresco.content.navigateTo
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BrowseArgs(
    val path: String,
    val id: String?,
    val title: String?
) : Parcelable {
    companion object {
        private const val PATH_KEY = "path"
        private const val ID_KEY = "id"
        private const val TITLE_KEY = "title"

        fun with(args: Bundle): BrowseArgs {
            return BrowseArgs(
                args.getString(PATH_KEY, ""),
                args.getString(ID_KEY, null),
                args.getString(TITLE_KEY, null)
            )
        }

        fun bundle(path: String) = bundleOf(PATH_KEY to path)
    }
}

class BrowseFragment : ListFragment<BrowseViewModel, BrowseViewState>() {

    private lateinit var args: BrowseArgs
    override val viewModel: BrowseViewModel by fragmentViewModelWithArgs { args }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        args = BrowseArgs.with(requireArguments())

        // Contextual search only in folders/sites
        if (args.id != null) {
            setHasOptionsMenu(true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_browse, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search -> {
                findNavController().navigate(Uri.parse("alfresco://content/${args.path}/${args.id}/search?title=${Uri.encode(args.title)}"))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onItemClicked(entry: Entry) {
        // Disable interaction on Trash items
        if (entry.isTrashed) return

        findNavController().navigateTo(entry)
    }

    companion object {

        fun withArg(path: String): BrowseFragment {
            val fragment = BrowseFragment()
            fragment.arguments = BrowseArgs.bundle(path)
            return fragment
        }
    }
}
