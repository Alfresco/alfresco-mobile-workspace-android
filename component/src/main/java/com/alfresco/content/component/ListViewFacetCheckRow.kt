package com.alfresco.content.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.alfresco.content.component.databinding.ViewCheckListRowBinding
import com.alfresco.content.getLocalizedName

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
internal class ListViewFacetCheckRow
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : FrameLayout(context, attrs, defStyleAttr) {
        private val binding = ViewCheckListRowBinding.inflate(LayoutInflater.from(context), this, true)

        @ModelProp
        fun setData(options: ComponentOptions) {
            binding.title.text =
                String.format(
                    context.getString(R.string.label_count_format_integer),
                    context.getLocalizedName(options.label),
                    options.count,
                )
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
