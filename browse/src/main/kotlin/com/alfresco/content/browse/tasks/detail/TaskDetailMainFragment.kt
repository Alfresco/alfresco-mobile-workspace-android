package com.alfresco.content.browse.tasks.detail

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.InternalMavericksApi
import com.airbnb.mvrx.MavericksView
import com.alfresco.content.browse.R
import com.alfresco.content.browse.databinding.FragmentTaskDetailMainBinding
import com.alfresco.content.data.TaskEntry
import com.alfresco.content.fragmentViewModelWithArgs
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
 * Marked as TaskDetailMainFragment class
 */
class TaskDetailMainFragment : Fragment(), MavericksView {

    private lateinit var args: TaskDetailsArgs

    @OptIn(InternalMavericksApi::class)
    val viewModel: TaskDetailViewModel by fragmentViewModelWithArgs { args }
    private lateinit var binding: FragmentTaskDetailMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        args = TaskDetailsArgs.with(requireArguments())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTaskDetailMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.apply {
            navigationContentDescription = getString(R.string.label_navigation_back)
            navigationIcon = requireContext().getDrawableForAttribute(R.attr.homeAsUpIndicator)
            setNavigationOnClickListener { goBack() }
        }
        enterTaskDetailScreen()
    }

    private fun goBack() {
        when (viewModel.path) {
            TaskPath.COMMENTS -> {
                viewModel.path = TaskPath.TASK_DETAILS
                binding.toolbar.title = resources.getString(R.string.title_task_view)
                binding.commentFragment.visibility = View.GONE
                binding.taskDetailFragment.visibility = View.VISIBLE
            }
            else -> requireActivity().onBackPressed()
        }
    }

    override fun invalidate() {
        // TODO
    }

    /**
     * show the CommentsFragment Screen
     */
    fun enterCommentsScreen() {
        binding.toolbar.title = resources.getString(R.string.title_comments)
        viewModel.path = TaskPath.COMMENTS
        binding.taskDetailFragment.visibility = View.GONE
        binding.commentFragment.visibility = View.VISIBLE
    }

    private fun enterTaskDetailScreen() {
        binding.toolbar.title = resources.getString(R.string.title_task_view)
        viewModel.path = TaskPath.TASK_DETAILS
    }
}

/**
 * Marked as TaskPath enum class
 */
enum class TaskPath {
    TASK_DETAILS,
    COMMENTS
}
