package com.alfresco.content.search

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.alfresco.content.search.databinding.ViewRecentSearchRowBinding

@ModelView(autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT)
class RecentSearchRow
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : FrameLayout(context, attrs, defStyleAttr) {
        private val binding = ViewRecentSearchRowBinding.inflate(LayoutInflater.from(context), this, true)

        @ModelProp
        fun setTitle(text: String) {
            binding.title.text = text
        }

        @CallbackProp
        fun setClickListener(listener: OnClickListener?) {
            setOnClickListener(listener)
        }
    }
