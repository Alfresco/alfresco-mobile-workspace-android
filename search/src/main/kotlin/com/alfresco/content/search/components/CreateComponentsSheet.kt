package com.alfresco.content.search.components

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.getLocalizedName
import com.alfresco.content.models.Options
import com.alfresco.content.search.ChipComponentType
import com.alfresco.content.search.R
import com.alfresco.content.search.SearchChipCategory
import com.alfresco.content.search.databinding.SheetComponentCreateBinding
import com.alfresco.content.showSoftInput
import com.alfresco.ui.BottomSheetDialogFragment

internal data class ComponentCreateState(
    val parent: SearchChipCategory
) : MavericksState

internal class ComponentCreateViewModel(
    val context: Context,
    stateChipCreate: ComponentCreateState
) : MavericksViewModel<ComponentCreateState>(stateChipCreate) {

    private var listOptionsData: MutableList<ComponentMetaData> = mutableListOf()

    /**
     * build single value component data
     */
    fun buildSingleDataModel() = withState { state ->
        if (state.parent.selectedQuery.isNotEmpty()) {
            listOptionsData.add(ComponentMetaData(state.parent.selectedName, state.parent.selectedQuery))
        }
    }

    /**
     * update single selected component option (text)
     */
    fun updateSingleComponentData(name: String) =
        setState { copy(parent = getSearchChipCategory(parent, name, parent.category.component?.settings?.field ?: "")) }

    /**
     * update single selected component option(radio)
     */
    fun updateSingleComponentData(name: String, query: String) =
        setState { copy(parent = getSearchChipCategory(parent, context.getLocalizedName(name), query)) }

    /**
     * copy default component data
     */
    fun copyDefaultComponentData() {
        setState {
            val obj = parent.category.component?.settings?.options?.find { it.default ?: false }
            copy(parent = getSearchChipCategory(parent, context.getLocalizedName(obj?.name ?: ""), obj?.value ?: ""))
        }
    }

    private fun getSearchChipCategory(
        parent: SearchChipCategory,
        selectedName: String,
        selectedQuery: String
    ): SearchChipCategory {
        return SearchChipCategory(
            category = parent.category,
            isSelected = parent.isSelected,
            selectedName = selectedName,
            selectedQuery = selectedQuery
        )
    }

    /**
     * return true if the component is selected,otherwise false
     */
    fun isOptionSelected(state: ComponentCreateState, options: Options): Boolean {

        if (state.parent.selectedQuery.isEmpty())
            return options.default ?: false

        val selectedQuery = state.parent.selectedQuery
        if (selectedQuery.contains(",")) {
            selectedQuery.split(",").forEach { query ->
                if (query == options.value)
                    return true
            }
        } else {
            return selectedQuery == options.value
        }
        return false
    }

    companion object : MavericksViewModelFactory<ComponentCreateViewModel, ComponentCreateState> {
        override fun create(
            viewModelContext: ViewModelContext,
            state: ComponentCreateState
        ) = ComponentCreateViewModel(viewModelContext.activity(), state)
    }
}

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
                ChipComponentType.RADIO.component -> {
                    viewModel.buildSingleDataModel()
                    if (state.parent.selectedName.isEmpty())
                        viewModel.copyDefaultComponentData()
                    binding.radioListComponent.radioParent.visibility = View.VISIBLE
                    binding.title.text = getString(R.string.title_file_type)
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
    }

    override fun invalidate() = withState(viewModel) { state ->

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
