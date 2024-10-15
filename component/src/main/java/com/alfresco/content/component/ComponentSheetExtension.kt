package com.alfresco.content.component

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.alfresco.content.common.updatePriorityView
import com.alfresco.content.component.ComponentViewModel.Companion.DUE_AFTER
import com.alfresco.content.component.ComponentViewModel.Companion.DUE_BEFORE
import com.alfresco.content.data.DefaultPriority
import com.alfresco.content.data.TaskPriority
import com.alfresco.content.data.getTaskPriority
import com.alfresco.content.getLocalizedName

/**
 * setup the SingleInputText Component
 * @param state
 */
fun ComponentSheet.setupSingleInputTextComponent(state: ComponentState) {
    binding.parentView.addView(binding.frameSingleInputText)
    binding.singleInputTextComponent.componentParent.visibility = View.VISIBLE
    binding.singleInputTextComponent.nameInput.isFocusableInTouchMode = true
    binding.singleInputTextComponent.nameInput.requestFocus()
    binding.singleInputTextComponent.nameInputLayout.hint = requireContext().getLocalizedName(state.parent?.properties?.placeholder ?: "")
    binding.singleInputTextComponent.nameInput.setText(state.parent?.selectedName)
    state.parent?.selectedName?.length?.let { length -> binding.singleInputTextComponent.nameInput.setSelection(length) }
    binding.singleInputTextComponent.nameInputLayout.editText?.addTextChangedListener(nameInputTextWatcher)
}

/**
 * setup the CheckList Component
 * @param viewModel
 */
fun ComponentSheet.setupCheckListComponent(viewModel: ComponentViewModel) {
    binding.parentView.addView(binding.frameCheckList)
    viewModel.buildCheckListModel()
    binding.checkListComponent.componentParent.visibility = View.VISIBLE
    binding.checkListComponent.recyclerView.setController(epoxyCheckListController)
}

/**
 * setup the RadioList Component
 * @param state
 * @param viewModel
 */
fun ComponentSheet.setupRadioListComponent(
    state: ComponentState,
    viewModel: ComponentViewModel,
) {
    binding.parentView.addView(binding.frameRadio)
    viewModel.buildSingleDataModel()
    if (state.parent?.selectedName?.isEmpty() == true) {
        viewModel.copyDefaultComponentData()
    }
    binding.radioListComponent.radioParent.visibility = View.VISIBLE
    binding.radioListComponent.recyclerView.setController(epoxyRadioListController)
}

/**
 * setup the NumberRange Component
 * @param state
 * @param viewModel
 */
fun ComponentSheet.setupNumberRangeComponent(
    state: ComponentState,
    viewModel: ComponentViewModel,
) {
    binding.parentView.addView(binding.frameNumberRange)
    binding.title.text = requireContext().getString(R.string.size_end_kb, binding.title.text.toString())
    binding.numberRangeComponent.componentParent.visibility = View.VISIBLE
    binding.numberRangeComponent.fromInput.isFocusableInTouchMode = true
    binding.numberRangeComponent.fromInput.requestFocus()
    if (state.parent?.selectedName?.isNotEmpty() == true) {
        val fromToArray = state.parent.selectedName.split("-")
        viewModel.fromValue = fromToArray[0].trim()
        viewModel.toValue = fromToArray[1].trim()
        binding.numberRangeComponent.fromInput.setText(fromToArray[0].trim())
        binding.numberRangeComponent.toInput.setText(fromToArray[1].trim())
        binding.numberRangeComponent.fromInput.setSelection(fromToArray[0].trim().length)
    }
    binding.numberRangeComponent.fromInputLayout.editText?.addTextChangedListener(numberFromInputTextWatcher)
    binding.numberRangeComponent.toInputLayout.editText?.addTextChangedListener(numberToInputTextWatcher)
}

/**
 * setup the Slider Component
 * @param state
 * @param viewModel
 */
fun ComponentSheet.setupSliderComponent(
    state: ComponentState,
    viewModel: ComponentViewModel,
) {
    binding.parentView.addView(binding.frameSlider)
    binding.sliderComponent.componentParent.visibility = View.VISIBLE

    viewModel.fromValue = "0"
    viewModel.buildSingleDataModel()

    binding.sliderComponent.slider.apply {
        state.parent?.properties?.min?.let { min ->
            valueFrom = min.toFloat()
        }
        state.parent?.properties?.max?.let { max ->
            valueTo = max.toFloat()
        }
        state.parent?.properties?.step?.let { step ->
            stepSize = step.toFloat()
        }
    }
    if (state.parent?.selectedName?.isNotEmpty() == true) {
        binding.sliderComponent.slider.value = state.parent.selectedName.toFloat()
    }

    binding.sliderComponent.slider.addOnChangeListener(sliderChangeListener)
}

/**
 * setup the DateRange Component
 * @param state
 * @param viewModel
 */
fun ComponentSheet.setupDateRangeComponent(
    state: ComponentState,
    viewModel: ComponentViewModel,
) {
    binding.parentView.addView(binding.frameDateRange)
    binding.dateRangeComponent.componentParent.visibility = View.VISIBLE

    binding.dateRangeComponent.fromInput.inputType = 0
    binding.dateRangeComponent.fromInput.isCursorVisible = false

    binding.dateRangeComponent.toInput.inputType = 0
    binding.dateRangeComponent.toInput.isCursorVisible = false

    state.parent?.properties?.dateFormat?.let { format ->
        viewModel.dateFormat = format.replace("D", "d").replace("Y", "y")
    }

    if (state.parent?.selector == ComponentType.DATE_RANGE.value) {
        if (state.parent.selectedName.isNotEmpty()) {
            val fromToArray = state.parent.selectedName.split("-")
            viewModel.fromDate = getString(R.string.date_format, fromToArray[0].trim(), fromToArray[1].trim(), fromToArray[2].trim())
            binding.dateRangeComponent.fromInput.setText(viewModel.fromDate)
            viewModel.toDate = getString(R.string.date_format, fromToArray[3].trim(), fromToArray[4].trim(), fromToArray[5].trim())
            binding.dateRangeComponent.toInput.setText(viewModel.toDate)
        }
    }

    if (state.parent?.selector == ComponentType.DATE_RANGE_FUTURE.value) {
        if (state.parent.selectedQueryMap.containsKey(DUE_BEFORE)) {
            val dueBeforeArray = state.parent.selectedQueryMap[DUE_BEFORE]!!.split("-")
            viewModel.toDate =
                getString(
                    R.string.date_format_new,
                    dueBeforeArray[0].trim(),
                    dueBeforeArray[1].trim(),
                    dueBeforeArray[2].trim(),
                )
            binding.dateRangeComponent.toInput.setText(viewModel.toDate)
        }

        if (state.parent.selectedQueryMap.containsKey(DUE_AFTER)) {
            val dueAfterArray = state.parent.selectedQueryMap[DUE_AFTER]!!.split("-")
            viewModel.fromDate =
                getString(
                    R.string.date_format_new,
                    dueAfterArray[0].trim(),
                    dueAfterArray[1].trim(),
                    dueAfterArray[2].trim(),
                )
            binding.dateRangeComponent.fromInput.setText(viewModel.fromDate)
        }
    }

    binding.dateRangeComponent.fromInput.setOnFocusChangeListener { view, hasFocus ->
        if (hasFocus) view.performClick()
    }
    binding.dateRangeComponent.toInput.setOnFocusChangeListener { view, hasFocus ->
        if (hasFocus) view.performClick()
    }

    binding.dateRangeComponent.fromInputLayout.editText?.addTextChangedListener(fromInputTextWatcher)
    binding.dateRangeComponent.toInputLayout.editText?.addTextChangedListener(toInputTextWatcher)
}

/**
 * setup the Facet Component
 * @param state
 * @param viewModel
 */
@SuppressLint("ClickableViewAccessibility")
fun ComponentSheet.setupFacetComponent(
    state: ComponentState,
    viewModel: ComponentViewModel,
) {
    viewModel.buildCheckListModel()
    state.parent?.options?.let {
        if (it.size > minVisibleItem) {
            binding.facetCheckListComponent.recyclerView.layoutParams = getRecyclerviewLayoutParams(this.requireContext(), minVisibleItem)
            binding.facetCheckListComponent.searchInputLayout.visibility = View.VISIBLE
        }
    }
    binding.parentView.addView(binding.frameFacet)
    binding.facetCheckListComponent.componentParent.visibility = View.VISIBLE

    binding.facetCheckListComponent.recyclerView.setController(epoxyCheckFacetListController)
    binding.facetCheckListComponent.searchInputLayout.editText?.addTextChangedListener(searchInputTextWatcher)
}

/**
 * setup the Process Actions Component
 * @param state
 * @param viewModel
 */
@SuppressLint("ClickableViewAccessibility")
fun ComponentSheet.setupProcessActionsComponent(
    state: ComponentState,
    viewModel: ComponentViewModel,
) {
    viewModel.buildCheckListModel()
    binding.parentView.addView(binding.frameActions)

    binding.processActions.componentParent.visibility = View.VISIBLE

    binding.processActions.recyclerView.setController(epoxyActionListController)
}

/**
 * setup the Title Description Component
 * @param state
 */
fun ComponentSheet.setupTextComponent(state: ComponentState) {
    binding.parentView.addView(binding.frameTitleDescription)
    if (state.parent?.query.isNullOrEmpty()) {
        binding.titleDescriptionComponent.tvTitle.visibility = View.GONE
    } else {
        binding.titleDescriptionComponent.tvTitle.text = state.parent?.query ?: ""
    }
    binding.titleDescriptionComponent.tvDescription.text = state.parent?.value ?: ""

    binding.bottomView.visibility = View.GONE
    binding.bottomSeparator.visibility = View.GONE
    binding.titleDescriptionComponent.componentParent.visibility = View.VISIBLE
}

/**
 * setup the Task Priority Component
 * @param state
 */
fun ComponentSheet.setupTaskPriorityComponent(state: ComponentState) {
    binding.parentView.addView(binding.frameTaskPriority)
    viewModel.priority = state.parent?.query?.toInt() ?: 0
    when (getTaskPriority(viewModel.priority)) {
        TaskPriority.LOW -> {
            binding.taskPriorityComponent.tvPriorityLow.updatePriorityView(viewModel.priority)
        }

        TaskPriority.MEDIUM -> {
            binding.taskPriorityComponent.tvPriorityMedium.updatePriorityView(viewModel.priority)
        }

        TaskPriority.HIGH -> {
            binding.taskPriorityComponent.tvPriorityHigh.updatePriorityView(viewModel.priority)
        }

        else -> {}
    }

    binding.taskPriorityComponent.tvPriorityLow.setOnClickListener {
        viewModel.priority = DefaultPriority.LOW.value
        binding.taskPriorityComponent.tvPriorityLow.updatePriorityView(viewModel.priority)
        binding.taskPriorityComponent.tvPriorityMedium.updatePriorityView(-1)
        binding.taskPriorityComponent.tvPriorityHigh.updatePriorityView(-1)
        cancelPriorityDialog()
    }
    binding.taskPriorityComponent.tvPriorityMedium.setOnClickListener {
        viewModel.priority = DefaultPriority.MEDIUM.value
        binding.taskPriorityComponent.tvPriorityLow.updatePriorityView(-1)
        binding.taskPriorityComponent.tvPriorityMedium.updatePriorityView(viewModel.priority)
        binding.taskPriorityComponent.tvPriorityHigh.updatePriorityView(-1)
        cancelPriorityDialog()
    }
    binding.taskPriorityComponent.tvPriorityHigh.setOnClickListener {
        viewModel.priority = DefaultPriority.HIGH.value
        binding.taskPriorityComponent.tvPriorityLow.updatePriorityView(-1)
        binding.taskPriorityComponent.tvPriorityMedium.updatePriorityView(-1)
        binding.taskPriorityComponent.tvPriorityHigh.updatePriorityView(viewModel.priority)
        cancelPriorityDialog()
    }

    binding.bottomView.visibility = View.GONE
    binding.bottomSeparator.visibility = View.GONE
}

private fun getRecyclerviewLayoutParams(
    context: Context,
    minVisibleItem: Int,
): LinearLayout.LayoutParams {
    val calculatedHeight =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            minVisibleItem * 48f,
            context.resources.displayMetrics,
        ).toInt()
    return LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, calculatedHeight)
}

private fun ComponentSheet.cancelPriorityDialog() {
    onApply?.invoke("", viewModel.priority.toString(), mapOf())
    dismiss()
}
