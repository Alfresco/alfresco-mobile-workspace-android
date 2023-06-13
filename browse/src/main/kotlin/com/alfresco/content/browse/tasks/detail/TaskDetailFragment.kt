package com.alfresco.content.browse.tasks.detail

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
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
import com.alfresco.content.DATE_FORMAT_1
import com.alfresco.content.DATE_FORMAT_4
import com.alfresco.content.actions.ActionOpenWith
import com.alfresco.content.browse.R
import com.alfresco.content.browse.databinding.FragmentTaskDetailBinding
import com.alfresco.content.browse.databinding.ViewListCommentRowBinding
import com.alfresco.content.browse.tasks.BaseDetailFragment
import com.alfresco.content.browse.tasks.TaskViewerActivity
import com.alfresco.content.browse.tasks.attachments.listViewAttachmentRow
import com.alfresco.content.component.ComponentBuilder
import com.alfresco.content.component.ComponentData
import com.alfresco.content.component.ComponentMetaData
import com.alfresco.content.component.searchusergroup.SearchUserGroupComponentBuilder
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.CommentEntry
import com.alfresco.content.data.Entry
import com.alfresco.content.data.EventName
import com.alfresco.content.data.PageView
import com.alfresco.content.data.ParentEntry
import com.alfresco.content.data.TaskEntry
import com.alfresco.content.data.UploadServerType
import com.alfresco.content.data.UserGroupDetails
import com.alfresco.content.getFormattedDate
import com.alfresco.content.getLocalizedName
import com.alfresco.content.listview.EntryListener
import com.alfresco.content.mimetype.MimeType
import com.alfresco.content.simpleController
import com.alfresco.ui.getDrawableForAttribute
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.ref.WeakReference
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Marked as TaskDetailFragment class
 */
class TaskDetailFragment : BaseDetailFragment(), MavericksView, EntryListener {

    val viewModel: TaskDetailViewModel by activityViewModel()
    lateinit var binding: FragmentTaskDetailBinding
    lateinit var commentViewBinding: ViewListCommentRowBinding
    private val epoxyAttachmentController: AsyncEpoxyController by lazy { epoxyAttachmentController() }
    private var taskCompleteConfirmationDialog = WeakReference<AlertDialog>(null)
    private var discardTaskDialog = WeakReference<AlertDialog>(null)
    private var viewLayout: View? = null
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
    lateinit var menuDetail: Menu

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (viewLayout == null) {
            binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
            commentViewBinding = ViewListCommentRowBinding.bind(binding.root)
            viewLayout = binding.root
        }
        return viewLayout as View
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AnalyticsManager().screenViewEvent(PageView.TaskView)
        (requireActivity() as TaskViewerActivity).setSupportActionBar(binding.toolbar)
        withState(viewModel) { state ->
            if (!viewModel.isWorkflowTask && !viewModel.isTaskCompleted(state)) {
                setHasOptionsMenu(true)
            }
        }

        binding.toolbar.apply {
            navigationContentDescription = getString(R.string.label_navigation_back)
            navigationIcon = requireContext().getDrawableForAttribute(R.attr.homeAsUpIndicator)
            setNavigationOnClickListener {
                withState(viewModel) { state ->
                    if (!viewModel.isWorkflowTask && (viewModel.isTaskAssigneeChanged(state) || viewModel.isTaskDetailsChanged(state)))
                        discardTaskPrompt()
                    else requireActivity().onBackPressed()
                }
            }
            title = resources.getString(R.string.title_task_view)
        }
        binding.recyclerViewAttachments.setController(epoxyAttachmentController)

        setListeners()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_task_detail, menu)
        menuDetail = menu
        withState(viewModel) { state ->
            if (state.parent?.isNewTaskCreated == true) {
                menu.findItem(R.id.action_edit).isVisible = false
                menu.findItem(R.id.action_done).isVisible = true
                updateTaskDetailUI(true)
            } else {
                menu.findItem(R.id.action_done).isVisible = false
                menu.findItem(R.id.action_edit).isVisible = true
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit -> {
                AnalyticsManager().taskEvent(EventName.EditTask)
                item.isVisible = false
                updateTaskDetailUI(true)
                menuDetail.findItem(R.id.action_done).isVisible = true
                true
            }

            R.id.action_done -> {
                AnalyticsManager().taskEvent(EventName.UpdateTaskDetails)
                withState(viewModel) { state ->
                    if (viewModel.isTaskDetailsChanged(state)) {
                        viewModel.isExecutingUpdateDetails = true
                        viewModel.updateTaskDetails()
                    }

                    if (viewModel.isTaskAssigneeChanged(state)) {
                        viewModel.isExecutingAssignUser = true
                        viewModel.assignUser()
                    }

                    if (!viewModel.isExecutingUpdateDetails && !viewModel.isExecutingAssignUser) {
                        AnalyticsManager().taskEvent(EventName.DoneTask)
                        item.isVisible = false
                        menuDetail.findItem(R.id.action_edit).isVisible = true
                        updateTaskDetailUI(false)
                    }
                }

                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onConfirmDelete(contentId: String) {
        viewModel.deleteAttachment(contentId)
    }

    override fun invalidate() = withState(viewModel) { state ->

        binding.loading.isVisible = (state.request is Loading && state.parent != null) ||
                (state.requestComments is Loading && state.listComments.isEmpty()) ||
                (state.requestContents is Loading && state.listContents.isEmpty()) ||
                (state.requestCompleteTask is Loading) || (state.requestUpdateTask is Loading) ||
                (state.requestDeleteContent is Loading) || (state.requestTaskForm is Loading) ||
                (state.requestOutcomes is Loading)

        setData(state)

        setCommentData(state.listComments)

        binding.completeButton.visibility = if (viewModel.isCompleteButtonVisible(state)) View.VISIBLE else View.GONE

        when {
            (state.requestCompleteTask.invoke()?.code() == 200) || (state.requestOutcomes.invoke()?.code() == 200) -> {
                viewModel.updateTaskList()
                requireActivity().onBackPressed()
            }

            state.requestUpdateTask is Success -> {
                if (!viewModel.isExecutingUpdateDetails && !viewModel.isExecutingAssignUser) {
                    AnalyticsManager().taskEvent(EventName.DoneTask)
                    menuDetail.findItem(R.id.action_done).isVisible = false
                    menuDetail.findItem(R.id.action_edit).isVisible = true
                    updateTaskDetailUI(false)
                    viewModel.copyEntry(state.parent)
                    viewModel.resetUpdateTaskRequest()
                    viewModel.updateTaskList()
                }
            }
        }

        if (state.requestContents.complete || (viewModel.isWorkflowTask && state.requestTaskForm.complete))
            epoxyAttachmentController.requestModelBuild()
    }

    private fun setCommentData(listComments: List<CommentEntry>) {
        if (listComments.isNotEmpty()) {
            binding.flRecentComment.visibility = View.VISIBLE
            binding.clCommentHeader.visibility = View.VISIBLE
            val commentObj = listComments.last()

            commentViewBinding.tvComment.maxLines = 4
            commentViewBinding.tvComment.ellipsize = TextUtils.TruncateAt.END

            if (listComments.size > 1) {
                binding.tvCommentViewAll.visibility = View.VISIBLE
                binding.tvNoOfComments.visibility = View.VISIBLE
                binding.tvNoOfComments.text = getString(R.string.text_multiple_comments, listComments.size)
            } else {
                binding.tvCommentViewAll.visibility = View.GONE
                binding.tvNoOfComments.visibility = View.GONE
            }

            commentViewBinding.tvUserInitial.text = requireContext().getLocalizedName(commentObj.userGroupDetails?.nameInitial ?: "")
            commentViewBinding.tvName.text = requireContext().getLocalizedName(commentObj.userGroupDetails?.name ?: "")
            commentViewBinding.tvDate.text = if (commentObj.created != null) commentObj.created?.toLocalDate().toString().getFormattedDate(DATE_FORMAT_1, DATE_FORMAT_4) else ""
            commentViewBinding.tvComment.text = commentObj.message
        } else {
            binding.clCommentHeader.visibility = View.GONE
            binding.flRecentComment.visibility = View.GONE
        }
    }

    private fun setData(state: TaskDetailViewState) {
        val dataObj = state.parent
        if (dataObj != null) {
            binding.tvTitle.text = dataObj.name
            if (viewModel.hasTaskEditMode)
                updateTaskDetailUI(true)

            if (viewModel.isWorkflowTask) {
                enableTaskFormUI()
            }

            binding.tvAssignedValue.apply {
                text = if (viewModel.getAPSUser().id == dataObj.assignee?.id) {
                    requireContext().getLocalizedName(dataObj.assignee?.let { UserGroupDetails.with(it).name } ?: "")
                } else requireContext().getLocalizedName(dataObj.assignee?.name ?: "")
            }
            binding.tvIdentifierValue.text = dataObj.id
            setTaskDetailAfterResponse(dataObj)
        }
    }

    private fun epoxyAttachmentController() = simpleController(viewModel) { state ->
        val handler = Handler(Looper.getMainLooper())
        if (state.listContents.isNotEmpty()) {
            handler.post {
                binding.clAddAttachment.visibility = View.VISIBLE
                binding.tvNoAttachedFilesError.visibility = View.GONE
                binding.tvAttachedTitle.text = getString(R.string.text_attached_files)
                binding.recyclerViewAttachments.visibility = View.VISIBLE

                if (state.listContents.size > 1)
                    binding.tvNoOfAttachments.visibility = View.VISIBLE
                else binding.tvNoOfAttachments.visibility = View.GONE

                if (state.listContents.size > 4)
                    binding.tvAttachmentViewAll.visibility = View.VISIBLE
                else
                    binding.tvAttachmentViewAll.visibility = View.GONE

                binding.clAddAttachment.isVisible = !viewModel.isTaskCompleted(state) && !viewModel.isWorkflowTask

                binding.tvNoOfAttachments.text = getString(R.string.text_multiple_attachment, state.listContents.size)
            }

            state.listContents.take(4).forEach { obj ->
                listViewAttachmentRow {
                    id(stableId(obj))
                    data(obj)
                    deleteButtonVisibility(!viewModel.isWorkflowTask)
                    clickListener { model, _, _, _ -> onItemClicked(model.data()) }
                    deleteContentClickListener { model, _, _, _ -> deleteContentPrompt(model.data()) }
                }
            }
        } else {
            handler.post {
                binding.recyclerViewAttachments.visibility = View.GONE
                binding.tvAttachmentViewAll.visibility = View.GONE
                binding.tvNoOfAttachments.visibility = View.GONE
                if (!viewModel.isTaskCompleted(state)) {
                    binding.clAddAttachment.visibility = if (!viewModel.isWorkflowTask) View.VISIBLE else View.GONE
                    binding.tvAttachedTitle.text = getString(R.string.text_attached_files)
                    binding.tvNoAttachedFilesError.visibility = View.VISIBLE
                    binding.tvNoAttachedFilesError.text = getString(R.string.no_attached_files)
                } else {
                    binding.clAddAttachment.visibility = View.GONE
                    binding.tvAttachedTitle.text = ""
                    binding.tvNoAttachedFilesError.visibility = View.GONE
                }
            }
        }
    }

    private fun onItemClicked(contentEntry: Entry) {
        if (!contentEntry.isUpload) {
            val entry = Entry.convertContentEntryToEntry(
                contentEntry,
                MimeType.isDocFile(contentEntry.mimeType), UploadServerType.UPLOAD_TO_TASK
            )
            if (!contentEntry.source.isNullOrEmpty())
                remoteViewerIntent(entry)
            else
                viewModel.executePreview(ActionOpenWith(entry))
        } else localViewerIntent(contentEntry)
    }

    internal fun taskCompletePrompt(filesInQueue: Boolean) {
        val oldDialog = taskCompleteConfirmationDialog.get()
        if (oldDialog != null && oldDialog.isShowing) return
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.dialog_title_complete_task))
            .setMessage(if (filesInQueue) getString(R.string.dialog_message_complete_task_files_queue) else getString(R.string.dialog_message_complete_task))
            .setNegativeButton(getString(R.string.dialog_negative_button_task), null)
            .setPositiveButton(getString(R.string.dialog_positive_button_task)) { _, _ ->
                if (filesInQueue) {
                    withState(viewModel) { state ->
                        viewModel.removeTaskEntries(state)
                    }
                }
                AnalyticsManager().taskEvent(EventName.TaskComplete)
                viewModel.completeTask()
            }
            .show()
        taskCompleteConfirmationDialog = WeakReference(dialog)
    }

    private fun discardTaskPrompt() {
        val oldDialog = discardTaskDialog.get()
        if (oldDialog != null && oldDialog.isShowing) return
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setCancelable(false)
            .setTitle(getString(R.string.dialog_title_discard_task))
            .setMessage(getString(R.string.dialog_message_discard_task))
            .setNegativeButton(getString(R.string.dialog_negative_button_task), null)
            .setPositiveButton(getString(R.string.dialog_positive_button_task)) { _, _ ->
                requireActivity().onBackPressed()
            }
            .show()
        discardTaskDialog = WeakReference(dialog)
    }

    override fun onEntryCreated(entry: ParentEntry) {
        if (isAdded)
            localViewerIntent(entry as Entry)
    }

    internal suspend fun showComponentSheetDialog(
        context: Context,
        componentData: ComponentData
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

    internal suspend fun showSearchUserComponentDialog(
        context: Context,
        taskEntry: TaskEntry
    ) = withContext(dispatcher) {
        suspendCoroutine {

            SearchUserGroupComponentBuilder(context, taskEntry)
                .onApply { userDetails ->
                    it.resume(userDetails)
                }
                .onCancel {
                    it.resume(null)
                }
                .show()
        }
    }

    private fun executeContinuation(continuation: Continuation<ComponentMetaData?>, name: String, query: String) {
        continuation.resume(ComponentMetaData(name = name, query = query))
    }
}
