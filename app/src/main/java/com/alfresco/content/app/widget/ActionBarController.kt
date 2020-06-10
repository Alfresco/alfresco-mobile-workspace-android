package com.alfresco.content.app.widget

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.alfresco.content.app.R

class ActionBarController(private val layout: ActionBarLayout) {

    private lateinit var navController: NavController
    private lateinit var menu: Menu

    fun setupActionBar(activity: AppCompatActivity, navController: NavController, appBarConfiguration: AppBarConfiguration) {
        this.navController = navController

        activity.setSupportActionBar(layout.toolbar)
        activity.setupActionBarWithNavController(navController, appBarConfiguration)

        layout.profileIcon.setOnClickListener {
            navController.navigate(R.id.nav_settings)
        }

        layout.card.setOnClickListener {
            enterSearchUi()
        }

        navController.addOnDestinationChangedListener(object : NavController.OnDestinationChangedListener {
            override fun onDestinationChanged(
                controller: NavController,
                destination: NavDestination,
                arguments: Bundle?
            ) {
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
        })
    }

    fun setupOptionsMenu(menu: Menu) {
        this.menu = menu

        val searchItem: MenuItem = menu.findItem(R.id.search)
        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                exitSearchUi()
                return true
            }
        })
    }

    private fun enterSearchUi() {
        menu.findItem(R.id.search).expandActionView()
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
