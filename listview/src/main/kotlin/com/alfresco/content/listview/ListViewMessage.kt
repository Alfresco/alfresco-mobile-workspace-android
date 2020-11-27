package com.alfresco.content.listview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.alfresco.content.listview.databinding.ViewListMessageBinding

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_MATCH_HEIGHT)
class ListViewMessage @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewListMessageBinding.inflate(LayoutInflater.from(context), this)

    @ModelProp
    fun setIconRes(@DrawableRes drawableRes: Int) {
        binding.icon.setImageResource(drawableRes)
    }

    @ModelProp
    fun setTitle(@StringRes stringRes: Int) {
        binding.title.text = resources.getText(stringRes)
    }

    @ModelProp
    fun setMessage(@StringRes stringRes: Int) {
        binding.message.text = resources.getText(stringRes)
    }
}
