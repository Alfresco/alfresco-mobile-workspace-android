package com.alfresco.content.listview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.ModelView
import com.airbnb.epoxy.TextProp
import com.alfresco.content.listview.databinding.ViewListGroupHeaderBinding

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class ListViewGroupHeader
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : FrameLayout(context, attrs, defStyleAttr) {
        private val binding = ViewListGroupHeaderBinding.inflate(LayoutInflater.from(context), this, true)

        @TextProp
        fun setTitle(text: CharSequence) {
            binding.title.text = text
            binding.parent.contentDescription = context.getString(R.string.accessibility_text_header, text)
        }
    }
