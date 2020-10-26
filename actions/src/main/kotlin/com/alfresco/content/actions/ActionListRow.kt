package com.alfresco.content.actions

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import kotlinx.android.synthetic.main.view_item_action_row.view.icon
import kotlinx.android.synthetic.main.view_item_action_row.view.title

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ActionListRow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_item_action_row, this, true)
    }

    @ModelProp(options = [ModelProp.Option.IgnoreRequireHashCode])
    fun setAction(action: Action) {
        title.text = resources.getString(action.title)
        icon.setImageDrawable(ResourcesCompat.getDrawable(resources, action.icon, context.theme))
    }

    @CallbackProp
    fun setClickListener(listener: OnClickListener?) {
        setOnClickListener(listener)
    }
}
