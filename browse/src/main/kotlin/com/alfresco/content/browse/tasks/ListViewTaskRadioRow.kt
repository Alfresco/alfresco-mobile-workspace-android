package com.alfresco.content.browse.tasks

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.alfresco.content.common.databinding.ViewRadioListRowBinding
import com.alfresco.content.data.FilterOptions
import com.alfresco.content.getLocalizedName

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
internal class ListViewTaskRadioRow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding = ViewRadioListRowBinding.inflate(LayoutInflater.from(context), this, true)

    @ModelProp
    fun setData(options: FilterOptions) {
        binding.title.text = context.getLocalizedName(options.label ?: "")
    }

    @ModelProp
    fun setOptionSelected(isSelected: Boolean) {
        binding.radioButton.isChecked = isSelected
    }

    @CallbackProp
    fun setClickListener(listener: OnClickListener?) {
        binding.parentListRow.setOnClickListener(listener)
    }
}
