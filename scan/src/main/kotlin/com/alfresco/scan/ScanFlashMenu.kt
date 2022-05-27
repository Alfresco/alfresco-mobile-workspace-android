package com.alfresco.scan

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.alfresco.scan.databinding.ViewFlashMenuBinding
import com.google.android.material.shape.MaterialShapeDrawable

/**
 * Options for ScanFlashMenu
 */
enum class ScanFlashMenuItem {
    Auto,
    On,
    Off
}

/**
 * Marked as ScanFlashMenu
 */
class ScanFlashMenu(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding: ViewFlashMenuBinding
    var onMenuItemClick: ((ScanFlashMenuItem) -> Unit)? = null

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
                    binding.flashModeOn -> ScanFlashMenuItem.On
                    binding.flashModeOff -> ScanFlashMenuItem.Off
                    else -> ScanFlashMenuItem.Auto
                }
                onMenuItemClick?.invoke(mode)
            }
        }
    }
}
