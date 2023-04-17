package com.alfresco.content.browse.processes.details

import androidx.core.view.isVisible
import com.airbnb.mvrx.withState
import com.alfresco.content.browse.R
import com.alfresco.content.common.updatePriorityView

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

internal fun ProcessDetailFragment.setInitData() = withState(viewModel) { state ->
    val dataEntry = state.entry
    binding.tvTitle.text = dataEntry.name
    binding.tvDescription.text = dataEntry.description
    binding.tvAttachedTitle.text = getString(R.string.text_attached_files)
    binding.tvDueDateValue.text = requireContext().getString(R.string.empty_no_due_date)
    binding.tvNoAttachedFilesError.text = getString(R.string.no_attached_files)
    binding.completeButton.text = getString(R.string.title_start_workflow)
    binding.tvPriorityValue.updatePriorityView(3)
}
