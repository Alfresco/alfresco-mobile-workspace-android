package com.alfresco.capture

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.alfresco.capture.databinding.ViewFlashMenuBinding
import com.google.android.material.shape.MaterialShapeDrawable

enum class FlashMenuItem {
    Auto,
    On,
    Off,
}

class FlashMenu(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int,
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding: ViewFlashMenuBinding
    var onMenuItemClick: ((FlashMenuItem) -> Unit)? = null

    constructor(context: Context) :
        this(context, null)

    constructor(context: Context, attrs: AttributeSet?) :
        this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
        this(context, attrs, defStyleAttr, 0)

    init {
        orientation = VERTICAL
        binding = ViewFlashMenuBinding.inflate(LayoutInflater.from(context), this)
        background = MaterialShapeDrawable(context, attrs, defStyleAttr, defStyleRes)

        arrayOf(binding.flashModeAuto, binding.flashModeOn, binding.flashModeOff).map {
            it.setOnClickListener { item ->
                val mode = when (item) {
                    binding.flashModeOn -> FlashMenuItem.On
                    binding.flashModeOff -> FlashMenuItem.Off
                    else -> FlashMenuItem.Auto
                }
                onMenuItemClick?.invoke(mode)
            }
        }
    }
}
