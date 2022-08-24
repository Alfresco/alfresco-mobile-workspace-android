package com.alfresco.content.browse.tasks.detail

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.activityViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.DATE_FORMAT_1
import com.alfresco.content.DATE_FORMAT_4
import com.alfresco.content.actions.ActionOpenWith
import com.alfresco.content.browse.R
import com.alfresco.content.browse.databinding.FragmentTaskDetailBinding
import com.alfresco.content.browse.databinding.ViewListCommentRowBinding
import com.alfresco.content.browse.preview.LocalPreviewActivity
import com.alfresco.content.browse.preview.LocalPreviewActivity.Companion.KEY_MIME_TYPE
import com.alfresco.content.browse.preview.LocalPreviewActivity.Companion.KEY_PATH
import com.alfresco.content.browse.preview.LocalPreviewActivity.Companion.KEY_TITLE
import com.alfresco.content.browse.tasks.attachments.listViewAttachmentRow
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.CommentEntry
import com.alfresco.content.data.ContentEntry
import com.alfresco.content.data.Entry
import com.alfresco.content.data.PageView
import com.alfresco.content.data.TaskEntry
import com.alfresco.content.getDateZoneFormat
import com.alfresco.content.listview.EntryListener
import com.alfresco.content.listview.addReadMore
import com.alfresco.content.listview.updatePriorityView
import com.alfresco.content.simpleController
import com.alfresco.ui.getDrawableForAttribute
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.ref.WeakReference

/**
 * Marked as TaskDetailFragment class
 */
class TaskDetailFragment : Fragment(), MavericksView, EntryListener {

    val viewModel: TaskDetailViewModel by activityViewModel()
    private lateinit var binding: FragmentTaskDetailBinding
    private lateinit var commentViewBinding: ViewListCommentRowBinding
    private val epoxyAttachmentController: AsyncEpoxyController by lazy { epoxyAttachmentController() }
    private var taskCompleteConfirmationDialog = WeakReference<AlertDialog>(null)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
        commentViewBinding = ViewListCommentRowBinding.bind(binding.root)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AnalyticsManager().screenViewEvent(PageView.TaskView)

        binding.toolbar.apply {
            navigationContentDescription = getString(R.string.label_navigation_back)
            navigationIcon = requireContext().getDrawableForAttribute(R.attr.homeAsUpIndicator)
            setNavigationOnClickListener { requireActivity().onBackPressed() }
            title = resources.getString(R.string.title_task_view)
        }
        binding.recyclerViewAttachments.setController(epoxyAttachmentController)

        setListeners()
    }

    private fun setListeners() {
        viewModel.setListener(this)
        binding.tvAddComment.setOnClickListener {
            viewModel.isAddComment = true
            navigateToCommentScreen()
        }
        binding.tvCommentViewAll.setOnClickListener {
            navigateToCommentScreen()
        }
        commentViewBinding.clRecentComment.setOnClickListener {
            navigateToCommentScreen()
        }
        binding.tvAttachmentViewAll.setOnClickListener {
            findNavController().navigate(R.id.action_nav_task_detail_to_nav_attached_files)
        }

        binding.completeButton.setOnClickListener {
            taskCompletePrompt()
        }
    }

    private fun navigateToCommentScreen() {
        findNavController().navigate(R.id.action_nav_task_detail_to_nav_comments)
    }

    override fun invalidate() = withState(viewModel) { state ->

        binding.loading.isVisible = (state.request is Loading && state.parent != null) ||
                (state.requestComments is Loading && state.listComments.isEmpty()) ||
                (state.requestContents is Loading && state.listContents.isEmpty()) ||
                (state.requestCompleteTask is Loading)

        setData(state.parent)

        setCommentData(state.listComments)

        binding.completeButton.visibility = if (viewModel.isCompleteButtonVisible(state)) View.VISIBLE else View.GONE

        if (state.requestCompleteTask.invoke()?.code() == 200) {
            viewModel.updateTaskList()
            requireActivity().onBackPressed()
        }

        if (state.requestContents.complete)
            epoxyAttachmentController.requestModelBuild()
    }

    private fun setCommentData(listComments: List<CommentEntry>) {
        if (listComments.isNotEmpty()) {
            binding.flRecentComment.visibility = View.VISIBLE
            binding.clCommentHeader.visibility = View.VISIBLE
            val commentObj = listComments.last()

            if (listComments.size > 1) {
                binding.tvCommentViewAll.visibility = View.VISIBLE
                binding.tvNoOfComments.visibility = View.VISIBLE
                binding.tvNoOfComments.text = getString(R.string.text_multiple_comments, listComments.size)
            } else {
                binding.tvCommentViewAll.visibility = View.GONE
                binding.tvNoOfComments.visibility = View.GONE
            }

            commentViewBinding.tvUserInitial.text = commentObj.userDetails?.nameInitial
            commentViewBinding.tvName.text = commentObj.userDetails?.name
            commentViewBinding.tvDate.text = if (commentObj.created != null) commentObj.created?.toLocalDate().toString().getDateZoneFormat(DATE_FORMAT_1, DATE_FORMAT_4) else ""
            commentViewBinding.tvComment.text = commentObj.message
            commentViewBinding.tvComment.post {
                commentViewBinding.tvComment.addReadMore()
            }
        } else {
            binding.clCommentHeader.visibility = View.GONE
            binding.flRecentComment.visibility = View.GONE
        }
    }

    private fun setData(dataObj: TaskEntry?) {
        if (dataObj != null) {
            binding.tvTaskTitle.text = dataObj.name
            binding.tvDueDateValue.text =
                if (dataObj.dueDate != null) dataObj.dueDate?.toLocalDate().toString().getDateZoneFormat(DATE_FORMAT_1, DATE_FORMAT_4) else requireContext().getString(R.string.empty_no_due_date)
            binding.tvPriorityValue.updatePriorityView(dataObj.priority)
            binding.tvAssignedValue.text = dataObj.assignee?.name
            binding.tvStatusValue.text = if (dataObj.endDate == null) getString(R.string.status_active) else getString(R.string.status_completed)
            binding.tvIdentifierValue.text = dataObj.id

            if (dataObj.endDate != null) {
                binding.tvAddComment.visibility = View.GONE
                binding.iconAddCommentUser.visibility = View.GONE
            }
        }
    }

    private fun epoxyAttachmentController() = simpleController(viewModel) { state ->
        binding.tvAttachedFilesTitle.visibility = View.VISIBLE
        if (state.listContents.isNotEmpty()) {
            binding.recyclerViewAttachments.visibility = View.VISIBLE
            binding.tvNoAttachedFilesError.visibility = View.GONE
            binding.clAttachmentHeader.visibility = View.VISIBLE

            if (state.listContents.size > 1)
                binding.tvNoOfAttachments.visibility = View.VISIBLE
            else binding.tvNoOfAttachments.visibility = View.GONE

            if (state.listContents.size > 4)
                binding.tvAttachmentViewAll.visibility = View.VISIBLE
            else
                binding.tvAttachmentViewAll.visibility = View.GONE

            binding.tvNoOfAttachments.text = getString(R.string.text_multiple_attachment, state.listContents.size)

            state.listContents.take(4).forEach { obj ->
                listViewAttachmentRow {
                    id(obj.id)
                    data(obj)
                    clickListener { model, _, _, _ -> onItemClicked(model.data()) }
                }
            }
        } else {
            binding.recyclerViewAttachments.visibility = View.GONE
            binding.tvAttachmentViewAll.visibility = View.GONE
            binding.tvNoOfAttachments.visibility = View.GONE
            binding.tvNoAttachedFilesError.visibility = View.VISIBLE
            binding.tvNoAttachedFilesError.text = getString(R.string.no_attached_files)
        }
    }

    private fun onItemClicked(contentEntry: ContentEntry) {
        viewModel.execute(ActionOpenWith(Entry.convertContentEntryToEntry(contentEntry)))
    }

    private fun taskCompletePrompt() {
        val oldDialog = taskCompleteConfirmationDialog.get()
        if (oldDialog != null && oldDialog.isShowing) return
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.dialog_title_complete_task))
            .setMessage(getString(R.string.dialog_message_complete_task))
            .setNegativeButton(getString(R.string.dialog_negative_button_complete_task), null)
            .setPositiveButton(getString(R.string.dialog_positive_button_complete_task)) { _, _ ->
                viewModel.completeTask()
            }
            .show()
        taskCompleteConfirmationDialog = WeakReference(dialog)
    }

    override fun onEntryCreated(entry: Entry) {
        if (isAdded)
            entry.mimeType?.let {
                startActivity(
                    Intent(requireActivity(), LocalPreviewActivity::class.java)
                        .putExtra(KEY_PATH, entry.path)
                        .putExtra(KEY_MIME_TYPE, it)
                        .putExtra(KEY_TITLE, entry.name)
                )
            }
    }
}
