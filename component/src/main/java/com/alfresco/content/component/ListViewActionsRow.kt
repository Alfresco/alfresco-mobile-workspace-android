package com.alfresco.content.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.alfresco.content.component.databinding.ViewActionsListRowBinding
import com.alfresco.content.getLocalizedName

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
internal class ListViewActionsRow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding =
        ViewActionsListRowBinding.inflate(LayoutInflater.from(context), this, true)

    @ModelProp
    fun setData(options: ComponentOptions) {
        binding.actionButton.text = context.getLocalizedName(options.label)
    }

    @CallbackProp
    fun setClickListener(listener: OnClickListener?) {
        binding.actionButton.setOnClickListener(listener)
    }
}
