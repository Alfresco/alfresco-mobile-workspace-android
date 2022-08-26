package com.alfresco.content.app.widget

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
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
    lateinit var taskToolbar: Toolbar
    lateinit var background: MaterialShapeView
    lateinit var profileView: ProfileIconView
    lateinit var tvSearchTitle: TextView

    private lateinit var expandedView: View
    private lateinit var collapsedView: View

    private var originalRadius: Float = 0f
    private var originalTopMargin: Int = 0
    private var originalHorizontalMargin: Int = 0

    override fun onFinishInflate() {
        super.onFinishInflate()

        toolbar = findViewById(R.id.expanded_toolbar)
        taskToolbar = findViewById(R.id.task_toolbar)
        background = findViewById(R.id.toolbar_back)
        expandedView = toolbar
        collapsedView = findViewById(R.id.collapsed_toolbar)
        profileView = findViewById(R.id.profile_icon)
        tvSearchTitle = findViewById(R.id.tv_search_title)

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

    fun collapse(animated: Boolean, isTaskScreen: Boolean = false) {
        if (animated) {
            TransitionManager.beginDelayedTransition(this, makeTransition())
        }

        if (isTaskScreen) {
            background.visibility = View.GONE
            tvSearchTitle.visibility = View.GONE
            taskToolbar.apply {
                visibility = VISIBLE
                title = context.getString(R.string.nav_title_tasks)
            }
        } else {
            taskToolbar.visibility = View.GONE
            tvSearchTitle.visibility = View.VISIBLE
            background.visibility = View.VISIBLE
            background.radius = originalRadius
        }

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

    fun loadProfileIcon(uri: Uri) {
        profileView.loadIcon(uri)
    }

    fun setOnline(value: Boolean) =
        profileView.setOffline(!value)
}
