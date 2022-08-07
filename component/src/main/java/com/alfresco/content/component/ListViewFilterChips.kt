package com.alfresco.content.component

import android.content.Context
import android.text.InputFilter
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.alfresco.content.component.databinding.ViewListFilterChipsBinding
import com.alfresco.content.component.models.SearchChipCategory
import com.alfresco.content.data.SearchFilter
import com.alfresco.content.getLocalizedName

/**
 * Generated Model View for the Advance Filter Chips
 */
@ModelView(autoLayout = ModelView.Size.WRAP_WIDTH_WRAP_HEIGHT)
class ListViewFilterChips @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewListFilterChipsBinding.inflate(LayoutInflater.from(context), this)

    /**
     * Bind the capture item data to the view
     */
    @ModelProp
    fun setData(dataObj: SearchChipCategory) {
        binding.chip.uncheck(false)

        if (dataObj.category?.id == SearchFilter.Contextual.toString()) {
            binding.chip.isChecked = true
        }

        when (dataObj.category?.component?.selector) {
            ComponentType.TEXT.value -> {
                if (dataObj.selectedName.length > chipTextDisplayLimit) {
                    binding.chip.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(chipTextDisplayLimit.plus(3)))
                    binding.chip.ellipsize = TextUtils.TruncateAt.END
                }
                binding.chip.text = if (dataObj.selectedName.isNotEmpty()) dataObj.selectedName.wrapWithLimit(context, chipTextDisplayLimit)
                else context.getLocalizedName(dataObj.category?.name?.wrapWithLimit(context, chipTextDisplayLimit) ?: "")
            }
            ComponentType.FACETS.value -> {
                if (dataObj.selectedName.isNotEmpty()) {
                    binding.chip.text = dataObj.selectedName.wrapWithLimit(context, chipTextDisplayLimit, ",")
                } else {
                    val replacedString = dataObj.facets?.label?.replace(" ", ".") ?: ""
                    val localizedName = context.getLocalizedName(replacedString)
                    if (localizedName == replacedString)
                        binding.chip.text = dataObj.facets?.label?.wrapWithLimit(context, chipTextDisplayLimit) ?: ""
                    else
                        binding.chip.text = localizedName.wrapWithLimit(context, chipTextDisplayLimit)
                }
            }
            else -> {
                binding.chip.text = if (dataObj.selectedName.isNotEmpty()) dataObj.selectedName.wrapWithLimit(context, chipTextDisplayLimit, ",")
                else context.getLocalizedName(dataObj.category?.name?.wrapWithLimit(context, chipTextDisplayLimit) ?: "")
            }
        }

        binding.chip.isChecked = dataObj.isSelected
    }

    /**
     * set clickListener to the list item
     */
    @CallbackProp
    fun setClickListener(listener: OnClickListener?) {
        binding.chip.setOnClickListener(listener)
    }
}
