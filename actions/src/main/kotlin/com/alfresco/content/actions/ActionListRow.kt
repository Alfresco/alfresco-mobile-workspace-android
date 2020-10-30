package com.alfresco.content.actions

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.alfresco.content.actions.databinding.ViewItemActionRowBinding

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ActionListRow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding = ViewItemActionRowBinding.inflate(LayoutInflater.from(context), this, true)

    @ModelProp(options = [ModelProp.Option.IgnoreRequireHashCode])
    fun setAction(action: Action) {
        binding.apply {
            title.text = resources.getString(action.title)
            icon.setImageDrawable(ResourcesCompat.getDrawable(resources, action.icon, context.theme))
        }
    }

    @CallbackProp
    fun setClickListener(listener: OnClickListener?) {
        setOnClickListener(listener)
    }
}
