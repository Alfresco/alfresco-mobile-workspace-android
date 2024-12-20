package com.alfresco.auth.activity

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.alfresco.android.aims.R
import com.alfresco.auth.AuthConfig
import com.alfresco.auth.Credentials
import com.alfresco.auth.fragments.AdvancedSettingsFragment
import com.alfresco.auth.fragments.BasicAuthFragment
import com.alfresco.auth.fragments.HelpFragment
import com.alfresco.auth.fragments.InputIdentityFragment
import com.alfresco.auth.fragments.InputServerFragment
import com.alfresco.auth.ui.AuthenticationActivity
import com.alfresco.auth.ui.observe
import com.alfresco.common.getViewModel
import com.alfresco.ui.components.TextInputLayout
import com.google.android.material.snackbar.Snackbar
import java.util.ArrayDeque

abstract class LoginActivity : AuthenticationActivity<LoginViewModel>() {
    override val viewModel: LoginViewModel by lazy {
        getViewModel {
            LoginViewModel.with(applicationContext, intent)
        }
    }

    private var ignoreStepEventOnce = false

    private lateinit var progressView: RelativeLayout

    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            ignoreStepEventOnce = true
        }

        if (resources.getBoolean(R.bool.isTablet)) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        setContentView(R.layout.activity_auth_welcome)

        setupToolbar()

        progressView = findViewById(R.id.rlProgress)
        ViewCompat.setElevation(progressView, 10f)

        observe(viewModel.hasNavigation, ::onNavigation)
        observe(viewModel.isLoading, ::onLoading)
        observe(viewModel.step, ::onMoveToStep)

        observe(viewModel.onShowHelp, ::showHelp)
        observe(viewModel.onShowSettings, ::showSettings)

        title = ""
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun onLoading(isLoading: Boolean) {
        progressView.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun onNavigation(hasNavigation: Boolean) {
        supportActionBar?.setDisplayShowHomeEnabled(hasNavigation)
        supportActionBar?.setHomeActionContentDescription(getString(R.string.accessibility_text_back))
        supportActionBar?.setDisplayHomeAsUpEnabled(hasNavigation)
    }

    private fun onMoveToStep(step: LoginViewModel.Step) {
        if (ignoreStepEventOnce) {
            ignoreStepEventOnce = false
            return
        }

        when (step) {
            LoginViewModel.Step.InputIdentityServer -> InputIdentityFragment.with(this).display()
            LoginViewModel.Step.InputAppServer -> InputServerFragment.with(this).display()
            LoginViewModel.Step.EnterBasicCredentials -> BasicAuthFragment.with(this).display()
            LoginViewModel.Step.EnterPkceCredentials -> {
                viewModel.ssoLogin()
            }

            LoginViewModel.Step.Cancelled -> finish()
        }
    }

    abstract fun onCredentials(
        credentials: Credentials,
        endpoint: String,
        authConfig: AuthConfig,
        isExtension: Boolean,
    )

    override fun onCredentials(credentials: Credentials) {
        onCredentials(credentials, viewModel.canonicalApplicationUrl, viewModel.authConfig, viewModel.isExtension)
    }

    override fun onError(error: String) {
        // Hide progress view on error
        viewModel.isLoading.value = false

        val parentLayout: View = findViewById(android.R.id.content)
        Snackbar.make(
            parentLayout,
            error,
            Snackbar.LENGTH_LONG,
        ).show()

        // Hacky way to set error state to match design
        updateAll<TextInputLayout> {
            it.setBoxStrokeColorStateList(ContextCompat.getColorStateList(this, R.color.alfresco_textinput_stroke_error)!!)
        }
    }

    protected fun onError(
        @StringRes messageResId: Int,
    ) {
        onError(resources.getString(messageResId))
    }

    private fun showSettings(
        @Suppress("UNUSED_PARAMETER") value: Int,
    ) {
        AdvancedSettingsFragment.with(this).display()
    }

    private fun showHelp(
        @StringRes msgResId: Int,
    ) {
        HelpFragment.with(this).message(msgResId).show()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        // Dismiss keyboard on touches outside editable fields
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private inline fun <reified T> updateAll(func: (T) -> Unit) {
        val queue = ArrayDeque<ViewGroup>()
        val rootView = (findViewById<ViewGroup>(android.R.id.content)).rootView as ViewGroup

        var current: ViewGroup? = rootView
        while (current != null) {
            for (i in 0..current.childCount) {
                val child = current.getChildAt(i)
                if (child is ViewGroup) {
                    queue.add(child)
                }
                if (child is T) {
                    func(child)
                }
            }

            current = queue.poll()
        }
    }
}
