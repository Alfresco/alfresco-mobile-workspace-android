package com.alfresco.content.browse.processes.details

import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.airbnb.mvrx.Mavericks
import com.airbnb.mvrx.withState
import com.alfresco.content.DATE_FORMAT_1
import com.alfresco.content.DATE_FORMAT_4
import com.alfresco.content.DATE_FORMAT_5
import com.alfresco.content.actions.ActionUpdateNameDescription
import com.alfresco.content.browse.R
import com.alfresco.content.common.isEllipsized
import com.alfresco.content.common.updatePriorityView
import com.alfresco.content.component.ComponentData
import com.alfresco.content.component.ComponentMetaData
import com.alfresco.content.component.ComponentType
import com.alfresco.content.component.DatePickerBuilder
import com.alfresco.content.data.UserGroupDetails
import com.alfresco.content.formatDate
import com.alfresco.content.getFormattedDate
import com.alfresco.content.getLocalizedName
import com.alfresco.content.parseDate
import com.alfresco.content.setSafeOnClickListener
import kotlinx.coroutines.launch
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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
    viewModel.setListener(this)
    binding.iconTitleEdit.setSafeOnClickListener {
        withState(viewModel) { state ->
            viewModel.execute(ActionUpdateNameDescription(requireNotNull(state.parent)))
        }
    }
    binding.tvTitle.setSafeOnClickListener {
        if (binding.tvTitle.isEllipsized()) {
            showTitleDescriptionComponent()
        }
    }
    binding.tvDueDateValue.setSafeOnClickListener {
        formatDateAndShowCalendar()
    }
    binding.iconDueDateEdit.setSafeOnClickListener {
        formatDateAndShowCalendar()
    }
    binding.iconDueDateClear.setSafeOnClickListener {
        viewModel.updateDate(null)
    }
    binding.iconPriorityEdit.setSafeOnClickListener {
        withState(viewModel) { state ->
            val dataObj = state.parent
            viewLifecycleOwner.lifecycleScope.launch {
                val result = showComponentSheetDialog(
                    requireContext(),
                    ComponentData(
                        name = requireContext().getString(R.string.title_priority),
                        query = dataObj?.priority.toString(),
                        selector = ComponentType.TASK_PROCESS_PRIORITY.value,
                    ),
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
                    requireContext(),
                    state.parent,
                )
                if (result != null) {
                    viewModel.updateAssignee(result)
                }
            }
        }
    }
    binding.tvAttachmentViewAll.setSafeOnClickListener {
        findNavController().navigate(R.id.action_nav_process_details_to_nav_process_attached_files)
    }
    binding.clAddAttachment.setSafeOnClickListener {
        withState(viewModel) {
            showCreateSheet(it)
        }
    }
    binding.completeButton.setSafeOnClickListener {
        withState(viewModel) { state ->
            val entry = state.listContents.find { it.isUpload }
            if (state.parent?.startedBy == null) {
                showSnackar(
                    binding.root,
                    getString(R.string.error_select_assignee),
                )
            } else if (entry != null) {
                confirmContentQueuePrompt()
            } else {
                viewModel.startWorkflow()
            }
        }
    }
    binding.clTasks.setSafeOnClickListener {
        withState(viewModel) { state ->
            if (state.parent != null && state.listTask.isNotEmpty()) {
                val bundle = bundleOf(Mavericks.KEY_ARG to state.parent)
                findNavController().navigate(R.id.action_nav_process_details_to_nav_task_list, bundle)
            }
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
                dateFormat = DATE_FORMAT_4,
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

internal fun ProcessDetailFragment.setData(state: ProcessDetailViewState) {
    val dataEntry = state.parent
    binding.tvTitle.text = dataEntry?.name
    binding.tvDescription.text = dataEntry?.description?.ifEmpty { requireContext().getString(R.string.empty_description) }
    binding.tvAssignedValue.apply {
        text = if (dataEntry?.startedBy?.groupName?.isEmpty() == true && viewModel.getAPSUser().id == dataEntry.startedBy?.id) {
            requireContext().getLocalizedName(dataEntry.startedBy?.let { UserGroupDetails.with(it).name } ?: getString(R.string.text_select_assignee))
        } else if (dataEntry?.startedBy?.groupName?.isNotEmpty() == true) {
            requireContext().getLocalizedName(dataEntry.startedBy?.groupName ?: getString(R.string.text_select_assignee))
        } else requireContext().getLocalizedName(dataEntry?.startedBy?.name ?: getString(R.string.text_select_assignee))
    }

    if (dataEntry?.processDefinitionId.isNullOrEmpty()) {
        binding.tvAttachedTitle.text = getString(R.string.text_attached_files)
        binding.tvDueDateValue.text =
            if (dataEntry?.formattedDueDate.isNullOrEmpty()) requireContext().getString(R.string.empty_no_due_date) else dataEntry?.formattedDueDate?.getFormattedDate(DATE_FORMAT_1, DATE_FORMAT_4)
        binding.tvNoAttachedFilesError.text = getString(R.string.no_attached_files)
        binding.completeButton.text = getString(R.string.title_start_workflow)
        binding.tvPriorityValue.updatePriorityView(state.parent?.priority ?: -1)
    } else {
        enableViewUI()
        binding.clStatus.visibility = View.VISIBLE
        binding.clTasks.visibility = View.VISIBLE
        binding.tvDueDateTitle.text = getString(R.string.title_start_date)
        binding.tvAssignedTitle.text = getString(R.string.title_started_by)
        binding.tvDueDateValue.text = dataEntry?.started?.toLocalDate()?.toString()?.getFormattedDate(DATE_FORMAT_1, DATE_FORMAT_4)
        binding.tvStatusValue.text = if (dataEntry?.ended != null) getString(R.string.status_completed) else getString(R.string.status_active)
        if (state.listTask.isNotEmpty()) {
            binding.clTasks.visibility = View.VISIBLE
            binding.tvTasksValue.text = state.listTask.size.toString()
        } else {
            binding.tvTasksValue.text = ""
            binding.clTasks.visibility = View.GONE
        }
    }
}

private fun ProcessDetailFragment.enableViewUI() {
    binding.clAttachmentHeader.visibility = View.GONE
    binding.completeButton.visibility = View.GONE
    binding.clPriority.visibility = View.GONE
    binding.iconTitleEdit.visibility = View.GONE
    binding.iconDueDateEdit.visibility = View.INVISIBLE
    binding.iconDueDateClear.visibility = View.INVISIBLE
    binding.iconAssignedEdit.visibility = View.INVISIBLE
}

internal fun ProcessDetailFragment.updateUI(state: ProcessDetailViewState) {
    if (state.parent?.formattedDueDate.isNullOrEmpty()) {
        binding.iconDueDateClear.isVisible = false
        binding.iconDueDateEdit.isVisible = true
    } else {
        binding.iconDueDateEdit.isVisible = false
        binding.iconDueDateClear.isVisible = true
    }
}

private fun ProcessDetailFragment.formatDateAndShowCalendar() {
    withState(viewModel) { state ->
        val parseDate = state.parent?.formattedDueDate?.parseDate(DATE_FORMAT_1)
        showCalendar(parseDate?.formatDate(DATE_FORMAT_4, parseDate) ?: "")
    }
}

internal fun ProcessDetailFragment.showTitleDescriptionComponent() = withState(viewModel) {
    viewLifecycleOwner.lifecycleScope.launch {
        showComponentSheetDialog(
            requireContext(),
            ComponentData(
                name = requireContext().getString(R.string.title_start_workflow),
                query = it.parent?.name,
                value = it.parent?.description,
                selector = ComponentType.VIEW_TEXT.value,
            ),
        )
    }
}

internal fun executeContinuation(continuation: Continuation<ComponentMetaData?>, name: String, query: String) {
    continuation.resume(ComponentMetaData(name = name, query = query))
}
