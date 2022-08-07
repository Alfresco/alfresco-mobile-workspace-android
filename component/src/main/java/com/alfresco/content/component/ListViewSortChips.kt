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
import com.alfresco.content.component.databinding.ViewListSortChipsBinding
import com.alfresco.content.data.TaskFilterData
import com.alfresco.content.getLocalizedName

/**
 * Generated Model View for the Task sort chips
 */
@ModelView(autoLayout = ModelView.Size.WRAP_WIDTH_WRAP_HEIGHT)
class ListViewSortChips @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewListSortChipsBinding.inflate(LayoutInflater.from(context), this)

    /**
     * Binding the TaskSortData type data to chip
     */
    @ModelProp
    fun setData(dataObj: TaskFilterData) {
        binding.chip.uncheck(false)
        binding.chip.text = dataObj.selectedName.ifEmpty { dataObj.name }

        when (dataObj.selector) {
            ComponentType.TEXT.value -> {
                if (dataObj.selectedName.length > chipTextDisplayLimit) {
                    binding.chip.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(chipTextDisplayLimit.plus(3)))
                    binding.chip.ellipsize = TextUtils.TruncateAt.END
                }
                binding.chip.text = if (dataObj.selectedName.isNotEmpty()) dataObj.selectedName.wrapWithLimit(context, chipTextDisplayLimit)
                else context.getLocalizedName(dataObj.name?.wrapWithLimit(context, chipTextDisplayLimit) ?: "")
            }
            else -> {
                binding.chip.text = if (dataObj.selectedName.isNotEmpty()) dataObj.selectedName.wrapWithLimit(context, chipTextDisplayLimit, ",")
                else context.getLocalizedName(dataObj.name?.wrapWithLimit(context, chipTextDisplayLimit) ?: "")
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
