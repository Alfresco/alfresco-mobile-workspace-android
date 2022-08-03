package com.alfresco.content.browse.tasks

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.alfresco.content.data.TaskFilterData
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

    private val binding = ViewListSortChipsBinding.inflate(LayoutInflater.from(context), this)

    /**
     * Binding the TaskSortData type data to chip
     */
    @ModelProp
    fun setData(dataObj: TaskFilterData) {
        binding.chip.uncheck(false)
        binding.chip.text = dataObj.selectedName.ifEmpty { dataObj.name }
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
