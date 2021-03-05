package com.alfresco.ui

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt

/**
 * Retrieve the [Drawable] object associated with a particular [attribute] ID and
 * styled for the current theme.
 */
fun Context.getDrawableForAttribute(attribute: Int): Drawable? {
    val attributes = obtainStyledAttributes(intArrayOf(attribute))
    val result = attributes.getDrawable(0)
    attributes.recycle()
    return result
}

/**
 * Retrieve the color value associated with a particular [attribute] ID and
 * styled for the current theme.
 */
@ColorInt
fun Context.getColorForAttribute(attribute: Int): Int {
    val attributes = obtainStyledAttributes(intArrayOf(attribute))
    val color = attributes.getColor(0, 0)
    attributes.recycle()
    return color
}
