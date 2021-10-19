package com.alfresco.content.search

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.CompoundButton
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.alfresco.content.data.SearchFilter
import com.alfresco.content.search.databinding.ViewListFilterChipsBinding
import java.lang.StringBuilder

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
        println("ListViewFilterChips.setData  ${dataObj.category.name}")
        binding.chip.uncheck(false)

        binding.chip.text = getChipName(dataObj)

        if (dataObj.category.id == SearchFilter.Contextual.toString()) {
            binding.chip.isChecked = true
        }

        binding.chip.isChecked = dataObj.isSelected
    }

    private fun getChipName(dataObj: SearchChipCategory): StringBuilder {

        val nameBuilder = StringBuilder()

        val chipTitle: String? = if (dataObj.selectedName.isNotEmpty())
            "${dataObj.category.name}: "
        else dataObj.category.name

        nameBuilder.append(chipTitle)
        nameBuilder.append(dataObj.selectedName)

        return nameBuilder
    }

    /**
     * set clickListener to the list item
     */
    @CallbackProp
    fun setCheckedChangeListener(listener: CompoundButton.OnCheckedChangeListener?) {
        binding.chip.setOnCheckedChangeListener(listener)
    }
}
