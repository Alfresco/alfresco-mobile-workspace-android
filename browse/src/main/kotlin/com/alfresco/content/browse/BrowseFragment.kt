package com.alfresco.content.browse

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.util.TypedValue
import android.view.Gravity
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.core.view.setMargins
import androidx.navigation.fragment.findNavController
import androidx.transition.ChangeBounds
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.airbnb.mvrx.InternalMavericksApi
import com.airbnb.mvrx.withState
import com.alfresco.content.actions.CreateActionsSheet
import com.alfresco.content.actions.MoveResultContract.Companion.MOVE_ID_KEY
import com.alfresco.content.browse.processes.sheet.ProcessDefinitionsSheet
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.Entry
import com.alfresco.content.data.MultiSelection
import com.alfresco.content.data.MultiSelectionData
import com.alfresco.content.data.PageView
import com.alfresco.content.data.ParentEntry
import com.alfresco.content.fragmentViewModelWithArgs
import com.alfresco.content.listview.ListFragment
import com.alfresco.content.navigateTo
import com.alfresco.content.navigateToContextualSearch
import com.alfresco.content.navigateToLocalPreview
import com.alfresco.content.navigateToUploadFilesPath
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@Parcelize
data class BrowseArgs(
    val path: String,
    val id: String?,
    val moveId: String,
    val title: String?,
) : Parcelable {
    companion object {
        private const val PATH_KEY = "path"
        private const val ID_KEY = "id"
        private const val TITLE_KEY = "title"

        fun with(args: Bundle): BrowseArgs {
            return BrowseArgs(
                args.getString(PATH_KEY, ""),
                args.getString(ID_KEY, null),
                args.getString(MOVE_ID_KEY, ""),
                args.getString(TITLE_KEY, null),
            )
        }

        fun bundle(path: String) = bundleOf(PATH_KEY to path)
    }
}

class BrowseFragment : ListFragment<BrowseViewModel, BrowseViewState>() {

    private lateinit var args: BrowseArgs
    private var fab: FloatingActionButton? = null

    @OptIn(InternalMavericksApi::class)
    override val viewModel: BrowseViewModel by fragmentViewModelWithArgs { args }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        args = BrowseArgs.with(requireArguments())

        GlobalScope.launch {
            MultiSelection.observeClearSelection().collect {
                Handler(Looper.getMainLooper()).post {
                    if (isAdded) {
                        clearMultiSelection()
                    }
                }
            }
        }

        // Contextual search only in folders/sites
        if (args.id != null) {
            setHasOptionsMenu(true)
            val supportActionBar = (requireActivity() as AppCompatActivity).supportActionBar
            supportActionBar?.title = args.title
            supportActionBar?.setHomeActionContentDescription(requireActivity().getString(R.string.label_navigation_back))
        }

        setViewRequiredMultiSelection(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        withState(viewModel) { state ->
            if (state.path == getString(R.string.nav_path_recents)) {
                viewModel.refreshTransfersSize()
                viewModel.observeTransferUploads()
                bannerTransferData?.setOnClickListener {
                    findNavController().navigateToUploadFilesPath(true, requireContext().getString(R.string.title_transfers))
                }
            } else bannerTransferData?.visibility = View.GONE
        }
    }

    override fun invalidate() = withState(viewModel) { state ->
        super.invalidate()

        if (state.path == getString(R.string.nav_path_recents)) {
            updateBanner(state.totalTransfersSize, state.uploadTransferList.size)
            if (state.uploadTransferList.isEmpty()) {
                viewModel.resetTransferData()
            }
        }

        state.title?.let {
            (requireActivity() as AppCompatActivity).supportActionBar?.title = it
        }

        if (viewModel.canAddItems(state) && fab == null) {
            fab = makeFab(requireContext())
            (view as ViewGroup).addView(fab)
        }

        fab?.visibility = if (state.selectedEntries.isNotEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        withState(viewModel) {
            viewModel.triggerAnalyticsEvent(it)
        }
    }

    private fun updateBanner(totalSize: Int, pendingFilesCount: Int) {
        if (totalSize != 0 && pendingFilesCount != 0) {
            bannerTransferData?.visibility = View.VISIBLE
        }

        val uploadFileCount = totalSize - pendingFilesCount
        val percentage = (uploadFileCount.toFloat().div(totalSize.toFloat())).times(100)

        if (pendingFilesCount != 0) {
            tvUploadingFiles?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_upload, 0, 0, 0)
            tvUploadingFiles?.text = String.format(getString(com.alfresco.content.listview.R.string.upload_file_text_multiple), pendingFilesCount)
        } else {
            tvUploadingFiles?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_upload_done, 0, 0, 0)
            tvUploadingFiles?.text = String.format(getString(com.alfresco.content.listview.R.string.upload_complete_text_multiple), totalSize)
        }

        tvPercentage?.text = String.format(getString(com.alfresco.content.listview.R.string.upload_percentage_text), percentage)

        percentageFiles?.progress = percentage.toInt()

        if (totalSize != 0 && pendingFilesCount == 0) {
            hideBanner(3000)
        }
    }

    private fun hideBanner(millis: Long) {
        bannerTransferData?.postDelayed({
            bannerTransferData?.apply {
                TransitionManager.beginDelayedTransition(
                    this,
                    TransitionSet()
                        .addTransition(Fade())
                        .addTransition(ChangeBounds()),
                )
                bannerTransferData?.visibility = View.GONE
            }
        }, millis)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_browse, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search -> {
                findNavController().navigateToContextualSearch(args.id ?: "", args.title ?: "", false)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onItemClicked(entry: Entry) {
        // Disable interaction on Trash or Upload items
        if (entry.isTrashed) return

        if (entry.isFolder) {
            AnalyticsManager().screenViewEvent(PageView.PersonalFiles)
        }

        if (entry.isUpload) {
            entry.mimeType?.let {
                findNavController().navigateToLocalPreview(it, entry.path.toString(), entry.name)
            }
        } else
            findNavController().navigateTo(entry)
    }

    override fun onItemLongClicked(entry: Entry) {
        viewModel.toggleSelection(entry)
        withState(viewModel) { state ->
            MultiSelection.multiSelectionChangedFlow.tryEmit(MultiSelectionData(state.selectedEntries, true))
        }
    }

    private fun makeFab(context: Context) =
        FloatingActionButton(context).apply {
            layoutParams = CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.WRAP_CONTENT,
                CoordinatorLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                // TODO: define margins
                setMargins(
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics)
                        .toInt(),
                )
            }
            contentDescription = getString(R.string.accessibility_text_create_button)
            setImageResource(R.drawable.ic_add_fab)
            setOnClickListener {
                showCreateSheet()
            }
        }

    private fun showCreateSheet() = withState(viewModel) {
        CreateActionsSheet.with(requireNotNull(it.parent)).show(childFragmentManager, null)
    }

    companion object {

        fun withArg(path: String): BrowseFragment {
            val fragment = BrowseFragment()
            fragment.arguments = BrowseArgs.bundle(path)
            return fragment
        }
    }

    override fun onEntryCreated(entry: ParentEntry) {
        if (isAdded && isVisible) {
            onItemClicked(entry as Entry)
        }
    }

    override fun onProcessStart(entry: ParentEntry) {
        if (isAdded && isVisible) {
            ProcessDefinitionsSheet.with(entry as Entry).show(parentFragmentManager, null)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fab = null
    }

    fun clearMultiSelection() {
        disableLongPress()
        viewModel.resetMultiSelection()
    }
}
