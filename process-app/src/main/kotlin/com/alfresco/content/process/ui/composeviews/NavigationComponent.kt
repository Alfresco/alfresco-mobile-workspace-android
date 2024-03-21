package com.alfresco.content.process.ui.composeviews

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alfresco.content.process.R
import com.alfresco.content.process.databinding.FragmentContainerViewBinding
import com.alfresco.content.process.ui.fragments.ProcessAttachedFilesFragment

@Composable
fun NavigationComponent() {
    val navController = rememberNavController()

    Surface(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = NavigationScreen.FIRST_SCREEN.value()) {
            composable(NavigationScreen.FIRST_SCREEN.value()) {
                // Replace with the content of your first fragment
                FormScreen(navController)
            }
            // Add more composable entries for other fragments in your navigation graph
            composable(NavigationScreen.ATTACHED_FILES_SCREEN.value()) {
                // Replace with the content of ProcessAttachedFilesFragment
                ProcessAttachedFilesScreen(navController)
            }
        }
    }
}

@Composable
fun ProcessAttachedFilesScreen(navController: NavHostController) {
    val context = LocalContext.current
    AndroidViewBinding(
        FragmentContainerViewBinding::inflate,
    ) {
        // Adjust layout properties
        fragmentContainerView.layoutParams = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )

        val contextApp = (context as? AppCompatActivity)

        println("<top>.ProcessAttachedFilesScreen $contextApp")

        // Adjust system UI visibility
        contextApp?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        fragmentContainerView.setPadding(
            fragmentContainerView.paddingLeft,
            context.resources.getDimensionPixelSize(R.dimen.default_status_bar_height),
            fragmentContainerView.paddingRight,
            context.resources.getDimensionPixelSize(R.dimen.default_bottom_controller_height),
        )

        val myFragment = fragmentContainerView.getFragment<ProcessAttachedFilesFragment>()
        // ...
    }
}

enum class NavigationScreen() {
    FIRST_SCREEN,
    ATTACHED_FILES_SCREEN,
    ;

    fun value() = this.name.lowercase()
}
