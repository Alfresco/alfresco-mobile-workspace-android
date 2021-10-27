package com.alfresco.content.search.components

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.search.ChipComponentType
import com.alfresco.content.search.R
import com.alfresco.content.search.databinding.SheetComponentCreateBinding
import com.alfresco.content.showSoftInput
import com.alfresco.ui.BottomSheetDialogFragment

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
                    viewModel.buildSingleDataModel()
                    binding.textComponent.nameInput.showSoftInput(requireContext())
                    binding.textComponent.componentParent.visibility = View.VISIBLE
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
                    binding.numberRangeComponent.minInput.showSoftInput(requireContext())
                    binding.numberRangeComponent.componentParent.visibility = View.VISIBLE
                    binding.title.text = getString(R.string.title_number_range)
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

        binding.numberRangeComponent.minInputLayout.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // no-op
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // no-op
            }

            override fun afterTextChanged(s: Editable?) {
                validateMinInput(s.toString())
                viewModel.minRange = s.toString()
            }
        })

        binding.numberRangeComponent.maxInputLayout.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // no-op
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // no-op
            }

            override fun afterTextChanged(s: Editable?) {
                validateMaxInput(s.toString())
                viewModel.maxRange = s.toString()
            }
        })
    }

    private fun validateMinInput(minValue: String) {
        val isEmpty = minValue.isEmpty()
        val valid = viewModel.isMinValueValid(minValue)
        binding.numberRangeComponent.minInputLayout.error = when {
            !valid -> resources.getString(R.string.component_number_range_invalid_input)
            isEmpty -> resources.getString(R.string.component_number_range_empty)
            else -> null
        }

        if (viewModel.minRange.isNotEmpty() && viewModel.maxRange.isNotEmpty())
            viewModel.updateFormatNumberRange()
    }

    private fun validateMaxInput(maxValue: String) {
        val isEmpty = maxValue.isEmpty()
        val valid = viewModel.isMaxValueValid(maxValue)
        binding.numberRangeComponent.maxInputLayout.error = when {
            !valid -> resources.getString(R.string.component_number_range_invalid_input)
            isEmpty -> resources.getString(R.string.component_number_range_empty)
            else -> null
        }
        if (viewModel.minRange.isNotEmpty() && viewModel.maxRange.isNotEmpty())
            viewModel.updateFormatNumberRange()
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
                                model.data().name ?: "",
                                model.data().value ?: ""
                            )
                        }
                    }
                }
        }
    }
}
