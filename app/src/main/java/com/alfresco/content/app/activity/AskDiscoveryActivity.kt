package com.alfresco.content.app.activity

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import com.airbnb.mvrx.InternalMavericksApi
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.withState
import com.alfresco.content.activityViewModel
import com.alfresco.content.app.R
import com.alfresco.content.app.fragment.AskDiscoveryViewModel
import com.alfresco.content.app.widget.ActionBarController

@OptIn(InternalMavericksApi::class)
class AskDiscoveryActivity : AppCompatActivity(), MavericksView {

    private val viewModel: AskDiscoveryViewModel by activityViewModel()
    private val navController by lazy { findNavController(R.id.nav_host_fragment) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.Theme_Alfresco)
        setContentView(R.layout.activity_ask_discovery)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeActionContentDescription(getString(R.string.accessibility_text_close))
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        if (!resources.getBoolean(R.bool.isTablet)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        configure()

    }

    private fun configure() =
        withState(viewModel) {
            val graph = navController.navInflater.inflate(R.navigation.nav_ask_discovery)
            graph.setStartDestination(R.id.nav_fake_door_ask_discovery)
            navController.graph = graph
        }

    override fun invalidate() {

    }
}
