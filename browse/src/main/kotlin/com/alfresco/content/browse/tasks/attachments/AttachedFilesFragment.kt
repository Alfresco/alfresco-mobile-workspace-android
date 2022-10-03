package com.alfresco.content.browse.tasks.attachments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.actions.ActionOpenWith
import com.alfresco.content.browse.R
import com.alfresco.content.browse.databinding.FragmentAttachedFilesBinding
import com.alfresco.content.browse.preview.LocalPreviewActivity
import com.alfresco.content.browse.tasks.detail.TaskDetailViewModel
import com.alfresco.content.browse.tasks.detail.execute
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.ContentEntry
import com.alfresco.content.data.Entry
import com.alfresco.content.data.EventName
import com.alfresco.content.data.PageView
import com.alfresco.content.data.ParentEntry
import com.alfresco.content.listview.EntryListener
import com.alfresco.content.mimetype.MimeType
import com.alfresco.content.simpleController
import com.alfresco.ui.getDrawableForAttribute
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.ref.WeakReference

/**
 * Marked as AttachedFilesFragment class
 */
class AttachedFilesFragment : Fragment(), MavericksView, EntryListener {

    val viewModel: TaskDetailViewModel by activityViewModel()
    private lateinit var binding: FragmentAttachedFilesBinding
    private var deleteContentDialog = WeakReference<AlertDialog>(null)
    private val epoxyController: AsyncEpoxyController by lazy { epoxyController() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAttachedFilesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AnalyticsManager().screenViewEvent(PageView.AttachedFiles)

        binding.toolbar.apply {
            navigationContentDescription = getString(R.string.label_navigation_back)
            navigationIcon = requireContext().getDrawableForAttribute(R.attr.homeAsUpIndicator)
            setNavigationOnClickListener { requireActivity().onBackPressed() }
            title = resources.getString(R.string.title_attached_files)
        }

        binding.recyclerView.setController(epoxyController)

        epoxyController.adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0) {
                    // @see: https://github.com/airbnb/epoxy/issues/224
                    binding.recyclerView.layoutManager?.scrollToPosition(0)
                }
            }
        })
        binding.refreshLayout.setOnRefreshListener {
            viewModel.getComments()
        }

        viewModel.setListener(this)
    }

    override fun invalidate() = withState(viewModel) { state ->

        binding.loading.isVisible = state.requestDeleteContent is Loading

            if (state.requestContents.complete) {
            binding.refreshLayout.isRefreshing = false
        }

        epoxyController.requestModelBuild()

        if (state.listContents.size > 4) {
            binding.tvNoOfAttachments.visibility = View.VISIBLE
            binding.tvNoOfAttachments.text = getString(R.string.text_multiple_attachment, state.listContents.size)
        } else {
            binding.tvNoOfAttachments.visibility = View.GONE
        }
    }

    private fun epoxyController() = simpleController(viewModel) { state ->

        if (state.listContents.isNotEmpty()) {
            state.listContents.forEach { obj ->
                listViewAttachmentRow {
                    id(obj.id)
                    data(obj)
                    clickListener { model, _, _, _ -> onItemClicked(model.data()) }
                    deleteContentClickListener { model, _, _, _ -> deleteContentPrompt(model.data()) }
                }
            }
        }
    }

    private fun onItemClicked(contentEntry: ContentEntry) {
        viewModel.execute(ActionOpenWith(Entry.convertContentEntryToEntry(contentEntry, MimeType.isDocFile(contentEntry.mimeType))))
    }

    private fun deleteContentPrompt(contentEntry: ContentEntry) {
        AnalyticsManager().taskEvent(EventName.DeleteTaskAttachment)
        val oldDialog = deleteContentDialog.get()
        if (oldDialog != null && oldDialog.isShowing) return
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.dialog_title_delete_content))
            .setMessage(contentEntry.name)
            .setIcon(
                ResourcesCompat.getDrawable(
                    requireContext().resources,
                    MimeType.with(contentEntry.mimeType).icon,
                    requireContext().theme
                )
            )
            .setNegativeButton(getString(R.string.dialog_negative_button_task), null)
            .setPositiveButton(getString(R.string.dialog_positive_button_task)) { _, _ ->
                viewModel.deleteAttachment(contentEntry.id.toString())
            }
            .show()
        deleteContentDialog = WeakReference(dialog)
    }

    override fun onEntryCreated(entry: ParentEntry) {
        if (isAdded)
            startActivity(
                Intent(requireActivity(), LocalPreviewActivity::class.java)
                    .putExtra(LocalPreviewActivity.KEY_ENTRY_OBJ, entry as Entry)
            )
    }
}
