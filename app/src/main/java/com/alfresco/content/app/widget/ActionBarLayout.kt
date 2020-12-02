package com.alfresco.content.app.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.transition.ChangeBounds
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.alfresco.content.app.R
import com.alfresco.ui.MaterialShapeView

class ActionBarLayout(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs) {

    lateinit var toolbar: Toolbar
    lateinit var background: MaterialShapeView
    lateinit var profileIcon: ImageView
    lateinit var profileView: View

    private lateinit var expandedView: View
    private lateinit var collapsedView: View

    private var originalRadius: Float = 0f
    private var originalTopMargin: Int = 0
    private var originalHorizontalMargin: Int = 0

    override fun onFinishInflate() {
        super.onFinishInflate()

        toolbar = findViewById(R.id.expanded_toolbar)
        background = findViewById(R.id.toolbar_back)
        expandedView = findViewById(R.id.expanded_toolbar)
        collapsedView = findViewById(R.id.collapsed_toolbar)
        profileIcon = findViewById(R.id.profile_icon)
        profileView = findViewById(R.id.profile_icon_frame)

        originalRadius = background.radius
        originalTopMargin = (background.layoutParams as MarginLayoutParams).topMargin
        originalHorizontalMargin = (background.layoutParams as MarginLayoutParams).marginStart
    }

    fun expand(animated: Boolean) {
        if (animated) {
            TransitionManager.beginDelayedTransition(this, makeTransition())
        }
        background.radius = 0f

        val params = background.layoutParams as MarginLayoutParams
        params.marginStart = -background.strokeWidth
        params.marginEnd = -background.strokeWidth
        params.topMargin = -background.strokeWidth
        background.layoutParams = params

        collapsedView.visibility = View.GONE
        expandedView.visibility = View.VISIBLE
    }

    fun collapse(animated: Boolean) {
        if (animated) {
            TransitionManager.beginDelayedTransition(this, makeTransition())
        }

        background.radius = originalRadius

        val params = background.layoutParams as MarginLayoutParams
        params.marginStart = originalHorizontalMargin
        params.marginEnd = originalHorizontalMargin
        params.topMargin = originalTopMargin
        background.layoutParams = params

        collapsedView.visibility = View.VISIBLE
        expandedView.visibility = View.GONE
    }

    private fun makeTransition() = TransitionSet()
        .addTransition(Fade())
        .addTransition(ChangeBounds())
}
