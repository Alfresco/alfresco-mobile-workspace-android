package com.alfresco.content.app.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.withState
import com.alfresco.content.activityViewModel
import com.alfresco.content.app.R
import com.alfresco.content.app.widget.ActionBarController

/**
 * Marked as ExtensionActivity class
 */
class ExtensionActivity : AppCompatActivity(), MavericksView {

    private val viewModel: MainActivityViewModel by activityViewModel()
    private val navController by lazy { findNavController(R.id.nav_host_fragment) }
    private lateinit var actionBarController: ActionBarController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_extension)

        when (intent?.action) {
            Intent.ACTION_SEND -> {
                println("EXTENSION SINGLE IMAGE")
                if (intent.type?.startsWith("image/") == true) {
                    handleSendImage(intent)
                }
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                println("EXTENSION MULTIPLE IMAGE")
                if (intent.type?.startsWith("image/") == true) {
                    handleSendMultipleImages(intent)
                }
            }
            else -> println("EXTENSION OTHER INTENT")
        }

        configure()
    }

    private fun configure() = withState(viewModel) { state ->
        val graph = navController.navInflater.inflate(R.navigation.nav_share_extension)
        graph.startDestination = R.id.nav_extension
        navController.graph = graph

        actionBarController = ActionBarController(findViewById(R.id.toolbar))
    }

    private fun handleSendImage(intent: Intent) {
        (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
            // Update UI to reflect image being shared
        }
    }

    private fun handleSendMultipleImages(intent: Intent) {
        intent.getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM)?.let {
            // Update UI to reflect multiple images being shared
        }
    }

    override fun invalidate() = withState(viewModel) {
    }
}
