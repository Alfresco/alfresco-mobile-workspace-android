package com.alfresco.content.move.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.Toolbar
import androidx.transition.ChangeBounds
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.alfresco.content.move.R
import com.alfresco.ui.MaterialShapeView

class MoveActionBarLayout(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs) {

    lateinit var toolbar: Toolbar
    lateinit var background: MaterialShapeView

    private lateinit var expandedView: View

    private var originalRadius: Float = 0f
    private var originalTopMargin: Int = 0
    private var originalHorizontalMargin: Int = 0

    override fun onFinishInflate() {
        super.onFinishInflate()

        toolbar = findViewById(R.id.expanded_toolbar_move)
        background = findViewById(R.id.toolbar_move_back)
        expandedView = toolbar

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

        expandedView.visibility = View.VISIBLE
    }

    private fun makeTransition() = TransitionSet()
        .addTransition(Fade())
        .addTransition(ChangeBounds())
}
