package com.alfresco.ui

import android.annotation.SuppressLint
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController

@Suppress("DEPRECATION")
@SuppressLint("AnnotateVersionCheck")
object WindowCompat {
    private var systemUiVisibility: Int = 0

    // Immersive mode pre SDK 30 is not implemented correctly in androidx WindowCompat
    // Refs: https://issuetracker.google.com/issues/173203649
    fun enterImmersiveMode(window: Window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let {
                it.hide(WindowInsets.Type.systemBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            systemUiVisibility = window.decorView.systemUiVisibility
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        }
    }

    fun restoreSystemUi(window: Window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true)
            window.insetsController?.show(WindowInsets.Type.systemBars())
        } else {
            window.decorView.systemUiVisibility = systemUiVisibility
        }
    }
}
