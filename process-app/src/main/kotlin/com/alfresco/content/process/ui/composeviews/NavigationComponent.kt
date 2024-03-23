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
import com.alfresco.content.process.databinding.FragmentContainerAttachFilesBinding
import com.alfresco.content.process.databinding.FragmentContainerAttachFolderBinding
import com.alfresco.content.process.ui.fragments.ProcessAttachedFilesFragment
import com.alfresco.content.search.SearchFragment

@Composable
fun NavigationComponent() {
    val navController = rememberNavController()

    Surface(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = NavigationScreen.FIRST_SCREEN.value()) {
            composable(NavigationScreen.FIRST_SCREEN.value()) {
                FormScreen(navController)
            }
            composable(NavigationScreen.ATTACHED_FILES_SCREEN.value()) {
                ProcessAttachedFilesScreen(navController)
            }
            composable(NavigationScreen.SEARCH_FOLDER_SCREEN.value()) {
                SearchFolderScreen(navController)
            }
        }
    }
}

@Composable
fun ProcessAttachedFilesScreen(navController: NavHostController) {
    val context = LocalContext.current
    AndroidViewBinding(
        FragmentContainerAttachFilesBinding::inflate,
    ) {
        // Adjust layout properties
        fragmentContainerView.layoutParams = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )

        // Adjust system UI visibility
        (context as? AppCompatActivity)?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        fragmentContainerView.setPadding(
            fragmentContainerView.paddingLeft,
            context.resources.getDimensionPixelSize(R.dimen.default_status_bar_height),
            fragmentContainerView.paddingRight,
            context.resources.getDimensionPixelSize(R.dimen.default_bottom_controller_height),
        )

        fragmentContainerView.getFragment<ProcessAttachedFilesFragment>()
    }
}

@Composable
fun SearchFolderScreen(navController: NavHostController) {
    val context = LocalContext.current
    AndroidViewBinding(
        FragmentContainerAttachFolderBinding::inflate,
    ) {
        // Adjust layout properties
        fragmentContainerView.layoutParams = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )

        // Adjust system UI visibility
        (context as? AppCompatActivity)?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        fragmentContainerView.setPadding(
            fragmentContainerView.paddingLeft,
            context.resources.getDimensionPixelSize(R.dimen.default_status_bar_height),
            fragmentContainerView.paddingRight,
            context.resources.getDimensionPixelSize(R.dimen.default_bottom_controller_height),
        )

        fragmentContainerView.getFragment<SearchFragment>()
    }
}

enum class NavigationScreen() {
    FIRST_SCREEN,
    ATTACHED_FILES_SCREEN,
    SEARCH_FOLDER_SCREEN,
    ;

    fun value() = this.name.lowercase()
}
