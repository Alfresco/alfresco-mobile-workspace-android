package com.alfresco.content.browse.offline

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.InternalMavericksApi
import com.airbnb.mvrx.withState
import com.alfresco.content.actions.ActionSyncNow
import com.alfresco.content.browse.R
import com.alfresco.content.browse.processes.sheet.ProcessDefinitionsSheet
import com.alfresco.content.data.Entry
import com.alfresco.content.data.MultiSelection
import com.alfresco.content.data.MultiSelectionData
import com.alfresco.content.data.ParentEntry
import com.alfresco.content.fragmentViewModelWithArgs
import com.alfresco.content.listview.ListFragment
import com.alfresco.content.navigateTo
import com.alfresco.events.emit
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

class OfflineFragment : ListFragment<OfflineViewModel, OfflineViewState>() {

    @OptIn(InternalMavericksApi::class)
    override val viewModel: OfflineViewModel by fragmentViewModelWithArgs { OfflineBrowseArgs.with(arguments) }
    private var fab: ExtendedFloatingActionButton? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViewRequiredMultiSelection(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        fab = null
    }

    override fun invalidate() = withState(viewModel) { state ->
        super.invalidate()

        // Add fab only to root folder
        if (state.parentId == null && fab == null) {
            fab = makeFab(requireContext()).apply {
                visibility = View.INVISIBLE // required for animation
            }
            (view as ViewGroup).addView(fab)
        }

        fab?.apply {
            if (state.entries.isNotEmpty()) {
                show()
            } else {
                hide()
            }

            isEnabled = state.syncNowEnabled
        }

        fab?.visibility = if (state.selectedEntries.isNotEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun makeFab(context: Context) =
        ExtendedFloatingActionButton(context).apply {
            layoutParams = CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.WRAP_CONTENT,
                CoordinatorLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                setMargins(0, 0, 0, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics).toInt())
            }
            text = context.getText(R.string.offline_sync_button_title)
            gravity = Gravity.CENTER
            setOnClickListener {
                onSyncButtonClick()
            }
        }

    private fun onSyncButtonClick() {
        if (viewModel.canSyncOverCurrentNetwork()) {
            startSync(false)
        } else {
            makeSyncUnavailablePrompt().show()
        }
    }

    private fun makeSyncUnavailablePrompt() =
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.offline_sync_unavailable_title))
            .setMessage(resources.getString(R.string.offline_sync_unavailable_message))
            .setPositiveButton(resources.getString(R.string.offline_sync_unavailable_positive)) { _, _ ->
                startSync(true)
            }
            .setNegativeButton(resources.getString(R.string.offline_sync_unavailable_negative)) { _, _ ->
                startSync(false)
            }

    private fun startSync(overrideNetwork: Boolean) =
        lifecycleScope.emit(ActionSyncNow(overrideNetwork))

    override fun onItemClicked(entry: Entry) {
        if (entry.isFolder || entry.isSynced) {
            findNavController().navigateTo(entry)
        }
    }

    override fun onItemLongClicked(entry: Entry) {
        viewModel.toggleSelection(entry)
        withState(viewModel) { state ->
            MultiSelection.multiSelectionChangedFlow.tryEmit(MultiSelectionData(state.selectedEntries, true))
        }
    }

    override fun onProcessStart(entry: ParentEntry) {
        if (isAdded && isVisible) {
            ProcessDefinitionsSheet.with(entry as Entry).show(parentFragmentManager, null)
        }
    }

    fun clearMultiSelection() {
        disableLongPress()
        viewModel.resetMultiSelection()
    }
}
