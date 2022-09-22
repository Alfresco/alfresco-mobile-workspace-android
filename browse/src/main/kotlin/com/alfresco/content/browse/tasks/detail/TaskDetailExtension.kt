package com.alfresco.content.browse.tasks.detail

import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.withState
import com.alfresco.content.DATE_FORMAT_1
import com.alfresco.content.DATE_FORMAT_4
import com.alfresco.content.DATE_FORMAT_5
import com.alfresco.content.actions.ActionUpdateNameDescription
import com.alfresco.content.browse.R
import com.alfresco.content.component.ComponentData
import com.alfresco.content.component.ComponentType
import com.alfresco.content.component.DatePickerBuilder
import com.alfresco.content.formatDate
import com.alfresco.content.getFormattedDate
import com.alfresco.content.parseDate
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.launch

internal fun TaskDetailFragment.updateTaskDetailUI(isEdit: Boolean) = withState(viewModel) { state ->
    viewModel.hasTaskEditMode = isEdit
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

internal fun TaskDetailFragment.setListeners() {
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
    binding.iconTitleEdit.setOnClickListener {
        withState(viewModel) { state ->
            viewModel.execute(ActionUpdateNameDescription(requireNotNull(state.parent)))
        }
    }
    binding.tvDueDateValue.setOnClickListener {
        if (viewModel.hasTaskEditMode)
            formatDateAndShowCalendar()
    }
    binding.iconDueDateClear.setOnClickListener {
        viewModel.updateDate(null, true)
    }
    binding.iconDueDateEdit.setOnClickListener {
        formatDateAndShowCalendar()
    }
    binding.iconPriorityEdit.setOnClickListener {
        withState(viewModel) { state ->
            val dataObj = state.parent
            viewLifecycleOwner.lifecycleScope.launch {
                showComponentSheetDialog(
                    requireContext(), ComponentData(
                        name = requireContext().getString(R.string.title_priority),
                        query = dataObj?.name,
                        value = dataObj?.description,
                        selector = ComponentType.TASK_PRIORITY.value
                    )
                )
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
