package com.alfresco.content.app.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.transition.ChangeBounds
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import coil.api.load
import coil.transform.CircleCropTransformation
import com.alfresco.content.app.R
import com.alfresco.content.data.PeopleRepository

class ActionBarLayout(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs) {

    lateinit var toolbar: Toolbar
    lateinit var card: CardView
    lateinit var profileIcon: ImageView

    private lateinit var expandedView: View
    private lateinit var collapsedView: View

    private var originalRadius: Float = 0f
    private var originalTopMargin: Int = 0
    private var originalHorizontalMargin: Int = 0

    override fun onFinishInflate() {
        super.onFinishInflate()

        toolbar = findViewById(R.id.expanded_toolbar)
        card = findViewById(R.id.toolbar_back)
        expandedView = findViewById(R.id.expanded_toolbar)
        collapsedView = findViewById(R.id.collapsed_toolbar)
        profileIcon = findViewById(R.id.profile_icon)

        originalRadius = card.radius
        originalTopMargin = (card.layoutParams as MarginLayoutParams).topMargin
        originalHorizontalMargin = (card.layoutParams as MarginLayoutParams).marginStart
    }

    fun refreshData() {
        profileIcon.load(PeopleRepository.myPicture(context)) {
            placeholder(R.drawable.ic_account)
            error(R.drawable.ic_account)
            transformations(CircleCropTransformation())
        }
    }

    fun expand(animated: Boolean) {
        if (animated) {
            TransitionManager.beginDelayedTransition(this, makeTransition())
        }
        card.radius = 0f

        val params = card.layoutParams as MarginLayoutParams
        params.marginStart = 0
        params.marginEnd = 0
        params.topMargin = 0
        card.layoutParams = params

        collapsedView.visibility = View.GONE
        expandedView.visibility = View.VISIBLE
    }

    fun collapse(animated: Boolean) {
        if (animated) {
            TransitionManager.beginDelayedTransition(this, makeTransition())
        }

        card.radius = originalRadius

        val params = card.layoutParams as MarginLayoutParams
        params.marginStart = originalHorizontalMargin
        params.marginEnd = originalHorizontalMargin
        params.topMargin = originalTopMargin
        card.layoutParams = params

        collapsedView.visibility = View.VISIBLE
        expandedView.visibility = View.GONE
    }

    private fun makeTransition() = TransitionSet()
        .addTransition(Fade())
        .addTransition(ChangeBounds())
}
