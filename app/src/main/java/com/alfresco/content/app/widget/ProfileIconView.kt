package com.alfresco.content.app.widget

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import coil.load
import coil.transform.CircleCropTransformation
import com.alfresco.content.app.R
import com.alfresco.content.app.databinding.ViewProfileIconBinding
import com.alfresco.ui.getDrawableForAttribute

class ProfileIconView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int,
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding = ViewProfileIconBinding.inflate(LayoutInflater.from(context), this)

    init {
        binding.status.isVisible = false
        background = context.getDrawableForAttribute(android.R.attr.selectableItemBackgroundBorderless)
    }

    constructor(context: Context) :
        this(context, null)

    constructor(context: Context, attrs: AttributeSet?) :
        this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
        this(context, attrs, defStyleAttr, 0)

    fun loadIcon(uri: Uri) {
        binding.icon.load(uri) {
            placeholder(R.drawable.ic_account)
            error(R.drawable.ic_account)
            transformations(CircleCropTransformation())
        }
    }

    fun setOffline(value: Boolean) {
        binding.apply {
            if (value) {
                statusIcon.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_profile_status_offline,
                    ),
                )
            }
            status.isVisible = value
        }
    }
}
