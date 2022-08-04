package com.alfresco.content.browse.tasks

import android.content.Context
import android.text.InputFilter
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.alfresco.content.browse.R
import com.alfresco.content.data.TaskFilterData
import com.alfresco.content.getLocalizedName
import com.alfresco.content.listview.databinding.ViewListSortChipsBinding

/**
 * Generated Model View for the Task sort chips
 */
@ModelView(autoLayout = ModelView.Size.WRAP_WIDTH_WRAP_HEIGHT)
class ListViewSortChips @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val chipTextDisplayLimit = 30
    private val binding = ViewListSortChipsBinding.inflate(LayoutInflater.from(context), this)

    /**
     * Binding the TaskSortData type data to chip
     */
    @ModelProp
    fun setData(dataObj: TaskFilterData) {
        binding.chip.uncheck(false)
        binding.chip.text = dataObj.selectedName.ifEmpty { dataObj.name }

        when (dataObj.selector) {
            ChipFilterType.TEXT.component -> {
                if (dataObj.selectedName.length > chipTextDisplayLimit) {
                    binding.chip.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(chipTextDisplayLimit.plus(3)))
                    binding.chip.ellipsize = TextUtils.TruncateAt.END
                }
                binding.chip.text = if (dataObj.selectedName.isNotEmpty()) dataObj.selectedName.wrapWithLimit(chipTextDisplayLimit)
                else context.getLocalizedName(dataObj.name?.wrapWithLimit(chipTextDisplayLimit) ?: "")
            }
            else -> {
                binding.chip.text = if (dataObj.selectedName.isNotEmpty()) dataObj.selectedName.wrapWithLimit(chipTextDisplayLimit, ",")
                else context.getLocalizedName(dataObj.name?.wrapWithLimit(chipTextDisplayLimit) ?: "")
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
