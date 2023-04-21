package com.alfresco.content.browse.processes.details

import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.airbnb.mvrx.withState
import com.alfresco.content.DATE_FORMAT_1
import com.alfresco.content.DATE_FORMAT_4
import com.alfresco.content.DATE_FORMAT_5
import com.alfresco.content.actions.ActionUpdateNameDescription
import com.alfresco.content.browse.R
import com.alfresco.content.common.isEllipsized
import com.alfresco.content.component.ComponentData
import com.alfresco.content.component.ComponentType
import com.alfresco.content.component.DatePickerBuilder
import com.alfresco.content.formatDate
import com.alfresco.content.getFormattedDate
import com.alfresco.content.parseDate
import com.alfresco.content.setSafeOnClickListener
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.launch

internal fun ProcessDetailFragment.showStartFormView() {
    binding.clStatus.isVisible = false
    binding.clIdentifier.isVisible = false
    binding.clComment.isVisible = false
    binding.iconTitleEdit.isVisible = true
    binding.iconDueDateEdit.isVisible = true
    binding.iconPriorityEdit.isVisible = true
    binding.iconAssignedEdit.isVisible = true
    binding.clAttachmentHeader.isVisible = true
    binding.clAddAttachment.isVisible = true
    binding.tvNoAttachedFilesError.isVisible = true
    binding.completeButton.isVisible = true
}

internal fun ProcessDetailFragment.setListeners() {
    binding.iconTitleEdit.setSafeOnClickListener {
        withState(viewModel) { state ->
            viewModel.execute(ActionUpdateNameDescription(requireNotNull(state.parent)))
        }
    }
    binding.tvTitle.setSafeOnClickListener {
        if (binding.tvTitle.isEllipsized())
            showTitleDescriptionComponent()
    }
    binding.tvDueDateValue.setSafeOnClickListener {
        formatDateAndShowCalendar()
    }
    binding.iconDueDateEdit.setSafeOnClickListener {
        formatDateAndShowCalendar()
    }
    binding.iconDueDateClear.setSafeOnClickListener {
        viewModel.updateDate(null, true)
    }
    binding.iconPriorityEdit.setSafeOnClickListener {
        withState(viewModel) { state ->
            val dataObj = state.parent
            viewLifecycleOwner.lifecycleScope.launch {
                val result = showComponentSheetDialog(
                    requireContext(), ComponentData(
                        name = requireContext().getString(R.string.title_priority),
                        query = dataObj.priority.toString(),
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
                val result = showSearchUserGroupComponentDialog(
                    requireContext(), state.parent
                )
                if (result != null) {
                    viewModel.updateAssignee(result)
                }
            }
        }
    }
    binding.clAddAttachment.setSafeOnClickListener {
        withState(viewModel) {
            showCreateSheet(it)
        }
    }
}

private fun ProcessDetailFragment.showCalendar(fromDate: String) {
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

internal fun ProcessDetailFragment.updateUI(state: ProcessDetailViewState) {
    if (state.parent.formattedDueDate.isNullOrEmpty()) {
        binding.iconDueDateClear.isVisible = false
        binding.iconDueDateEdit.isVisible = true
    } else {
        binding.iconDueDateEdit.isVisible = false
        binding.iconDueDateClear.isVisible = true
    }
}

private fun ProcessDetailFragment.formatDateAndShowCalendar() {
    withState(viewModel) { state ->
        val parseDate = state.parent.formattedDueDate?.parseDate(DATE_FORMAT_1)
        showCalendar(parseDate?.formatDate(DATE_FORMAT_4, parseDate) ?: "")
    }
}

internal fun ProcessDetailFragment.showTitleDescriptionComponent() = withState(viewModel) {
    viewLifecycleOwner.lifecycleScope.launch {
        showComponentSheetDialog(
            requireContext(), ComponentData(
                name = requireContext().getString(R.string.title_start_workflow),
                query = it.parent.name,
                value = it.parent.description,
                selector = ComponentType.VIEW_TEXT.value
            )
        )
    }
}
