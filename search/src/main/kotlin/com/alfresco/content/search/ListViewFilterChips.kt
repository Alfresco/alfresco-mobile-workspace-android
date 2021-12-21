package com.alfresco.content.search

import android.content.Context
import android.text.InputFilter
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.alfresco.content.data.SearchFilter
import com.alfresco.content.getLocalizedName
import com.alfresco.content.search.databinding.ViewListFilterChipsBinding

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
            ChipComponentType.TEXT.component -> {
                if (dataObj.selectedName.length > 20) {
                    binding.chip.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(23))
                    binding.chip.ellipsize = TextUtils.TruncateAt.END
                }
                binding.chip.text = if (dataObj.selectedName.isNotEmpty()) dataObj.selectedName.wrapWithLimit(20) else dataObj.category?.name
            }
            ChipComponentType.FACETS.component -> {
                binding.chip.text = if (dataObj.selectedName.isNotEmpty()) dataObj.selectedName.wrapWithLimit(20, ",") else context.getLocalizedName(dataObj.facets?.label ?: "")
            }
            else -> binding.chip.text = if (dataObj.selectedName.isNotEmpty()) dataObj.selectedName.wrapWithLimit(20, ",") else dataObj.category?.name
        }

        binding.chip.isChecked = dataObj.isSelected
    }

    private fun String.wrapWithLimit(limit: Int, delimiter: String? = null, multipleValue: Boolean = false): String {
        if (this.length <= limit)
            return this

        if (delimiter != null && this.contains(delimiter)) {
            val splitStringArray = this.split(delimiter)
            return context.getString(R.string.name_truncate_in_end, splitStringArray[0].wrapWithLimit(20, ",", true), splitStringArray.size.minus(1))
        }

        return if (multipleValue)
            context.getString(R.string.name_truncate_in, this.take(5), this.takeLast(5))
        else
            context.getString(R.string.name_truncate_end, this.take(20))
    }

    /**
     * set clickListener to the list item
     */
    @CallbackProp
    fun setClickListener(listener: OnClickListener?) {
        binding.chip.setOnClickListener(listener)
    }
}
