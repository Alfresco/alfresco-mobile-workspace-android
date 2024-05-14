package com.alfresco.content.process.ui.fragments

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
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.GetMultipleContents
import com.alfresco.content.actions.ActionOpenWith
import com.alfresco.content.common.EntryListener
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.AttachFilesData
import com.alfresco.content.data.Entry
import com.alfresco.content.data.PageView
import com.alfresco.content.data.ParentEntry
import com.alfresco.content.data.UploadServerType
import com.alfresco.content.data.payloads.FieldType
import com.alfresco.content.listview.listViewMessage
import com.alfresco.content.mimetype.MimeType
import com.alfresco.content.process.R
import com.alfresco.content.process.databinding.FragmentAttachFilesBinding
import com.alfresco.content.process.ui.epoxy.listViewAttachmentRow
import com.alfresco.content.simpleController
import com.alfresco.events.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Marked as ProcessAttachFilesFragment class
 */
class ProcessAttachFilesFragment : ProcessBaseFragment(), MavericksView, EntryListener {

    val viewModel: ProcessAttachFilesViewModel by fragmentViewModel()
    private lateinit var binding: FragmentAttachFilesBinding
    private val epoxyController: AsyncEpoxyController by lazy { epoxyController() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAttachFilesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AnalyticsManager().screenViewEvent(PageView.AttachedFiles)

        viewModel.setListener(this)

        binding.refreshLayout.isEnabled = false

        binding.recyclerView.setController(epoxyController)

        epoxyController.adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0) {
                    // @see: https://github.com/airbnb/epoxy/issues/224
                    binding.recyclerView.layoutManager?.scrollToPosition(0)
                }
            }
        })
    }

    override fun onConfirmDelete(entry: Entry) {
        viewModel.deleteAttachment(entry)
    }

    override fun invalidate() = withState(viewModel) { state ->
        val handler = Handler(Looper.getMainLooper())
        binding.refreshLayout.isRefreshing = false
        binding.loading.isVisible = false

        handler.post {
            if (isAdded) {
                if (state.listContents.isNotEmpty()) {
                    binding.tvNoOfAttachments.visibility = View.VISIBLE
                    val filesHeader = StringBuilder()
                    filesHeader.append(getString(R.string.text_multiple_attachment, state.listContents.size)).apply {
                        if (!state.isReadOnlyField) {
                            this.append("\n")
                                .append(getString(R.string.process_max_file_size, GetMultipleContents.MAX_FILE_SIZE_10))
                        }
                    }

                    binding.tvNoOfAttachments.text = filesHeader
                } else {
                    binding.tvNoOfAttachments.visibility = View.GONE
                }
            }
        }

        binding.fabAddAttachments.visibility = if (hasContentAddButton(state)) View.VISIBLE else View.GONE

        binding.fabAddAttachments.setOnClickListener {
            showCreateSheet(state, viewModel.parentId)
        }
        epoxyController.requestModelBuild()
    }

    private fun hasContentAddButton(state: ProcessAttachFilesViewState): Boolean {
        val field = state.parent.field
        if (field.type == FieldType.READONLY.value() || field.type == FieldType.READONLY_TEXT.value()) {
            return false
        }
        return !(field.params?.multiple == false && state.listContents.isNotEmpty())
    }

    private fun epoxyController() = simpleController(viewModel) { state ->

        if (state.listContents.isEmpty()) {
            val args = viewModel.emptyMessageArgs(state)
            listViewMessage {
                id("empty_message")
                iconRes(args.first)
                title(args.second)
                message(args.third)
            }
        } else {
            state.listContents.forEach { obj ->
                listViewAttachmentRow {
                    id(stableId(obj))
                    data(obj)
                    processData(state.isProcessInstance && state.isReadOnlyField)
                    clickListener { model, _, _, _ ->
                        if (state.isProcessInstance) {
                            onItemClicked(model.data())
                        }
                    }
                    deleteContentClickListener { model, _, _, _ -> onConfirmDelete(model.data()) }
                }
            }
        }
    }

    private fun onItemClicked(contentEntry: Entry) {
        if (!contentEntry.isUpload) {
            val entry = Entry.convertContentEntryToEntry(
                contentEntry,
                MimeType.isDocFile(contentEntry.mimeType),
                UploadServerType.UPLOAD_TO_TASK,
            )
            if (!contentEntry.source.isNullOrEmpty()) {
                remoteViewerIntent(entry)
            } else
                viewModel.executePreview(ActionOpenWith(entry))
        } else {
            localViewerIntent(contentEntry)
        }
    }

    override fun onEntryCreated(entry: ParentEntry) {
        if (isAdded) {
            localViewerIntent(entry as Entry)
        }
    }

    override fun onDestroy() {
        withState(viewModel) {
            CoroutineScope(Dispatchers.Main).launch {
                EventBus.default.send(AttachFilesData(it.parent.field))
            }
        }
        super.onDestroy()
    }
}
