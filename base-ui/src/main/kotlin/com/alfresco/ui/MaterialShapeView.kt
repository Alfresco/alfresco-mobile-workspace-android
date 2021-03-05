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

class MaterialShapeView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val background = MaterialShapeDrawable(context, attrs, defStyleAttr, defStyleRes)
    private var onElevation: ((Float) -> Unit)? = null

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

    override fun setElevation(elevation: Float) {
        super.setElevation(elevation)
        onElevation?.invoke(elevation)
    }

    init {
        ViewCompat.setBackground(this, background)

        // Load attributes
        val attributes = context.obtainStyledAttributes(
            attrs,
            R.styleable.MaterialShapeView,
            defStyleAttr,
            defStyleRes
        )

        try {
            loadFromAttributes(attributes)
        } finally {
            attributes.recycle()
        }

        foreground = ColorDrawable()

        // Deferred elevation as this is called during base class init and would otherwise crash
        onElevation = { elevation ->
            background.elevation = elevation
        }
        background.initializeElevationOverlay(context)
        onElevation?.invoke(elevation)
    }

    constructor(context: Context) :
        this(context, null)

    constructor(context: Context, attrs: AttributeSet?) :
        this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
        this(context, attrs, defStyleAttr, 0)

    private fun loadFromAttributes(attrs: TypedArray) {
        strokeColor = attrs.getColorStateList(R.styleable.MaterialShapeView_strokeColor)
        strokeWidth = attrs.getDimensionPixelSize(R.styleable.MaterialShapeView_strokeWidth, 0)
        val backgroundColor = attrs.getColorStateList(R.styleable.MaterialShapeView_backgroundColor)
        background.fillColor = backgroundColor
        updateStroke()
    }

    private fun updateStroke() {
        background.setStroke(strokeWidth.toFloat(), strokeColor)
    }
}
