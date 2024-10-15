package com.alfresco.content.browse.processes.details

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.browse.R
import com.alfresco.content.browse.databinding.FragmentTaskDetailBinding
import com.alfresco.content.browse.processes.ProcessDetailActivity
import com.alfresco.content.browse.tasks.BaseDetailFragment
import com.alfresco.content.browse.tasks.attachments.listViewAttachmentRow
import com.alfresco.content.common.EntryListener
import com.alfresco.content.component.ComponentBuilder
import com.alfresco.content.component.ComponentData
import com.alfresco.content.component.searchusergroup.SearchUserGroupComponentBuilder
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.Entry
import com.alfresco.content.data.PageView
import com.alfresco.content.data.ParentEntry
import com.alfresco.content.data.ProcessEntry
import com.alfresco.content.data.UploadServerType
import com.alfresco.content.mimetype.MimeType
import com.alfresco.content.simpleController
import com.alfresco.ui.getDrawableForAttribute
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Marked as ProcessDetailFragment
 */
class ProcessDetailFragment : BaseDetailFragment(), MavericksView, EntryListener {
    lateinit var binding: FragmentTaskDetailBinding
    val viewModel: ProcessDetailViewModel by activityViewModel()
    private val epoxyAttachmentController: AsyncEpoxyController by lazy { epoxyAttachmentController() }
    private var viewLayout: View? = null
    private var confirmContentQueueDialog = WeakReference<AlertDialog>(null)
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        if (viewLayout == null) {
            binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
            viewLayout = binding.root
        }
        return viewLayout as View
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        AnalyticsManager().screenViewEvent(PageView.WorkflowView)
        (requireActivity() as ProcessDetailActivity).setSupportActionBar(binding.toolbar)
        binding.toolbar.apply {
            navigationContentDescription = getString(R.string.label_navigation_back)
            navigationIcon = requireContext().getDrawableForAttribute(R.attr.homeAsUpIndicator)
            setNavigationOnClickListener {
                withState(viewModel) { _ ->
                    requireActivity().onBackPressed()
                }
            }
        }

        showStartFormView()
        setListeners()
        binding.recyclerViewAttachments.setController(epoxyAttachmentController)
    }

    override fun onConfirmDelete(contentId: String) {
        viewModel.deleteAttachment(contentId)
    }

    override fun invalidate() =
        withState(viewModel) { state ->
            binding.loading.isVisible = state.requestContent is Loading || state.requestProcessDefinition is Loading ||
                state.requestStartWorkflow is Loading || state.requestTasks is Loading
            setData(state)
            if (state.parent?.processDefinitionId.isNullOrEmpty()) {
                binding.toolbar.title = resources.getString(R.string.title_start_workflow)
                updateUI(state)
                when {
                    state.requestStartWorkflow is Success && state.requestStartWorkflow.complete -> {
                        viewModel.updateProcessList()
                        requireActivity().onBackPressed()
                    }
                }
                epoxyAttachmentController.requestModelBuild()
            } else {
                binding.toolbar.title = resources.getString(R.string.title_workflow)
            }
        }

    internal suspend fun showComponentSheetDialog(
        context: Context,
        componentData: ComponentData,
    ) = withContext(dispatcher) {
        suspendCoroutine {
            ComponentBuilder(context, componentData)
                .onApply { name, query, _ ->
                    executeContinuation(it, name, query)
                }
                .onReset { name, query, _ ->
                    executeContinuation(it, name, query)
                }
                .onCancel {
                    it.resume(null)
                }
                .show()
        }
    }

    internal suspend fun showSearchUserGroupComponentDialog(
        context: Context,
        processEntry: ProcessEntry,
    ) = withContext(dispatcher) {
        suspendCoroutine {
            SearchUserGroupComponentBuilder(context, processEntry)
                .onApply { userDetails ->
                    it.resume(userDetails)
                }
                .onCancel {
                    it.resume(null)
                }
                .show()
        }
    }

    /**
     * It will prompt if user trying to start workflow and if any of content file is in uploaded
     */
    fun confirmContentQueuePrompt() {
        val oldDialog = confirmContentQueueDialog.get()
        if (oldDialog != null && oldDialog.isShowing) return
        val dialog =
            MaterialAlertDialogBuilder(requireContext())
                .setCancelable(false)
                .setTitle(getString(R.string.title_content_in_queue))
                .setMessage(getString(R.string.message_content_in_queue))
                .setNegativeButton(getString(R.string.dialog_negative_button_task), null)
                .setPositiveButton(getString(R.string.dialog_positive_button_task)) { _, _ ->
                    viewModel.startWorkflow()
                }
                .show()
        confirmContentQueueDialog = WeakReference(dialog)
    }

    private fun epoxyAttachmentController() =
        simpleController(viewModel) { state ->
            val handler = Handler(Looper.getMainLooper())
            if (state.listContents.isNotEmpty()) {
                handler.post {
                    binding.clAddAttachment.visibility = View.VISIBLE
                    binding.tvNoAttachedFilesError.visibility = View.GONE
                    binding.tvAttachedTitle.text = getString(R.string.text_attached_files)
                    binding.recyclerViewAttachments.visibility = View.VISIBLE

                    if (state.listContents.size > 1) {
                        binding.tvNoOfAttachments.visibility = View.VISIBLE
                    } else {
                        binding.tvNoOfAttachments.visibility = View.GONE
                    }

                    if (state.listContents.size > 4) {
                        binding.tvAttachmentViewAll.visibility = View.VISIBLE
                    } else {
                        binding.tvAttachmentViewAll.visibility = View.GONE
                    }

                    binding.tvNoOfAttachments.text = getString(R.string.text_multiple_attachment, state.listContents.size)
                }

                state.listContents.take(4).forEach { obj ->
                    listViewAttachmentRow {
                        id(stableId(obj))
                        data(obj)
                        deleteContentClickListener { model, _, _, _ -> onConfirmDelete(model.data().id) }
                    }
                }
            } else {
                handler.post {
                    binding.recyclerViewAttachments.visibility = View.GONE
                    binding.tvAttachmentViewAll.visibility = View.GONE
                    binding.tvNoOfAttachments.visibility = View.GONE

                    binding.clAddAttachment.visibility = View.VISIBLE
                    binding.tvAttachedTitle.text = getString(R.string.text_attached_files)
                    binding.tvNoAttachedFilesError.visibility = View.VISIBLE
                    binding.tvNoAttachedFilesError.text = getString(R.string.no_attached_files)
                }
            }
        }

    override fun onEntryCreated(entry: ParentEntry) {
        if (isAdded) {
            localViewerIntent(entry as Entry)
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
}
