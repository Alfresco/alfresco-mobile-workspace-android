package com.alfresco.content.browse

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.airbnb.epoxy.TextProp
import kotlinx.android.synthetic.main.view_browse_list_message.view.icon
import kotlinx.android.synthetic.main.view_browse_list_row.view.title

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_MATCH_HEIGHT)
class BrowseListMessageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        inflate(context, R.layout.view_browse_list_message, this)
    }

    @ModelProp
    fun setIconRes(@DrawableRes drawableRes: Int) {
        icon.setImageResource(drawableRes)
    }

    @TextProp
    fun setTitle(text: CharSequence) {
        title.text = text
    }
}