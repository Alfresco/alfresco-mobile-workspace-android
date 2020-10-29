package com.alfresco.ui

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.Dimension
import androidx.core.view.ViewCompat
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

class RoundedView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val background = MaterialShapeDrawable(context, attrs, defStyleAttr, defStyleRes)

    var backgroundColor: ColorStateList? = background.fillColor
        set(value) {
            field = value
            background.fillColor = value
        }

    var shapeAppearanceModel: ShapeAppearanceModel = background.shapeAppearanceModel
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

    init {
        ViewCompat.setBackground(this, background)

        // Load attributes
        val attributes = context.obtainStyledAttributes(
            attrs,
            R.styleable.RoundedView,
            defStyleAttr,
            defStyleRes
        )

        try {
            loadFromAttributes(attributes)
        } finally {
            attributes.recycle()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            foreground = ColorDrawable()
        }
    }

    constructor(context: Context) :
        this(context, null)

    constructor(context: Context, attrs: AttributeSet?) :
        this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
        this(context, attrs, defStyleAttr, 0)

    private fun loadFromAttributes(attrs: TypedArray) {
        strokeColor = attrs.getColorStateList(R.styleable.RoundedView_strokeColor)
        strokeWidth = attrs.getDimensionPixelSize(R.styleable.RoundedView_strokeWidth, 0)
        val backgroundColor = attrs.getColorStateList(R.styleable.RoundedView_backgroundColor)
        background.fillColor = backgroundColor
        updateStroke()
    }

    private fun updateStroke() {
        background.setStroke(strokeWidth.toFloat(), strokeColor)
    }
}
