package com.alfresco.content.browse

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
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
    fun setEntry(entry: FileEntry) {
        binding.title.text = entry.title
        binding.icon.setImageDrawable(resources.getDrawable(entry.icon, context.theme))
    }

    @CallbackProp
    fun setClickListener(listener: OnClickListener?) {
        setOnClickListener(listener)
    }
}
