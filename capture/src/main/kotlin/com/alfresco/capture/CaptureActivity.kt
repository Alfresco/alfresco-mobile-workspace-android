package com.alfresco.capture

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Bundle
import android.os.Looper
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.NavHostFragment
import com.alfresco.capture.databinding.ActivityCaptureBinding
import com.alfresco.ui.KeyHandler
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

class CaptureActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCaptureBinding

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityCaptureBinding.inflate(layoutInflater)
    setContentView(binding.root)
    configureNav()
}

private fun configureNav() {
    val navHostFragment =
        supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
    val navController = navHostFragment.navController
    val inflater = navController.navInflater
    val graph = inflater.inflate(R.navigation.nav_capture)
    navController.setGraph(graph, intent.extras)
}

override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
    val fragment =
        supportFragmentManager
            .primaryNavigationFragment
            ?.childFragmentManager
            ?.primaryNavigationFragment
    return if (fragment is KeyHandler && fragment.onKeyDown(keyCode, event)) {
        true
    } else {
        super.onKeyDown(keyCode, event)
    }
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
                val imm = getSystemService(InputMethodManager::class.java)
                imm.hideSoftInputFromWindow(v.windowToken, 0)
            }
        }
    }
    return super.dispatchTouchEvent(event)
}
}
