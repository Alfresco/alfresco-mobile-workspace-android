package com.alfresco.content.browse.menu

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.alfresco.content.browse.databinding.ViewBrowseMenuRowBinding

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class BrowseMenuRow @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewBrowseMenuRowBinding.inflate(LayoutInflater.from(context), this, true)

    @ModelProp
    fun setEntry(entry: MenuEntry) {
        binding.title.text = entry.title
        binding.icon.setImageDrawable(
            ResourcesCompat.getDrawable(resources, entry.icon, context.theme)
        )
    }

    @CallbackProp
    fun setClickListener(listener: OnClickListener?) {
        setOnClickListener(listener)
    }
}
