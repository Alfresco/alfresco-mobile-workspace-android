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

    private val chipTextDisplayLimit = 30
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
                binding.chip.text = if (dataObj.selectedName.isNotEmpty()) dataObj.selectedName.wrapWithLimit(chipTextDisplayLimit)
                else context.getLocalizedName(dataObj.category?.name?.wrapWithLimit(chipTextDisplayLimit) ?: "")
            }
            ComponentType.FACETS.value -> {
                if (dataObj.selectedName.isNotEmpty()) {
                    binding.chip.text = dataObj.selectedName.wrapWithLimit(chipTextDisplayLimit, ",")
                } else {
                    val replacedString = dataObj.facets?.label?.replace(" ", ".") ?: ""
                    val localizedName = context.getLocalizedName(replacedString)
                    if (localizedName == replacedString)
                        binding.chip.text = dataObj.facets?.label?.wrapWithLimit(chipTextDisplayLimit) ?: ""
                    else
                        binding.chip.text = localizedName.wrapWithLimit(chipTextDisplayLimit)
                }
            }
            else -> {
                binding.chip.text = if (dataObj.selectedName.isNotEmpty()) dataObj.selectedName.wrapWithLimit(chipTextDisplayLimit, ",")
                else context.getLocalizedName(dataObj.category?.name?.wrapWithLimit(chipTextDisplayLimit) ?: "")
            }
        }

        binding.chip.isChecked = dataObj.isSelected
    }

    private fun String.wrapWithLimit(limit: Int, delimiter: String? = null, multipleValue: Boolean = false): String {
        if (this.length <= limit && delimiter == null)
            return this

        if (delimiter != null) {
            if (this.contains(delimiter)) {
                val splitStringArray = this.split(delimiter)
                val chip1stString = splitStringArray[0]
                if (chip1stString.length > limit) {
                    return context.getString(R.string.name_truncate_in_end, chip1stString.wrapWithLimit(chipTextDisplayLimit, multipleValue = true), splitStringArray.size.minus(1))
                }
                return context.getString(R.string.name_truncate_in_end, chip1stString, splitStringArray.size.minus(1))
            } else {
                return this.wrapWithLimit(chipTextDisplayLimit)
            }
        }

        return if (multipleValue)
            context.getString(R.string.name_truncate_in, this.take(5), this.takeLast(5))
        else
            context.getString(R.string.name_truncate_end, this.take(chipTextDisplayLimit))
    }

    /**
     * set clickListener to the list item
     */
    @CallbackProp
    fun setClickListener(listener: OnClickListener?) {
        binding.chip.setOnClickListener(listener)
    }
}
