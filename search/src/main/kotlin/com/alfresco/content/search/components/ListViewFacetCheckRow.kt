package com.alfresco.content.search.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.alfresco.content.data.Buckets
import com.alfresco.content.getLocalizedName
import com.alfresco.content.search.R
import com.alfresco.content.search.databinding.ViewCheckListRowBinding

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
internal class ListViewFacetCheckRow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding = ViewCheckListRowBinding.inflate(LayoutInflater.from(context), this, true)

    @ModelProp
    fun setData(options: Buckets) {
        val label = options.label ?: ""
        if (options.metrics == null) {
            binding.title.text = String.format(context.getString(R.string.label_count_format_integer), context.getLocalizedName(label), options.count)
        } else {
            binding.title.text = String.format(context.getString(R.string.label_count_format_string), context.getLocalizedName(label), options.metrics?.get(0)?.value?.count)
        }
    }

    @ModelProp
    fun setOptionSelected(isSelected: Boolean) {
        binding.checkBox.isChecked = isSelected
    }

    @CallbackProp
    fun setClickListener(listener: OnClickListener?) {
        binding.parentListRow.setOnClickListener(listener)
    }
}
