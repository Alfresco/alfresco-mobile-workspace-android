package com.alfresco.content.listview

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import kotlinx.android.synthetic.main.view_list_message.view.icon
import kotlinx.android.synthetic.main.view_list_message.view.message
import kotlinx.android.synthetic.main.view_list_message.view.title

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_MATCH_HEIGHT)
class ListViewMessage @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        inflate(context, R.layout.view_list_message, this)
    }

    @ModelProp
    fun setIconRes(@DrawableRes drawableRes: Int) {
        icon.setImageResource(drawableRes)
    }

    @ModelProp
    fun setTitle(@StringRes stringRes: Int) {
        title.text = resources.getText(stringRes)
    }

    @ModelProp
    fun setMessage(@StringRes stringRes: Int) {
        message.text = resources.getText(stringRes)
    }
}
