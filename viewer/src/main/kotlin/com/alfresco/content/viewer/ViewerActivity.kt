package com.alfresco.content.viewer

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.alfresco.content.actions.Action
import com.alfresco.content.viewer.databinding.ActivityViewerBinding
import com.alfresco.ui.getColorForAttribute

class ViewerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        // showSystemUi()

        binding = ActivityViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        title = intent.extras?.getString("title")

        ViewCompat.setOnApplyWindowInsetsListener(binding.content) { v, insets ->
            val oldInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            WindowInsetsCompat.Builder(insets)
                .setInsets(
                    WindowInsetsCompat.Type.systemBars(),
                    Insets.of(oldInsets.left, oldInsets.top + binding.toolbar.height, oldInsets.right, oldInsets.bottom + binding.bottomBar.height))
                .build()
        }

        configureStatusBar()
        configureNavigationBar()

        Action.showActionToasts(
            lifecycleScope,
            binding.root,
            binding.actionListBar
        )
    }

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)

        if (fragment is ViewerFragment) {
            fragment.arguments = intent.extras
        }
    }

    private fun configureStatusBar() {
        val color = this.getColorForAttribute(android.R.attr.statusBarColor)
        val transparentColor = ColorUtils.setAlphaComponent(color, 0x88)
        window.statusBarColor = transparentColor
        // binding.toolbar.setBackgroundColor(transparentColor)
        binding.toolbar.setBackgroundColor(transparentColor)
    }

    private fun configureNavigationBar() {
        val color = this.getColorForAttribute(android.R.attr.navigationBarColor)
        val transparentColor = ColorUtils.setAlphaComponent(color, 0x88)
        window.navigationBarColor = transparentColor
        // binding.actionListBackground.setBackgroundColor(transparentColor)
        binding.actionListBackground.setBackgroundColor(transparentColor)
    }

    var fullscreen: Boolean = false

    fun toggleFullscreen() {
        if (fullscreen) {
            // WindowCompat.setDecorFitsSystemWindows(window, true)
            WindowInsetsControllerCompat(
                window,
                binding.root
            ).show(WindowInsetsCompat.Type.systemBars())
            // showSystemUi()

            setAppVarsVisibility(View.VISIBLE)
        } else {
            val insetsController = WindowInsetsControllerCompat(
                window,
                binding.root
            )
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            // hideSystemUI()

            setAppVarsVisibility(View.GONE)
        }

        fullscreen = !fullscreen
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let {
                it.hide(WindowInsets.Type.systemBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        }
    }

    private fun showSystemUi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true)
            window.insetsController?.let {
                it.show(WindowInsets.Type.systemBars())
                // it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        }
    }

    private fun setAppVarsVisibility(visibility: Int) {
        val alpha: Float
        val toolbarYTranslation: Float
        val bottomBarYTranslation: Float

        if (visibility == View.GONE) {
            alpha = 0f
            toolbarYTranslation = -binding.toolbar.height.toFloat()
            bottomBarYTranslation = binding.bottomBar.height.toFloat()
        } else {
            alpha = 1f
            toolbarYTranslation = 0f
            bottomBarYTranslation = 0f
        }

        binding.toolbar.animate()
            .setInterpolator(DecelerateInterpolator())
            .alpha(alpha)
            .translationY(toolbarYTranslation)
        binding.bottomBar.animate()
            .setInterpolator(DecelerateInterpolator())
            .alpha(alpha)
            .translationY(bottomBarYTranslation)
    }
}
