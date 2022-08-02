package com.alfresco.content.browse.tasks

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.DatePickerBuilder
import com.alfresco.content.browse.R
import com.alfresco.content.browse.databinding.SheetFilterCreateBinding
import com.alfresco.content.browse.tasks.FilterCreateViewModel.Companion.DUE_AFTER
import com.alfresco.content.browse.tasks.FilterCreateViewModel.Companion.DUE_BEFORE
import com.alfresco.content.getLocalizedName
import com.alfresco.content.simpleController
import com.alfresco.ui.BottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.launch

/**
 * Filter sheet for chip filters
 */
class CreateFilterSheet : BottomSheetDialogFragment(), MavericksView {
    private val viewModel: FilterCreateViewModel by fragmentViewModel()
    private lateinit var binding: SheetFilterCreateBinding

    private val epoxyRadioListController: AsyncEpoxyController by lazy { epoxyRadioListController() }
    var onApply: FilterApplyCallback? = null
    var onReset: FilterResetCallback? = null
    var onCancel: FilterCancelCallback? = null
    var executedPicker = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SheetFilterCreateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog?.setOnCancelListener {
            onCancel?.invoke()
        }
        setupComponents()
        setListeners()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view?.viewTreeObserver?.addOnGlobalLayoutListener {
            val bottomSheet =
                (dialog as BottomSheetDialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                BottomSheetBehavior.from<View>(it).apply {
                    val peekAmount = 1.0
                    peekHeight = ((it.parent as View).height * peekAmount).toInt()
                }
            }
        }
    }

    private fun setupComponents() {
        withState(viewModel) { state ->
            binding.parentView.removeAllViews()
            binding.parentView.addView(binding.topView)
            binding.parentView.addView(binding.separator)

            val localizedName = requireContext().getLocalizedName(state.parent?.name ?: "")
            binding.title.text = localizedName

            when (state.parent?.selector) {
                ChipFilterType.TEXT.component -> setupTextComponent(state)
                ChipFilterType.RADIO.component -> setupRadioListComponent(state)
                ChipFilterType.DATE_RANGE.component -> setupDateRangeComponent(state)
            }
        }
    }

    private fun setListeners() {
        binding.applyButton.setOnClickListener {
            withState(viewModel) { state ->
                if (state.parent?.selector == ChipFilterType.DATE_RANGE.component) {
                    when {
                        viewModel.fromDate.isEmpty() && viewModel.toDate.isEmpty() -> {
                            binding.dateRangeComponent.fromInputLayout.error = getString(R.string.component_number_range_empty)
                            binding.dateRangeComponent.toInputLayout.error = getString(R.string.component_number_range_empty)
                        }
                        else -> {
                            onApply?.invoke(state.parent.selectedName, state.parent.selectedQuery, state.parent.selectedQueryMap)
                            dismiss()
                        }
                    }
                } else {
                    onApply?.invoke(state.parent?.selectedName ?: "", state.parent?.selectedQuery ?: "", state.parent?.selectedQueryMap ?: mapOf())
                    dismiss()
                }
            }
        }
        binding.resetButton.setOnClickListener {
            onReset?.invoke("", "", mapOf())
            dismiss()
        }
        binding.cancelButton.setOnClickListener {
            onCancel?.invoke()
            dismiss()
        }
    }

    private fun setupTextComponent(state: FilterCreateState) {
        binding.parentView.addView(binding.frameText)
        binding.textComponent.componentParent.visibility = View.VISIBLE
        binding.textComponent.nameInput.isFocusableInTouchMode = true
        binding.textComponent.nameInput.requestFocus()
        binding.textComponent.nameInput.setText(state.parent?.selectedName)
        state.parent?.selectedName?.length?.let { length -> binding.textComponent.nameInput.setSelection(length) }
        binding.textComponent.nameInputLayout.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // no-op
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // no-op
            }

            override fun afterTextChanged(s: Editable?) {
                viewModel.updateSingleComponentData(viewModel.cleanupSearchQuery(s.toString()))
            }
        })
    }

    private fun setupRadioListComponent(state: FilterCreateState) {
        binding.parentView.addView(binding.frameRadio)
        viewModel.buildSingleDataModel()
        if (state.parent?.selectedName?.isEmpty() == true)
            viewModel.copyDefaultComponentData()
        binding.radioListComponent.radioParent.visibility = View.VISIBLE
        binding.radioListComponent.recyclerView.setController(epoxyRadioListController)
    }

    private fun setupDateRangeComponent(state: FilterCreateState) {

        binding.parentView.addView(binding.frameDateRange)
        binding.dateRangeComponent.componentParent.visibility = View.VISIBLE

        binding.dateRangeComponent.fromInput.inputType = 0
        binding.dateRangeComponent.fromInput.isCursorVisible = false

        binding.dateRangeComponent.toInput.inputType = 0
        binding.dateRangeComponent.toInput.isCursorVisible = false

        if (state.parent?.selectedQueryMap?.containsKey(DUE_BEFORE) == true) {
            val dueBeforeArray = state.parent.selectedQueryMap[DUE_BEFORE]!!.split("-")
            viewModel.fromDate = getString(R.string.date_format_new, dueBeforeArray[0].trim(), dueBeforeArray[1].trim(), dueBeforeArray[2].trim())
            binding.dateRangeComponent.fromInput.setText(viewModel.fromDate)
        }

        if (state.parent?.selectedQueryMap?.containsKey(DUE_AFTER) == true) {
            val dueAfterArray = state.parent.selectedQueryMap[DUE_AFTER]!!.split("-")
            viewModel.toDate = getString(R.string.date_format_new, dueAfterArray[0].trim(), dueAfterArray[1].trim(), dueAfterArray[2].trim())
            binding.dateRangeComponent.toInput.setText(viewModel.toDate)
        }

        binding.dateRangeComponent.fromInput.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) view.performClick()
        }
        binding.dateRangeComponent.toInput.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) view.performClick()
        }

        binding.dateRangeComponent.fromInput.setOnClickListener {
            if (!executedPicker) {
                binding.dateRangeComponent.componentParent.isEnabled = false
                executedPicker = true
                showCalendar(true)
            }
        }

        binding.dateRangeComponent.toInput.setOnClickListener {
            if (!executedPicker) {
                binding.dateRangeComponent.componentParent.isEnabled = false
                executedPicker = true
                showCalendar(false)
            }
        }

        binding.dateRangeComponent.fromInputLayout.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // no-op
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // no-op
            }

            override fun afterTextChanged(s: Editable?) {
                viewModel.fromDate = s.toString()
                if (s.toString().isNotEmpty()) {
                    binding.dateRangeComponent.fromInputLayout.error = null
                    binding.dateRangeComponent.fromInputLayout.isErrorEnabled = false
                }
            }
        })

        binding.dateRangeComponent.toInputLayout.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // no-op
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // no-op
            }

            override fun afterTextChanged(s: Editable?) {
                viewModel.toDate = s.toString()
                if (s.toString().isNotEmpty()) {
                    binding.dateRangeComponent.toInputLayout.error = null
                    binding.dateRangeComponent.toInputLayout.isErrorEnabled = false
                }
            }
        })
    }

    override fun invalidate() = withState(viewModel) { state ->
        when (state.parent?.selector) {
            ChipFilterType.RADIO.component -> {
                epoxyRadioListController.requestModelBuild()
            }
        }
    }

    private fun epoxyRadioListController() = simpleController(viewModel) { state ->
        if (state.parent?.options?.isNotEmpty() == true)
            state.parent.options?.forEach { option ->
                listViewTaskRadioRow {
                    id(option.hashCode())
                    data(option)
                    optionSelected(viewModel.isOptionSelected(state, option))
                    clickListener { model, _, _, _ ->
                        viewModel.updateSingleComponentData(
                            requireContext().getLocalizedName(model.data().label),
                            model.data().value
                        )
                    }
                }
            }
    }

    private fun showCalendar(isFrom: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = suspendCoroutine {
                DatePickerBuilder(
                    context = requireContext(),
                    fromDate = viewModel.fromDate,
                    toDate = viewModel.toDate,
                    isFrom = isFrom,
                    dateFormat = viewModel.dateFormat
                )
                    .onSuccess { date -> it.resume(date) }
                    .onFailure { it.resume(null) }
                    .show()
            }
            executedPicker = false
            binding.dateRangeComponent.componentParent.isEnabled = true
            result?.let { date ->
                if (isFrom) {
                    viewModel.fromDate = date
                    binding.dateRangeComponent.fromInput.setText(date)
                } else {
                    viewModel.toDate = date
                    binding.dateRangeComponent.toInput.setText(date)
                }
                viewModel.updateFormatDateRange()
            }
        }
    }
}
