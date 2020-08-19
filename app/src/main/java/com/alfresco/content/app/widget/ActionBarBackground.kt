package com.alfresco.content.app.widget

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.Dimension
import androidx.annotation.StyleableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import com.alfresco.content.app.R
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

class ActionBarBackground(
    context: Context?,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : View(context, attrs, defStyleAttr, defStyleRes) {

    val background: MaterialShapeDrawable
    var shapeAppearanceModel: ShapeAppearanceModel = ShapeAppearanceModel()
        set(value) {
            field = value
            background.shapeAppearanceModel = value
        }

    var radius: Float
        get() = background.topLeftCornerResolvedSize
        set(value) {
            shapeAppearanceModel = shapeAppearanceModel.withCornerSize(value)
        }

    @Dimension
    var strokeWidth: Int = 0
        set(value) {
            field = value
            updateStroke()
        }

    var strokeColor: ColorStateList? = null
        set(value) {
            field = value
            updateStroke()
        }

    constructor(context: Context) : this(context, null)

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0, 0)

    init {
        requireNotNull(context)

        // Setup background
        background = MaterialShapeDrawable(context, attrs, defStyleAttr, defStyleRes)
        shapeAppearanceModel = background.shapeAppearanceModel
        ViewCompat.setBackground(this, background)

        // Load attributes
        val attributes = context.obtainStyledAttributes(
            attrs,
            R.styleable.ActionBarBackground,
            defStyleAttr,
            0
        )

        loadFromAttributes(context, attributes)

        attributes.recycle()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            foreground = ColorDrawable()
        }
    }

    private fun loadFromAttributes(context: Context, attributes: TypedArray) {
        strokeColor =
            getColorStateList(context, attributes, R.styleable.ActionBarBackground_strokeColor)
        strokeWidth =
            attributes.getDimensionPixelSize(R.styleable.ActionBarBackground_strokeWidth, 0)
        val backgroundColor =
            getColorStateList(context, attributes, R.styleable.ActionBarBackground_backgroundColor)

        background.fillColor = backgroundColor
        updateStroke()
    }

    private fun updateStroke() {
        background.setStroke(strokeWidth.toFloat(), strokeColor)
    }

    private fun updateBackgroundColor(color: Int) {
        background.fillColor = ColorStateList.valueOf(color)
    }

    private fun getColorStateList(
        context: Context,
        attributes: TypedArray,
        @StyleableRes index: Int
    ): ColorStateList? {
        if (attributes.hasValue(index)) {
            val resourceId = attributes.getResourceId(index, 0)
            if (resourceId != 0) {
                val value =
                    AppCompatResources.getColorStateList(context, resourceId)
                if (value != null) {
                    return value
                }
            }
        }

        return attributes.getColorStateList(index)
    }
}
