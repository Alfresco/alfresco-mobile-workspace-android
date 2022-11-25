package com.alfresco.content.component

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
import com.alfresco.content.component.databinding.SheetComponentFilterBinding
import com.alfresco.content.getLocalizedName
import com.alfresco.content.simpleController
import com.alfresco.ui.BottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.slider.Slider
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.launch

/**
 * Marked as ComponentSheet class
 */
class ComponentSheet : BottomSheetDialogFragment(), MavericksView {

    internal val viewModel: ComponentViewModel by fragmentViewModel()
    lateinit var binding: SheetComponentFilterBinding

    var onApply: ComponentApplyCallback? = null
    var onReset: ComponentResetCallback? = null
    var onCancel: ComponentCancelCallback? = null

    val epoxyCheckListController: AsyncEpoxyController by lazy { epoxyCheckListController() }
    val epoxyRadioListController: AsyncEpoxyController by lazy { epoxyRadioListController() }
    val epoxyCheckFacetListController: AsyncEpoxyController by lazy { epoxyCheckFacetListController() }

    private var executedPicker = false
    val minVisibleItem = 10
    private val textFileSize = "search.facet_fields.size"

    val nameInputTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // no-op
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // no-op
        }

        override fun afterTextChanged(s: Editable?) {
            viewModel.updateSingleComponentData(s.toString())
        }
    }

    val fromInputTextWatcher = object : TextWatcher {
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
    }

    val toInputTextWatcher = object : TextWatcher {
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
    }

    val searchInputTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // no-op
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // no-op
        }

        override fun afterTextChanged(s: Editable?) {
            viewModel.searchQuery = s.toString().trim().replace("\\s+".toRegex(), " ").trim()
            viewModel.searchBucket(searchText = viewModel.searchQuery)
        }
    }

    val numberFromInputTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // no-op
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // no-op
        }

        override fun afterTextChanged(s: Editable?) {
            val min = s.toString().trim()
            val valid = viewModel.isFromValueValid(min)
            binding.numberRangeComponent.numberRangeError.visibility = when {
                !valid -> View.VISIBLE
                else -> View.GONE
            }
            viewModel.fromValue = min
            viewModel.updateFormatNumberRange(false)
        }
    }

    val numberToInputTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // no-op
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // no-op
        }

        override fun afterTextChanged(s: Editable?) {
            val max = s.toString().trim()
            val valid = viewModel.isToValueValid(max)
            binding.numberRangeComponent.numberRangeError.visibility = when {
                !valid -> View.VISIBLE
                else -> View.GONE
            }
            viewModel.toValue = max
            viewModel.updateFormatNumberRange(false)
        }
    }

    val sliderChangeListener = Slider.OnChangeListener { _, value, _ ->
        val sliderValue = value.toInt().toString()
        if (viewModel.isToValueValid(sliderValue)) {
            viewModel.toValue = sliderValue
        } else viewModel.toValue = ""
        viewModel.updateFormatNumberRange(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SheetComponentFilterBinding.inflate(inflater, container, false)
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
        dialog?.window?.decorView?.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
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

    private fun setupComponents() = withState(viewModel) { state ->
        binding.parentView.removeAllViews()
        binding.parentView.addView(binding.topView)
        binding.parentView.addView(binding.separator)

        val replacedString = state.parent?.name?.replace(" ", ".") ?: ""
        val localizedName = requireContext().getLocalizedName(replacedString)
        if (localizedName == replacedString)
            binding.title.text = state.parent?.name ?: ""
        else if (state.parent?.name?.lowercase().equals(textFileSize))
            binding.title.text = requireContext().getString(R.string.size_end_kb, localizedName)
        else
            binding.title.text = localizedName

        when (val selector = state.parent?.selector) {
            ComponentType.TEXT.value -> setupSingleInputTextComponent(state)
            ComponentType.TASK_PRIORITY.value -> setupTaskPriorityComponent(state)
            ComponentType.VIEW_TEXT.value -> setupTextComponent(state)
            ComponentType.CHECK_LIST.value -> setupCheckListComponent(viewModel)
            ComponentType.RADIO.value -> setupRadioListComponent(state, viewModel)
            ComponentType.NUMBER_RANGE.value -> setupNumberRangeComponent(state, viewModel)
            ComponentType.SLIDER.value -> setupSliderComponent(state, viewModel)
            ComponentType.DATE_RANGE.value, ComponentType.DATE_RANGE_FUTURE.value -> {
                setupDateRangeComponent(state, viewModel)

                binding.dateRangeComponent.fromInput.setOnClickListener {
                    if (!executedPicker) {
                        binding.dateRangeComponent.componentParent.isEnabled = false
                        executedPicker = true
                        showCalendar(true, (selector == ComponentType.DATE_RANGE_FUTURE.value))
                    }
                }

                binding.dateRangeComponent.toInput.setOnClickListener {
                    if (!executedPicker) {
                        binding.dateRangeComponent.componentParent.isEnabled = false
                        executedPicker = true
                        showCalendar(false, (selector == ComponentType.DATE_RANGE_FUTURE.value))
                    }
                }
            }
            ComponentType.FACETS.value -> setupFacetComponent(state, viewModel)
        }
    }

    private fun setListeners() {
        binding.applyButton.setOnClickListener {
            withState(viewModel) { state ->
                when (state.parent?.selector) {
                    ComponentType.DATE_RANGE.value -> {
                        if (viewModel.fromDate.isEmpty()) binding.dateRangeComponent.fromInputLayout.error = getString(R.string.component_number_range_empty)
                        else if (viewModel.toDate.isEmpty()) binding.dateRangeComponent.toInputLayout.error = getString(R.string.component_number_range_empty)
                        else {
                            onApply?.invoke(state.parent.selectedName, state.parent.selectedQuery, state.parent.selectedQueryMap)
                            dismiss()
                        }
                    }
                    ComponentType.DATE_RANGE_FUTURE.value -> {
                        if (viewModel.fromDate.isEmpty() && viewModel.toDate.isEmpty()) binding.dateRangeComponent.fromInputLayout.error = getString(R.string.component_number_range_empty)
                        else {
                            onApply?.invoke(state.parent.selectedName, state.parent.selectedQuery, state.parent.selectedQueryMap)
                            dismiss()
                        }
                    }
                    else -> {
                        onApply?.invoke(state.parent?.selectedName ?: "", state.parent?.selectedQuery ?: "", state.parent?.selectedQueryMap ?: mapOf())
                        dismiss()
                    }
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

        viewModel.onSearchComplete = {
            epoxyCheckFacetListController.requestModelBuild()
        }
    }

    override fun invalidate() = withState(viewModel) { state ->
        when (state.parent?.selector) {
            ComponentType.CHECK_LIST.value -> {
                epoxyCheckListController.requestModelBuild()
            }
            ComponentType.RADIO.value -> {
                epoxyRadioListController.requestModelBuild()
            }
            ComponentType.FACETS.value -> {
                epoxyCheckFacetListController.requestModelBuild()
            }
        }
    }

    private fun epoxyCheckListController() = simpleController(viewModel) { state ->
        if (state.parent?.options?.isNotEmpty() == true)
            state.parent.options.forEach { option ->
                listViewCheckRow {
                    id(option.hashCode())
                    data(option)
                    optionSelected(viewModel.isOptionSelected(state, option))
                    clickListener { model, _, _, _ ->
                        viewModel.updateMultipleComponentData(model.data().label, model.data().value)
                    }
                }
            }
    }

    private fun epoxyRadioListController() = simpleController(viewModel) { state ->
        if (state.parent?.options?.isNotEmpty() == true)
            state.parent.options.forEach { option ->
                listViewRadioRow {
                    id(option.hashCode())
                    data(option)
                    optionSelected(viewModel.isOptionSelected(state, option))
                    clickListener { model, _, _, _ ->
                        viewModel.updateSingleComponentData(
                            requireContext().getLocalizedName(model.data().label),
                            model.data().query
                        )
                    }
                }
            }
    }

    private fun epoxyCheckFacetListController() = simpleController(viewModel) { state ->
        var listBucket: List<ComponentOptions>? = null
        when (state.parent?.selector) {
            ComponentType.FACETS.value -> {
                listBucket = if (viewModel.searchQuery.isNotEmpty())
                    viewModel.searchComponentList
                else
                    state.parent.options
            }
        }
        if (listBucket?.isNotEmpty() == true)
            listBucket.forEach { bucket ->
                listViewFacetCheckRow {
                    id(bucket.hashCode())
                    data(bucket)
                    optionSelected(viewModel.isOptionSelected(state, bucket))
                    clickListener { model, _, _, _ ->
                        viewModel.updateMultipleComponentData(
                            requireContext().getLocalizedName(model.data().label),
                            model.data().query
                        )
                    }
                }
            }
    }

    private fun showCalendar(isFrom: Boolean, isFutureDate: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = suspendCoroutine {
                DatePickerBuilder(
                    context = requireContext(),
                    fromDate = viewModel.fromDate,
                    toDate = viewModel.toDate,
                    isFrom = isFrom,
                    isFutureDate = isFutureDate,
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
