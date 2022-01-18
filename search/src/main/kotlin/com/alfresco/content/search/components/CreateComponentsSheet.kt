package com.alfresco.content.search.components

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.lifecycle.lifecycleScope
import com.airbnb.epoxy.AsyncEpoxyController
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.data.Buckets
import com.alfresco.content.getLocalizedName
import com.alfresco.content.search.ChipComponentType
import com.alfresco.content.search.R
import com.alfresco.content.search.databinding.SheetComponentCreateBinding
import com.alfresco.content.simpleController
import com.alfresco.ui.BottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.launch

/**
 * Component sheet for chip components
 */
class CreateComponentsSheet : BottomSheetDialogFragment(), MavericksView {
    private val viewModel: ComponentCreateViewModel by fragmentViewModel()
    private lateinit var binding: SheetComponentCreateBinding

    private val epoxyCheckListController: AsyncEpoxyController by lazy { epoxyCheckListController() }
    private val epoxyRadioListController: AsyncEpoxyController by lazy { epoxyRadioListController() }
    private val epoxyCheckFacetListController: AsyncEpoxyController by lazy { epoxyCheckFacetListController() }
    var onApply: ComponentApplyCallback? = null
    var onReset: ComponentResetCallback? = null
    var onCancel: ComponentCancelCallback? = null
    var executedPicker = false
    private val minVisibleItem = 10

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SheetComponentCreateBinding.inflate(inflater, container, false)
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

            val replacedString = state.parent?.category?.name?.replace(" ", ".") ?: ""
            val localizedName = requireContext().getLocalizedName(replacedString)
            if (localizedName == replacedString)
                binding.title.text = state.parent?.category?.name ?: ""
            else
                binding.title.text = localizedName

            when (state.parent?.category?.component?.selector) {
                ChipComponentType.TEXT.component -> setupTextComponent(state)
                ChipComponentType.CHECK_LIST.component -> setupCheckListComponent()
                ChipComponentType.RADIO.component -> setupRadioListComponent(state)
                ChipComponentType.NUMBER_RANGE.component -> setupNumberRangeComponent(state)
                ChipComponentType.SLIDER.component -> setupSliderComponent(state)
                ChipComponentType.DATE_RANGE.component -> setupDateRangeComponent(state)
                ChipComponentType.FACETS.component -> {
                    state.parent.facets?.buckets?.let {
                        if (it.size > minVisibleItem) {
                            binding.facetCheckListComponent.recyclerView.layoutParams = getRecyclerviewLayoutParams()
                            binding.facetCheckListComponent.searchInputLayout.visibility = View.VISIBLE
                        }
                    }
                    setupFacetComponent()
                }
            }
        }
    }

    private fun setListeners() {
        binding.applyButton.setOnClickListener {
            withState(viewModel) { state ->
                if (state.parent?.category?.component?.selector == ChipComponentType.DATE_RANGE.component) {
                    when {
                        viewModel.fromDate.isEmpty() -> {
                            binding.dateRangeComponent.fromInputLayout.error = getString(R.string.component_number_range_empty)
                        }
                        viewModel.toDate.isEmpty() -> {
                            binding.dateRangeComponent.toInputLayout.error = getString(R.string.component_number_range_empty)
                        }
                        else -> {
                            onApply?.invoke(state.parent.selectedName, state.parent.selectedQuery)
                            dismiss()
                        }
                    }
                } else {
                    onApply?.invoke(state.parent?.selectedName ?: "", state.parent?.selectedQuery ?: "")
                    dismiss()
                }
            }
        }
        binding.resetButton.setOnClickListener {
            onReset?.invoke("", "")
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

    private fun setupTextComponent(state: ComponentCreateState) {
        binding.parentView.addView(binding.frameText)
        binding.textComponent.componentParent.visibility = View.VISIBLE
        binding.textComponent.nameInput.isFocusableInTouchMode = true
        binding.textComponent.nameInput.requestFocus()
        binding.textComponent.nameInputLayout.hint = requireContext().getLocalizedName(state.parent?.category?.component?.settings?.placeholder ?: "")
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
                viewModel.updateSingleComponentData(s.toString())
            }
        })
    }

    private fun setupCheckListComponent() {
        binding.parentView.addView(binding.frameCheckList)
        viewModel.buildCheckListModel()
        binding.checkListComponent.componentParent.visibility = View.VISIBLE
        binding.checkListComponent.recyclerView.setController(epoxyCheckListController)
    }

    private fun setupRadioListComponent(state: ComponentCreateState) {
        binding.parentView.addView(binding.frameRadio)
        viewModel.buildSingleDataModel()
        if (state.parent?.selectedName?.isEmpty() == true)
            viewModel.copyDefaultComponentData()
        binding.radioListComponent.radioParent.visibility = View.VISIBLE
        binding.radioListComponent.recyclerView.setController(epoxyRadioListController)
    }

    private fun setupNumberRangeComponent(state: ComponentCreateState) {
        binding.parentView.addView(binding.frameNumberRange)
        viewModel.buildSingleDataModel()
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
        binding.numberRangeComponent.fromInputLayout.editText?.addTextChangedListener(object : TextWatcher {
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
        })

        binding.numberRangeComponent.toInputLayout.editText?.addTextChangedListener(object : TextWatcher {
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
        })
    }

    private fun setupSliderComponent(state: ComponentCreateState) {
        binding.parentView.addView(binding.frameSlider)
        viewModel.fromValue = "0"
        viewModel.buildSingleDataModel()
        binding.sliderComponent.componentParent.visibility = View.VISIBLE

        binding.sliderComponent.slider.apply {
            state.parent?.category?.component?.settings?.min?.let { min ->
                valueFrom = min.toFloat()
            }
            state.parent?.category?.component?.settings?.max?.let { max ->
                valueTo = max.toFloat()
            }
            state.parent?.category?.component?.settings?.step?.let { step ->
                stepSize = step.toFloat()
            }
        }
        if (state.parent?.selectedName?.isNotEmpty() == true) {
            binding.sliderComponent.slider.value = state.parent.selectedName.toFloat()
        }
        binding.sliderComponent.slider.addOnChangeListener { _, value, _ ->
            val sliderValue = value.toInt().toString()
            if (viewModel.isToValueValid(sliderValue)) {
                viewModel.toValue = sliderValue
            } else viewModel.toValue = ""
            viewModel.updateFormatNumberRange(true)
        }
    }

    private fun setupDateRangeComponent(state: ComponentCreateState) {

        binding.parentView.addView(binding.frameDateRange)
        binding.dateRangeComponent.componentParent.visibility = View.VISIBLE

        binding.dateRangeComponent.fromInput.inputType = 0
        binding.dateRangeComponent.fromInput.isCursorVisible = false

        binding.dateRangeComponent.toInput.inputType = 0
        binding.dateRangeComponent.toInput.isCursorVisible = false

        state.parent?.category?.component?.settings?.dateFormat?.let { format ->
            viewModel.dateFormat = format.replace("D", "d").replace("Y", "y")
        }

        if (state.parent?.selectedName?.isNotEmpty() == true) {
            val fromToArray = state.parent.selectedName.split("-")
            viewModel.fromDate = getString(R.string.date_format, fromToArray[0].trim(), fromToArray[1].trim(), fromToArray[2].trim())
            binding.dateRangeComponent.fromInput.setText(viewModel.fromDate)
            viewModel.toDate = getString(R.string.date_format, fromToArray[3].trim(), fromToArray[4].trim(), fromToArray[5].trim())
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

    @SuppressLint("ClickableViewAccessibility")
    private fun setupFacetComponent() {
        viewModel.buildCheckListModel()
        binding.parentView.addView(binding.frameFacet)
        binding.facetCheckListComponent.componentParent.visibility = View.VISIBLE
        binding.facetCheckListComponent.recyclerView.setController(epoxyCheckFacetListController)
        binding.facetCheckListComponent.searchInputLayout.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // no-op
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // no-op
            }

            override fun afterTextChanged(s: Editable?) {
                setSearchQuery(s.toString().trim())
            }
        })
    }

    private fun setSearchQuery(query: String) {
        viewModel.searchQuery = cleanupSearchQuery(query)
        viewModel.searchBucket(searchText = viewModel.searchQuery)
    }

    private fun cleanupSearchQuery(query: String): String {
        return query.replace("\\s+".toRegex(), " ").trim()
    }

    override fun invalidate() = withState(viewModel) { state ->
        when (state.parent?.category?.component?.selector) {
            ChipComponentType.CHECK_LIST.component -> {
                epoxyCheckListController.requestModelBuild()
            }
            ChipComponentType.RADIO.component -> {
                epoxyRadioListController.requestModelBuild()
            }
            ChipComponentType.FACETS.component -> {
                epoxyCheckFacetListController.requestModelBuild()
            }
        }
    }

    private fun epoxyCheckListController() = simpleController(viewModel) { state ->
        if (state.parent?.category?.component?.settings?.options?.isNotEmpty() == true)
            state.parent.category?.component?.settings?.options?.forEach { option ->
                listViewCheckRow {
                    id(option.hashCode())
                    data(option)
                    optionSelected(viewModel.isOptionSelected(state, option))
                    clickListener { model, _, _, _ ->
                        viewModel.updateMultipleComponentData(
                            model.data().name ?: "",
                            model.data().value ?: ""
                        )
                    }
                }
            }
    }

    private fun epoxyRadioListController() = simpleController(viewModel) { state ->
        if (state.parent?.category?.component?.settings?.options?.isNotEmpty() == true)
            state.parent.category?.component?.settings?.options?.forEach { option ->
                listViewRadioRow {
                    id(option.hashCode())
                    data(option)
                    optionSelected(viewModel.isOptionSelected(state, option))
                    clickListener { model, _, _, _ ->
                        viewModel.updateSingleComponentData(
                            requireContext().getLocalizedName(model.data().name ?: ""),
                            model.data().value ?: ""
                        )
                    }
                }
            }
    }

    private fun epoxyCheckFacetListController() = simpleController(viewModel) { state ->
        var listBucket: List<Buckets>? = null
        when (state.parent?.category?.component?.selector) {
            ChipComponentType.FACETS.component -> {
                listBucket = if (viewModel.searchQuery.isNotEmpty())
                    viewModel.searchBucketList
                else
                    state.parent.facets?.buckets
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
                            requireContext().getLocalizedName(model.data().label ?: ""),
                            model.data().filterQuery ?: ""
                        )
                    }
                }
            }
    }

    private fun showCalendar(isFrom: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = suspendCoroutine<String?> {
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

    private fun getRecyclerviewLayoutParams(): LinearLayout.LayoutParams {
        val calculatedHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minVisibleItem * 48f, resources.displayMetrics).toInt()
        return LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, calculatedHeight)
    }
}
