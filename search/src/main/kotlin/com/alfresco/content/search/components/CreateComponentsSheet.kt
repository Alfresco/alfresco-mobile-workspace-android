package com.alfresco.content.search.components

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.Mavericks
import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.alfresco.content.search.ChipComponentType
import com.alfresco.content.search.R
import com.alfresco.content.search.SearchChipCategory
import com.alfresco.content.search.databinding.SheetComponentCreateBinding
import com.alfresco.ui.BottomSheetDialogFragment

internal data class ComponentCreateState(val parent: SearchChipCategory) : MavericksState

internal class ComponentCreateViewModel(
    val context: Context,
    stateChipCreate: ComponentCreateState
) : MavericksViewModel<ComponentCreateState>(stateChipCreate) {

    companion object : MavericksViewModelFactory<ComponentCreateViewModel, ComponentCreateState> {
        override fun create(
            viewModelContext: ViewModelContext,
            state: ComponentCreateState
        ) = ComponentCreateViewModel(viewModelContext.activity(), state)
    }
}

internal typealias ComponentApplyCallback = (String, String) -> Unit
internal typealias ComponentResetCallback = (String, String) -> Unit
internal typealias ComponentCancelCallback = () -> Unit

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

        dialog?.setOnCancelListener {
            onCancel?.invoke()
        }

        withState(viewModel) { state ->
            if (state.parent.category.component?.selector == ChipComponentType.TEXT.component) {
                binding.textComponent.nameInputLayout.hint = state.parent.category.component?.settings?.placeholder
                binding.textComponent.nameInput.setText(state.parent.selectedName)
                binding.title.text = getString(R.string.title_text_filter)
            }
        }

        binding.applyButton.setOnClickListener {
            withState(viewModel) { state ->
                val query = state.parent.category.component?.settings?.field ?: ""
                onApply?.invoke(binding.textComponent.nameInput.text.toString(), query)
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
    }

    override fun invalidate() = withState(viewModel) { state ->
    }

    /**
     * Builder for build the component sheet
     */
    data class Builder(
        val context: Context,
        val searchChipCategory: SearchChipCategory,
        var onApply: ComponentApplyCallback? = null,
        var onReset: ComponentResetCallback? = null,
        var onCancel: ComponentCancelCallback? = null
    ) {

        /**
         * Component sheet apply callback
         */
        fun onApply(callback: ComponentApplyCallback?) =
            apply { this.onApply = callback }

        /**
         * Component sheet reset callback
         */
        fun onReset(callback: ComponentResetCallback?) =
            apply { this.onReset = callback }

        /**
         * Component sheet cancel callback
         */
        fun onCancel(callback: ComponentCancelCallback?) =
            apply { this.onCancel = callback }

        /**
         * Component sheet show method
         */
        fun show() {
            val fragmentManager = when (context) {
                is AppCompatActivity -> context.supportFragmentManager
                is Fragment -> context.childFragmentManager
                else -> throw IllegalArgumentException()
            }
            CreateComponentsSheet().apply {
                arguments = bundleOf(Mavericks.KEY_ARG to searchChipCategory)
                onApply = this@Builder.onApply
                onReset = this@Builder.onReset
                onCancel = this@Builder.onCancel
            }.show(fragmentManager, CreateComponentsSheet::class.java.simpleName)
        }
    }
}
