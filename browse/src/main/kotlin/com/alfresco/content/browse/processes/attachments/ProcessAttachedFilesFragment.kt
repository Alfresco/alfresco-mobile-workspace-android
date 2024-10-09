package com.alfresco.content.browse.processes.attachments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.browse.R
import com.alfresco.content.browse.databinding.FragmentAttachedFilesBinding
import com.alfresco.content.browse.processes.details.ProcessDetailViewModel
import com.alfresco.content.browse.tasks.BaseDetailFragment
import com.alfresco.content.browse.tasks.attachments.listViewAttachmentRow
import com.alfresco.content.common.EntryListener
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.Entry
import com.alfresco.content.data.PageView
import com.alfresco.content.data.ParentEntry
import com.alfresco.content.data.UploadServerType
import com.alfresco.content.mimetype.MimeType
import com.alfresco.content.simpleController
import com.alfresco.ui.getDrawableForAttribute

/**
 * Marked as ProcessAttachedFilesFragment class
 */
class ProcessAttachedFilesFragment : BaseDetailFragment(), MavericksView, EntryListener {
    val viewModel: ProcessDetailViewModel by activityViewModel()
    private lateinit var binding: FragmentAttachedFilesBinding
    private val epoxyController: AsyncEpoxyController by lazy { epoxyController() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAttachedFilesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        AnalyticsManager().screenViewEvent(PageView.AttachedFiles)
        binding.refreshLayout.isEnabled = false
        binding.toolbar.apply {
            navigationContentDescription = getString(R.string.label_navigation_back)
            navigationIcon = requireContext().getDrawableForAttribute(R.attr.homeAsUpIndicator)
            setNavigationOnClickListener { requireActivity().onBackPressed() }
            title = resources.getString(R.string.title_attached_files)
        }

        binding.recyclerView.setController(epoxyController)

        epoxyController.adapter.registerAdapterDataObserver(
            object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(
                    positionStart: Int,
                    itemCount: Int,
                ) {
                    if (positionStart == 0) {
                        // @see: https://github.com/airbnb/epoxy/issues/224
                        binding.recyclerView.layoutManager?.scrollToPosition(0)
                    }
                }
            },
        )
    }

    override fun onConfirmDelete(contentId: String) {
        viewModel.deleteAttachment(contentId)
    }

    override fun invalidate() =
        withState(viewModel) { state ->
            val handler = Handler(Looper.getMainLooper())
            binding.refreshLayout.isRefreshing = false
            binding.loading.isVisible = false
            handler.post {
                if (state.listContents.size > 4) {
                    binding.tvNoOfAttachments.visibility = View.VISIBLE
                    binding.tvNoOfAttachments.text = getString(R.string.text_multiple_attachment, state.listContents.size)
                } else {
                    binding.tvNoOfAttachments.visibility = View.GONE
                }
            }

            binding.fabAddAttachments.visibility = View.VISIBLE
            binding.fabAddAttachments.setOnClickListener {
                showCreateSheet(state, viewModel.observerID)
            }

            if (state.listContents.isEmpty()) requireActivity().onBackPressed()

            epoxyController.requestModelBuild()
        }

    private fun epoxyController() =
        simpleController(viewModel) { state ->

            if (state.listContents.isNotEmpty()) {
                state.listContents.forEach { obj ->
                    listViewAttachmentRow {
                        id(stableId(obj))
                        data(obj)
                        deleteContentClickListener { model, _, _, _ -> onConfirmDelete(model.data().id) }
                    }
                }
            }
        }

    private fun onItemClicked(contentEntry: Entry) {
        if (!contentEntry.isUpload) {
            if (!contentEntry.source.isNullOrEmpty()) {
                val entry =
                    Entry.convertContentEntryToEntry(
                        contentEntry,
                        MimeType.isDocFile(contentEntry.mimeType),
                        UploadServerType.UPLOAD_TO_PROCESS,
                    )
                remoteViewerIntent(entry)
            }
        } else {
            localViewerIntent(contentEntry)
        }
    }

    override fun onEntryCreated(entry: ParentEntry) {
        if (isAdded) {
            localViewerIntent(entry as Entry)
        }
    }
}
