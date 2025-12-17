package com.alfresco.content.app.widget

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import coil.load
import coil.size.Scale
import coil.transform.CircleCropTransformation
import com.alfresco.content.app.R
import com.alfresco.content.app.databinding.ViewAskDiscoveryIconBinding
import com.alfresco.content.app.databinding.ViewProfileIconBinding
import com.alfresco.ui.getDrawableForAttribute

class AskDiscoveryIconView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int,
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {
    private val binding = ViewAskDiscoveryIconBinding.inflate(LayoutInflater.from(context), this)

    init {
        background = context.getDrawableForAttribute(android.R.attr.selectableItemBackgroundBorderless)
    }

    constructor(context: Context) :
        this(context, null)

    constructor(context: Context, attrs: AttributeSet?) :
        this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
        this(context, attrs, defStyleAttr, 0)
}
