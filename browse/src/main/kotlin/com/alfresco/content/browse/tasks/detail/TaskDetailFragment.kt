package com.alfresco.content.browse.tasks.detail

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.InternalMavericksApi
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.withState
import com.alfresco.content.browse.R
import com.alfresco.content.browse.databinding.FragmentTaskDetailBinding
import com.alfresco.content.component.getDateZoneFormat
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.PageView
import com.alfresco.content.data.TaskEntry
import com.alfresco.content.fragmentViewModelWithArgs
import com.alfresco.content.listview.tasks.updatePriorityView
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.apply {
            navigationIcon = requireContext().getDrawableForAttribute(R.attr.homeAsUpIndicator)
            setNavigationOnClickListener { requireActivity().onBackPressed() }
            title = resources.getString(R.string.title_task_view)
        }
    }

    override fun invalidate() = withState(viewModel) { state ->

        binding.loading.isVisible = state.request is Loading && state.taskDetailObj != null

        setData(state.taskDetailObj)
    }

    private fun setData(dataObj: TaskEntry?) {
        if (dataObj != null) {
            binding.tvTaskTitle.text = dataObj.name
            binding.tvDueDateValue.text = if (dataObj.dueDate != null) dataObj.dueDate?.toLocalDate().toString().getDateZoneFormat() else "N/A"
            binding.tvPriorityValue.updatePriorityView(dataObj.priority)
            binding.tvAssignedValue.text = dataObj.assignee?.name
            binding.tvStatusValue.text = if (dataObj.endDate == null) getString(R.string.status_active) else getString(R.string.status_completed)
            binding.tvIdentifierValue.text = dataObj.id
        }
    }
}
