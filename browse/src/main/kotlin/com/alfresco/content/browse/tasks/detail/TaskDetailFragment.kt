package com.alfresco.content.browse.tasks.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
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
import com.alfresco.content.browse.preview.LocalPreviewActivity.Companion.KEY_ENTRY_OBJ
import com.alfresco.content.browse.tasks.attachments.listViewAttachmentRow
import com.alfresco.content.component.ComponentBuilder
import com.alfresco.content.component.ComponentData
import com.alfresco.content.component.ComponentMetaData
import com.alfresco.content.component.ComponentType
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.CommentEntry
import com.alfresco.content.data.ContentEntry
import com.alfresco.content.data.Entry
import com.alfresco.content.data.EventName
import com.alfresco.content.data.PageView
import com.alfresco.content.data.ParentEntry
import com.alfresco.content.getDateZoneFormat
import com.alfresco.content.listview.EntryListener
import com.alfresco.content.listview.addTextViewPrefix
import com.alfresco.content.listview.updatePriorityView
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Marked as TaskDetailFragment class
 */
class TaskDetailFragment : Fragment(), MavericksView, EntryListener {

    val viewModel: TaskDetailViewModel by activityViewModel()
    private lateinit var binding: FragmentTaskDetailBinding
    private lateinit var commentViewBinding: ViewListCommentRowBinding
    private val epoxyAttachmentController: AsyncEpoxyController by lazy { epoxyAttachmentController() }
    private var taskCompleteConfirmationDialog = WeakReference<AlertDialog>(null)
    private var viewLayout: View? = null
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main

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

        setData(state)

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

            commentViewBinding.tvUserInitial.text = commentObj.userDetails?.nameInitial
            commentViewBinding.tvName.text = commentObj.userDetails?.name
            commentViewBinding.tvDate.text = if (commentObj.created != null) commentObj.created?.toLocalDate().toString().getDateZoneFormat(DATE_FORMAT_1, DATE_FORMAT_4) else ""
            commentViewBinding.tvComment.text = commentObj.message
        } else {
            binding.clCommentHeader.visibility = View.GONE
            binding.flRecentComment.visibility = View.GONE
        }
    }

    private fun setData(state: TaskDetailViewState) {
        val dataObj = state.parent
        if (dataObj != null) {
            binding.tvTaskTitle.text = dataObj.name
            binding.tvDueDateValue.text =
                if (dataObj.dueDate != null) dataObj.dueDate?.toLocalDate().toString().getDateZoneFormat(DATE_FORMAT_1, DATE_FORMAT_4) else requireContext().getString(R.string.empty_no_due_date)
            binding.tvPriorityValue.updatePriorityView(dataObj.priority)
            binding.tvAssignedValue.text = dataObj.assignee?.name
            binding.tvIdentifierValue.text = dataObj.id
            binding.tvTaskDescription.text = if (dataObj.description.isNullOrEmpty()) requireContext().getString(R.string.empty_description) else dataObj.description

            binding.tvTaskDescription.addTextViewPrefix(requireContext().getString(R.string.text_view_all)) {
                viewLifecycleOwner.lifecycleScope.launch {
                    showComponentSheetDialog(requireContext(), ComponentData(value = dataObj.description, selector = ComponentType.VIEW_TEXT.value))
                }
            }

            if (viewModel.isTaskCompleted(state)) {
                binding.tvAddComment.visibility = View.GONE
                binding.iconAddCommentUser.visibility = View.GONE
                binding.iconCompleted.visibility = View.VISIBLE
                binding.tvCompletedTitle.visibility = View.VISIBLE
                binding.tvCompletedValue.visibility = View.VISIBLE
                if (state.listComments.isEmpty()) binding.viewComment2.visibility = View.GONE else View.VISIBLE
                binding.tvCompletedValue.text = dataObj.endDate?.toLocalDate().toString().getDateZoneFormat(DATE_FORMAT_1, DATE_FORMAT_4)
                (binding.iconDueDate.layoutParams as ConstraintLayout.LayoutParams).apply {
                    topMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, resources.displayMetrics).toInt()
                }
                binding.iconStatus.visibility = View.GONE
                binding.tvStatusTitle.visibility = View.GONE
                binding.tvStatusValue.visibility = View.GONE
            } else {
                binding.iconCompleted.visibility = View.GONE
                binding.tvCompletedTitle.visibility = View.GONE
                binding.tvCompletedValue.visibility = View.GONE
                (binding.iconDueDate.layoutParams as ConstraintLayout.LayoutParams).apply {
                    topMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0f, resources.displayMetrics).toInt()
                }
                binding.iconStatus.visibility = View.VISIBLE
                binding.tvStatusTitle.visibility = View.VISIBLE
                binding.tvStatusValue.visibility = View.VISIBLE
                binding.tvStatusValue.text = getString(R.string.status_active)
            }
        }
    }

    private fun epoxyAttachmentController() = simpleController(viewModel) { state ->
        if (state.listContents.isNotEmpty()) {
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
            if (!viewModel.isTaskCompleted(state)) {
                binding.tvAttachedTitle.text = getString(R.string.text_attached_files)
                binding.tvNoAttachedFilesError.visibility = View.VISIBLE
                binding.tvNoAttachedFilesError.text = getString(R.string.no_attached_files)
            } else {
                binding.tvAttachedTitle.text = ""
                binding.tvNoAttachedFilesError.visibility = View.GONE
            }
        }
    }

    private fun onItemClicked(contentEntry: ContentEntry) {
        viewModel.execute(ActionOpenWith(Entry.convertContentEntryToEntry(contentEntry, MimeType.isDocFile(contentEntry.mimeType))))
    }

    private fun taskCompletePrompt() {
        val oldDialog = taskCompleteConfirmationDialog.get()
        if (oldDialog != null && oldDialog.isShowing) return
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.dialog_title_complete_task))
            .setMessage(getString(R.string.dialog_message_complete_task))
            .setNegativeButton(getString(R.string.dialog_negative_button_complete_task), null)
            .setPositiveButton(getString(R.string.dialog_positive_button_complete_task)) { _, _ ->
                AnalyticsManager().taskEvent(EventName.TaskComplete)
                viewModel.completeTask()
            }
            .show()
        taskCompleteConfirmationDialog = WeakReference(dialog)
    }

    override fun onEntryCreated(entry: ParentEntry) {
        if (isAdded)
            startActivity(
                Intent(requireActivity(), LocalPreviewActivity::class.java)
                    .putExtra(KEY_ENTRY_OBJ, entry as Entry)
            )
    }

    private suspend fun showComponentSheetDialog(
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

    private fun executeContinuation(continuation: Continuation<ComponentMetaData?>, name: String, query: String) {
        continuation.resume(ComponentMetaData(name = name, query = query))
    }
}
