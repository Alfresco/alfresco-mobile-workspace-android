package com.alfresco.content.search.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.alfresco.content.getLocalizedName
import com.alfresco.content.models.Options
import com.alfresco.content.search.databinding.ViewRadioListRowBinding

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
internal class ListViewRadioRow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding = ViewRadioListRowBinding.inflate(LayoutInflater.from(context), this, true)

    @ModelProp
    fun setData(options: Options) {
        binding.title.text = context.getLocalizedName(options.name ?: "")
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
