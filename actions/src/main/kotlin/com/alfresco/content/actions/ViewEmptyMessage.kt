package com.alfresco.content.actions

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.StringRes
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.alfresco.content.actions.databinding.ViewEmptyMessageBinding

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_MATCH_HEIGHT)
class ViewEmptyMessage @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewEmptyMessageBinding.inflate(LayoutInflater.from(context), this)

    @ModelProp
    fun setTitle(@StringRes stringRes: Int) {
        binding.title.text = resources.getText(stringRes)
    }

    @ModelProp
    fun setMessage(@StringRes stringRes: Int) {
        binding.message.text = resources.getText(stringRes)
    }
}
