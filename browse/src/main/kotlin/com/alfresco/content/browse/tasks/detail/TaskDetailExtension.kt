package com.alfresco.content.browse.tasks.detail

import android.util.TypedValue
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.withState
import com.alfresco.content.DATE_FORMAT_1
import com.alfresco.content.DATE_FORMAT_4
import com.alfresco.content.DATE_FORMAT_5
import com.alfresco.content.actions.ActionUpdateNameDescription
import com.alfresco.content.browse.R
import com.alfresco.content.common.addTextViewPrefix
import com.alfresco.content.common.isEllipsized
import com.alfresco.content.common.updatePriorityView
import com.alfresco.content.component.ComponentData
import com.alfresco.content.component.ComponentType
import com.alfresco.content.component.DatePickerBuilder
import com.alfresco.content.data.TaskEntry
import com.alfresco.content.formatDate
import com.alfresco.content.getFormattedDate
import com.alfresco.content.getLocalizedName
import com.alfresco.content.parseDate
import com.alfresco.content.setSafeOnClickListener
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal fun TaskDetailFragment.updateTaskDetailUI(isEdit: Boolean) = withState(viewModel) { state ->
    viewModel.hasTaskEditMode = isEdit
    menuDetail.findItem(R.id.action_edit).isVisible = !isEdit
    menuDetail.findItem(R.id.action_done).isVisible = isEdit
    if (isEdit) {
        binding.iconTitleEdit.visibility = View.VISIBLE
        if (state.parent?.localDueDate != null) {
            binding.iconDueDateEdit.visibility = View.GONE
            binding.iconDueDateClear.visibility = View.VISIBLE
        } else {
            binding.iconDueDateClear.visibility = View.GONE
            binding.iconDueDateEdit.visibility = View.VISIBLE
        }
        binding.iconPriorityEdit.visibility = View.VISIBLE
        binding.iconAssignedEdit.visibility = View.VISIBLE
        binding.completeButton.isEnabled = false
    } else {
        binding.iconTitleEdit.visibility = View.GONE
        binding.iconDueDateEdit.visibility = View.INVISIBLE
        binding.iconDueDateClear.visibility = View.INVISIBLE
        binding.iconPriorityEdit.visibility = View.INVISIBLE
        binding.iconAssignedEdit.visibility = View.INVISIBLE
        binding.completeButton.isEnabled = true
    }
}

internal fun TaskDetailFragment.enableTaskFormUI() = withState(viewModel) { state ->
    binding.clComment.visibility = View.GONE
    binding.clIdentifier.visibility = View.GONE
    binding.iconStatusNav.visibility = View.VISIBLE
    binding.iconStatus.setImageResource(R.drawable.ic_task_status_star)

    binding.clStatus.setSafeOnClickListener {
        findNavController().navigate(R.id.action_nav_task_detail_to_nav_task_status)
    }
}

internal fun TaskDetailFragment.setTaskDetailAfterResponse(dataObj: TaskEntry) = withState(viewModel) { state ->
    if (state.requestTaskForm.complete || state.request.complete) {
        if (dataObj.localDueDate != null) {
            binding.tvDueDateValue.text = dataObj.localDueDate?.getFormattedDate(DATE_FORMAT_1, DATE_FORMAT_4)
        } else {
            binding.tvDueDateValue.text = requireContext().getString(R.string.empty_no_due_date)
        }

        binding.tvPriorityValue.updatePriorityView(dataObj.priority)
        binding.tvDescription.text = if (dataObj.description.isNullOrEmpty()) requireContext().getString(R.string.empty_description) else dataObj.description
        binding.tvDescription.addTextViewPrefix(requireContext().getString(R.string.suffix_view_all)) {
            showTitleDescriptionComponent()
        }

        if (viewModel.isTaskCompleted(state)) {
            binding.tvAddComment.visibility = View.GONE
            binding.iconAddCommentUser.visibility = View.GONE
            binding.clCompleted.visibility = View.VISIBLE
            if (state.listComments.isEmpty()) binding.viewComment2.visibility = View.GONE else View.VISIBLE
            binding.tvCompletedValue.text = dataObj.endDate?.toLocalDate().toString().getFormattedDate(DATE_FORMAT_1, DATE_FORMAT_4)

            (binding.clDueDate.layoutParams as ConstraintLayout.LayoutParams).apply {
                topMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, resources.displayMetrics).toInt()
            }
            binding.clStatus.visibility = if (viewModel.isWorkflowTask && viewModel.hasTaskStatusEnabled(state)) {
                binding.tvStatusValue.text = dataObj.taskFormStatus
                View.VISIBLE
            } else View.GONE
        } else {
            makeOutcomes()
            binding.clCompleted.visibility = View.GONE
            (binding.clDueDate.layoutParams as ConstraintLayout.LayoutParams).apply {
                topMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0f, resources.displayMetrics).toInt()
            }
            binding.clStatus.visibility = if (viewModel.isWorkflowTask && !viewModel.hasTaskStatusEnabled(state)) {
                View.GONE
            } else {
                View.VISIBLE
            }

            binding.tvStatusValue.text = if (!viewModel.isWorkflowTask) {
                getString(R.string.status_active)
            } else dataObj.taskFormStatus
        }
    }
}

internal fun TaskDetailFragment.setListeners() {
    viewModel.setListener(this)
    binding.tvAddComment.setSafeOnClickListener {
        viewModel.isAddComment = true
        navigateToCommentScreen()
    }
    binding.tvCommentViewAll.setSafeOnClickListener {
        navigateToCommentScreen()
    }
    commentViewBinding.clRecentComment.setSafeOnClickListener {
        navigateToCommentScreen()
    }
    binding.tvAttachmentViewAll.setSafeOnClickListener {
        findNavController().navigate(R.id.action_nav_task_detail_to_nav_attached_files)
    }
    binding.completeButton.setSafeOnClickListener {
        withState(viewModel) { state ->
            taskCompletePrompt(viewModel.isFilesInQueue(state))
        }
    }
    binding.iconTitleEdit.setSafeOnClickListener {
        withState(viewModel) { state ->
            viewModel.execute(ActionUpdateNameDescription(requireNotNull(state.parent)))
        }
    }
    binding.tvDueDateValue.setSafeOnClickListener {
        if (viewModel.hasTaskEditMode)
            formatDateAndShowCalendar()
    }
    binding.iconDueDateClear.setSafeOnClickListener {
        viewModel.updateDate(null, true)
    }
    binding.iconDueDateEdit.setSafeOnClickListener {
        formatDateAndShowCalendar()
    }
    binding.tvTitle.setSafeOnClickListener {
        if (binding.tvTitle.isEllipsized())
            showTitleDescriptionComponent()
    }
    binding.clAddAttachment.setSafeOnClickListener {
        withState(viewModel) {
            showCreateSheet(it)
        }
    }
    binding.iconPriorityEdit.setSafeOnClickListener {
        withState(viewModel) { state ->
            val dataObj = state.parent
            viewLifecycleOwner.lifecycleScope.launch {
                val result = showComponentSheetDialog(
                    requireContext(), ComponentData(
                        name = requireContext().getString(R.string.title_priority),
                        query = dataObj?.priority.toString(),
                        selector = ComponentType.TASK_PROCESS_PRIORITY.value
                    )
                )

                if (result != null) {
                    viewModel.updatePriority(result)
                }
            }
        }
    }
    binding.iconAssignedEdit.setSafeOnClickListener {
        withState(viewModel) { state ->
            requireNotNull(state.parent)
            viewLifecycleOwner.lifecycleScope.launch {
                val result = showSearchUserComponentDialog(
                    requireContext(), state.parent
                )
                if (result != null) {
                    viewModel.updateAssignee(result)
                }
            }
        }
    }
}

private fun TaskDetailFragment.formatDateAndShowCalendar() {
    withState(viewModel) { state ->
        val parseDate = state.parent?.localDueDate?.parseDate(DATE_FORMAT_1)
        showCalendar(parseDate?.formatDate(DATE_FORMAT_4, parseDate) ?: "")
    }
}

private fun TaskDetailFragment.navigateToCommentScreen() {
    findNavController().navigate(R.id.action_nav_task_detail_to_nav_comments)
}

private fun TaskDetailFragment.showCalendar(fromDate: String) {
    viewLifecycleOwner.lifecycleScope.launch {
        val result = suspendCoroutine {
            DatePickerBuilder(
                context = requireContext(),
                fromDate = fromDate,
                isFrom = true,
                isFutureDate = true,
                dateFormat = DATE_FORMAT_4
            )
                .onSuccess { date -> it.resume(date) }
                .onFailure { it.resume(null) }
                .show()
        }

        result?.let { date ->
            viewModel.updateDate(date.getFormattedDate(DATE_FORMAT_4, DATE_FORMAT_5))
        }
    }
}

internal fun TaskDetailFragment.showTitleDescriptionComponent() = withState(viewModel) {
    viewLifecycleOwner.lifecycleScope.launch {
        showComponentSheetDialog(
            requireContext(), ComponentData(
                name = requireContext().getString(R.string.task_title),
                query = it.parent?.name,
                value = it.parent?.description,
                selector = ComponentType.VIEW_TEXT.value
            )
        )
    }
}

internal fun TaskDetailFragment.makeOutcomes() = withState(viewModel) { state ->
    if (binding.parentOutcomes.childCount == 0)
        state.parent?.outcomes?.forEach { dataObj ->
            val button = this.layoutInflater.inflate(R.layout.view_layout_outcomes, binding.parentOutcomes, false) as MaterialButton
            button.text = requireContext().getLocalizedName(dataObj.name)
            button.setOnClickListener {
                withState(viewModel) { newState ->
                    if (viewModel.hasTaskStatusEnabled(newState) && (newState.parent?.taskFormStatus ==
                                newState.parent?.statusOption?.find { option -> option.id == "empty" }?.name)
                    )
                        showSnackar(
                            binding.root,
                            getString(R.string.error_select_status)
                        )
                    else viewModel.actionOutcome(dataObj.outcome)
                }
            }
            binding.parentOutcomes.addView(button)
        }
}
