package com.alfresco.content.browse.tasks.detail

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.mvrx.InternalMavericksApi
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.withState
import com.alfresco.content.DATE_FORMAT_1
import com.alfresco.content.DATE_FORMAT_3
import com.alfresco.content.browse.R
import com.alfresco.content.browse.databinding.FragmentTaskDetailBinding
import com.alfresco.content.browse.databinding.ViewListCommentRowBinding
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.CommentEntry
import com.alfresco.content.data.PageView
import com.alfresco.content.data.TaskEntry
import com.alfresco.content.fragmentViewModelWithArgs
import com.alfresco.content.getDateZoneFormat
import com.alfresco.content.listview.addReadMore
import com.alfresco.content.listview.updatePriorityView
import com.alfresco.content.simpleController
import com.alfresco.ui.getDrawableForAttribute
import kotlinx.parcelize.Parcelize

/**
 * Mark as TaskDetailsArgs class
 */
@Parcelize
data class TaskDetailsArgs(
    val taskObj: TaskEntry?
) : Parcelable {
    companion object {
        const val TASK_OBJ = "taskObj"

        /**
         * return the TaskDetailsArgs obj
         */
        fun with(args: Bundle): TaskDetailsArgs {
            return TaskDetailsArgs(
                args.getParcelable(TASK_OBJ)
            )
        }
    }
}

/**
 * Marked as TaskDetailFragment class
 */
class TaskDetailFragment : Fragment(), MavericksView {

    private lateinit var args: TaskDetailsArgs

    @OptIn(InternalMavericksApi::class)
    val viewModel: TaskDetailViewModel by fragmentViewModelWithArgs { args }
    private lateinit var binding: FragmentTaskDetailBinding
    private lateinit var commentViewBinding: ViewListCommentRowBinding
    private val epoxyAttachmentController: AsyncEpoxyController by lazy { epoxyAttachmentController() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AnalyticsManager().screenViewEvent(PageView.TaskView)
        args = TaskDetailsArgs.with(requireArguments())
    }

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
        binding.toolbar.apply {
            navigationIcon = requireContext().getDrawableForAttribute(R.attr.homeAsUpIndicator)
            setNavigationOnClickListener { requireActivity().onBackPressed() }
            title = resources.getString(R.string.title_task_view)
        }
        binding.recyclerViewAttachments.setController(epoxyAttachmentController)
    }

    override fun invalidate() = withState(viewModel) { state ->

        binding.loading.isVisible = state.request is Loading && state.taskDetailObj != null

        setData(state.taskDetailObj)

        setCommentData(state.listComments)

        epoxyAttachmentController.requestModelBuild()
    }

    private fun setCommentData(listComments: List<CommentEntry>) {
        if (listComments.isNotEmpty()) {
            binding.clCommentHeader.visibility = View.VISIBLE
            binding.flRecentComment.visibility = View.VISIBLE
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
            binding.tvDueDateValue.text = if (dataObj.dueDate != null) dataObj.dueDate?.toLocalDate().toString().getDateZoneFormat(DATE_FORMAT_1, DATE_FORMAT_3) else "N/A"
            binding.tvPriorityValue.updatePriorityView(dataObj.priority)
            binding.tvAssignedValue.text = dataObj.assignee?.name
            binding.tvStatusValue.text = if (dataObj.endDate == null) getString(R.string.status_active) else getString(R.string.status_completed)
            binding.tvIdentifierValue.text = dataObj.id
        }
    }

    private fun epoxyAttachmentController() = simpleController(viewModel) { state ->
        if (state.listContents.isNotEmpty()) {
            binding.clAttachmentHeader.visibility = View.VISIBLE
            binding.tvNoOfAttachments.text = getString(R.string.text_multiple_attachment, state.listContents.size)

            state.listContents.take(4).forEach { obj ->
                listViewAttachmentRow {
                    id(obj.id)
                    data(obj)
                }
            }
        } else binding.clAttachmentHeader.visibility = View.GONE
    }
}
