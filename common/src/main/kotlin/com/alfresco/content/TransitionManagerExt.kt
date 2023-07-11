package com.alfresco.content

import android.view.Gravity
import android.view.ViewGroup
import androidx.transition.ChangeBounds
import androidx.transition.Slide
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet

private fun makeSlideUpTransition() = TransitionSet().setDuration(0L).addTransition(Slide(Gravity.TOP)).addTransition(ChangeBounds())

private fun makeSlideBottomTransition() = TransitionSet().setDuration(0L).addTransition(Slide(Gravity.BOTTOM)).addTransition(ChangeBounds())

fun ViewGroup.slideTop() = TransitionManager.beginDelayedTransition(this, makeSlideUpTransition())

fun ViewGroup.slideBottom() = TransitionManager.beginDelayedTransition(this, makeSlideBottomTransition())