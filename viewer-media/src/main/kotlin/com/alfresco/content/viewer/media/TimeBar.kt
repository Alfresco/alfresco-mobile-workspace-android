package com.alfresco.content.viewer.media

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import com.google.android.exoplayer2.ui.DefaultTimeBar
import kotlin.math.roundToInt

class TimeBar(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    timeBarAttrs: AttributeSet?
) : DefaultTimeBar(
    context,
    attrs,
    defStyleAttr,
    timeBarAttrs
) {
    init {
        val a = context.theme.obtainStyledAttributes(timeBarAttrs, R.styleable.DefaultTimeBar, 0, 0)
        try {
            val bufferedColor = a.getColor(R.styleable.DefaultTimeBar_buffered_color, DEFAULT_BUFFERED_COLOR)
            setBufferedColor(getColorWithAlpha(bufferedColor, BUFFERED_COLOR_ALPHA))

            val unplayedColor = a.getColor(R.styleable.DefaultTimeBar_unplayed_color, DEFAULT_UNPLAYED_COLOR)
            setUnplayedColor(getColorWithAlpha(unplayedColor, UNPLAYED_COLOR_ALPHA))
        } finally {
            a.recycle()
        }
    }

    constructor(context: Context) :
        this(context, null)

    constructor(context: Context, attrs: AttributeSet?) :
        this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
        this(context, attrs, defStyleAttr, attrs)

    private fun getColorWithAlpha(color: Int, ratio: Float): Int {
        var newColor = 0
        val alpha = (Color.alpha(color) * ratio).roundToInt()
        val r: Int = Color.red(color)
        val g: Int = Color.green(color)
        val b: Int = Color.blue(color)
        newColor = Color.argb(alpha, r, g, b)
        return newColor
    }

    private companion object {
        const val BUFFERED_COLOR_ALPHA = 0.7f
        const val UNPLAYED_COLOR_ALPHA = 0.3f
    }
}
