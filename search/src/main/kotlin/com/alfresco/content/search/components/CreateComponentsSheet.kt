package com.alfresco.content.search.components

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.getLocalizedName
import com.alfresco.content.search.ChipComponentType
import com.alfresco.content.search.R
import com.alfresco.content.search.databinding.SheetComponentCreateBinding
import com.alfresco.ui.BottomSheetDialogFragment
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.launch

/**
 * Component sheet for chip components
 */
class CreateComponentsSheet : BottomSheetDialogFragment(), MavericksView {
    private val viewModel: ComponentCreateViewModel by fragmentViewModel()
    private lateinit var binding: SheetComponentCreateBinding

    var onApply: ComponentApplyCallback? = null
    var onReset: ComponentResetCallback? = null
    var onCancel: ComponentCancelCallback? = null

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
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        dialog?.setOnCancelListener {
            onCancel?.invoke()
        }
        setupComponents()
        setListeners()
    }

    private fun setupComponents() {
        withState(viewModel) { state ->
            when (state.parent.category.component?.selector) {
                ChipComponentType.TEXT.component -> {
                    binding.textComponent.componentParent.visibility = View.VISIBLE
                    binding.textComponent.nameInput.isFocusableInTouchMode = true
                    binding.textComponent.nameInput.requestFocus()
                    binding.textComponent.nameInputLayout.hint = state.parent.category.component?.settings?.placeholder
                    binding.textComponent.nameInput.setText(state.parent.selectedName)
                    binding.title.text = getString(R.string.title_text_filter)
                }

                ChipComponentType.CHECK_LIST.component -> {
                    viewModel.buildCheckListModel()
                    binding.checkListComponent.componentParent.visibility = View.VISIBLE
                    binding.title.text = getString(R.string.title_file_type)
                }
                ChipComponentType.RADIO.component -> {
                    viewModel.buildSingleDataModel()
                    if (state.parent.selectedName.isEmpty())
                        viewModel.copyDefaultComponentData()
                    binding.radioListComponent.radioParent.visibility = View.VISIBLE
                    binding.title.text = getString(R.string.title_file_type)
                }
                ChipComponentType.NUMBER_RANGE.component -> {
                    viewModel.buildSingleDataModel()
                    binding.numberRangeComponent.componentParent.visibility = View.VISIBLE
                    binding.numberRangeComponent.fromInput.isFocusableInTouchMode = true
                    binding.numberRangeComponent.fromInput.requestFocus()
                    if (state.parent.selectedName.isNotEmpty()) {
                        val fromToArray = state.parent.selectedName.split("-")
                        binding.numberRangeComponent.fromInput.setText(fromToArray[0].trim())
                        binding.numberRangeComponent.toInput.setText(fromToArray[1].trim())
                    }
                    binding.title.text = getString(R.string.title_number_range)
                }
                ChipComponentType.SLIDER.component -> {
                    viewModel.fromValue = "0"
                    viewModel.buildSingleDataModel()
                    binding.sliderComponent.componentParent.visibility = View.VISIBLE

                    binding.sliderComponent.slider.apply {
                        state.parent.category.component?.settings?.min?.let { min ->
                            valueFrom = min.toFloat()
                        }
                        state.parent.category.component?.settings?.max?.let { max ->
                            valueTo = max.toFloat()
                        }
                        state.parent.category.component?.settings?.step?.let { step ->
                            stepSize = step.toFloat()
                        }
                        state.parent.category.component?.settings?.thumbLabel?.let { thumb ->
                        }
                    }
                    if (state.parent.selectedName.isNotEmpty()) {
                        binding.sliderComponent.slider.value = state.parent.selectedName.toFloat()
                    }
                    binding.title.text = getString(R.string.title_slider)
                }
            }
        }
    }

    private fun setListeners() {
        binding.applyButton.setOnClickListener {
            withState(viewModel) { state ->
                onApply?.invoke(state.parent.selectedName, state.parent.selectedQuery)
                dismiss()
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

        binding.numberRangeComponent.fromInputLayout.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // no-op
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // no-op
            }

            override fun afterTextChanged(s: Editable?) {
                val valid = viewModel.isFromValueValid(s.toString())
                binding.numberRangeComponent.numberRangeError.visibility = when {
                    !valid -> View.VISIBLE
                    else -> View.GONE
                }
                viewModel.fromValue = s.toString()
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
                val valid = viewModel.isToValueValid(s.toString())
                if (!valid)
                    binding.numberRangeComponent.numberRangeError.visibility = View.VISIBLE
                else
                    binding.numberRangeComponent.numberRangeError.visibility = View.GONE

                viewModel.toValue = s.toString()
                viewModel.updateFormatNumberRange(false)
            }
        })

        binding.sliderComponent.slider.addOnChangeListener { _, value, _ ->
            val sliderValue = value.toInt().toString()
            if (viewModel.isToValueValid(sliderValue)) {
                viewModel.toValue = sliderValue
            } else viewModel.toValue = ""
            viewModel.updateFormatNumberRange(true)
        }

        binding.dateRangeComponent.fromInput.setOnClickListener { showCalendar(true) }
        binding.dateRangeComponent.toInput.setOnClickListener { showCalendar(false) }
    }

    override fun invalidate() = withState(viewModel) { state ->
        binding.checkListComponent.recyclerView.withModels {
            state.parent.category
                .component?.settings?.options?.forEach { option ->
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

        binding.radioListComponent.recyclerView.withModels {
            state.parent.category
                .component?.settings?.options?.forEach { option ->
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
    }

    private fun showCalendar(isFrom: Boolean) {

        viewLifecycleOwner.lifecycleScope.launch {
            val result = suspendCoroutine<String?> {
                DatePickerBuilder(requireContext())
                    .onSuccess { date -> it.resume(date) }
                    .onFailure { it.resume(null) }
                    .show()
            }

            result?.let { date ->
                if (isFrom) {
                    viewModel.fromDate = date
                    binding.dateRangeComponent.fromInput.setText(date)
                } else {
                    viewModel.toValue = date
                    binding.dateRangeComponent.toInput.setText(date)
                }
            }
        }
    }
}
