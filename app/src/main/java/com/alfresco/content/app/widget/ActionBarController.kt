package com.alfresco.content.app.widget

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.alfresco.content.app.R

class ActionBarController(private val layout: ActionBarLayout) {

    private lateinit var navController: NavController

    fun setupActionBar(activity: AppCompatActivity, navController: NavController, appBarConfiguration: AppBarConfiguration) {
        this.navController = navController

        activity.setSupportActionBar(layout.toolbar)
        activity.setupActionBarWithNavController(navController, appBarConfiguration)

        layout.profileView.setOnClickListener {
            navController.navigate(R.id.nav_settings)
        }

        layout.background.setOnClickListener {
            enterSearchUi()
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isTopLevelDestination = matchDestinations(
                destination,
                appBarConfiguration.topLevelDestinations
            )
            if (isTopLevelDestination) {
                layout.collapse(false)
            } else {
                layout.expand(false)
            }
        }
    }

    fun hideActionBarLayout() {
        layout.expand(false)
    }

    fun setProfileIcon(uri: Uri) = layout.loadProfileIcon(uri)

    fun setOnline(value: Boolean) = layout.setOnline(value)

    private fun enterSearchUi() {
        layout.expand(true)
        navController.navigate(R.id.enter_search)
    }

    private fun exitSearchUi() {
        layout.collapse(true)

        if (navController.currentDestination?.id == R.id.searchFragment) {
            navController.navigateUp()
        }
    }

    private fun matchDestinations(
        destination: NavDestination,
        destinationIds: Set<Int?>
    ): Boolean {
        return destinationIds.contains(destination.id)
    }
}
