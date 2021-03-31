package com.alfresco.content.browse

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.TypedValue
import android.view.Gravity
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.core.view.setMargins
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.withState
import com.alfresco.content.actions.create.ActionSheet
import com.alfresco.content.data.Entry
import com.alfresco.content.fragmentViewModelWithArgs
import com.alfresco.content.listview.ListFragment
import com.alfresco.content.navigateTo
import com.alfresco.content.navigateToContextualSearch
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.parcelize.Parcelize

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

    override fun invalidate() {
        super.invalidate()

        withState(viewModel) { state ->
            if (viewModel.canAddItems(state)) {
                (view as ViewGroup).addView(makeFab(requireContext()))
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_browse, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search -> {
                findNavController().navigateToContextualSearch(args.id ?: "", args.title ?: "")
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

    private fun makeFab(context: Context) =
        FloatingActionButton(context).apply {
            layoutParams = CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.WRAP_CONTENT,
                CoordinatorLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                // TODO: define margins
                setMargins(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics)
                    .toInt())
            }
            setImageResource(R.drawable.ic_plus)
            setOnClickListener {
                showCreateSheet()
            }
        }

    private fun showCreateSheet() = withState(viewModel) {
        ActionSheet.with(requireNotNull(it.parent)).show(childFragmentManager, null)
    }

    companion object {

        fun withArg(path: String): BrowseFragment {
            val fragment = BrowseFragment()
            fragment.arguments = BrowseArgs.bundle(path)
            return fragment
        }
    }
}
