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
                if (dataObj.selectedName.isNotEmpty())
                    binding.chip.text = dataObj.selectedName.take20orDefault()
                else
                    binding.chip.text = dataObj.category?.name
            }
            ChipComponentType.FACET_FIELDS.component -> {
                binding.chip.text = if (dataObj.selectedName.isNotEmpty()) dataObj.selectedName.take20orDefault() else context.getLocalizedName(dataObj.fieldsItem?.label ?: "")
            }
            ChipComponentType.FACET_INTERVALS.component -> {
                binding.chip.text = if (dataObj.selectedName.isNotEmpty()) dataObj.selectedName.take20orDefault() else context.getLocalizedName(dataObj.intervalsItem?.label ?: "")
            }
            else -> binding.chip.text = if (dataObj.selectedName.isNotEmpty()) dataObj.selectedName.take20orDefault() else dataObj.category?.name
        }

        binding.chip.isChecked = dataObj.isSelected
    }

    private fun String.take20orDefault(): String {
        if (this.length <= 20)
            return this

        if (this.contains(",")) {
            val splitStringArray = this.split(",")
            return context.getString(R.string.name_truncate_in_end, splitStringArray[0].take20orDefault(), splitStringArray.size.minus(1))
        }

        return context.getString(R.string.name_truncate_in, this.take(5), this.takeLast(5))
    }

    /**
     * set clickListener to the list item
     */
    @CallbackProp
    fun setClickListener(listener: OnClickListener?) {
        binding.chip.setOnClickListener(listener)
    }
}
