package com.alfresco.content.move.widget

import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.ui.setupActionBarWithNavController

class ActionBarController(private val layout: MoveActionBarLayout) {

    private lateinit var navController: NavController

    /**
     * setup the actionbar without appbar
     */
    fun setupActionBar(activity: AppCompatActivity, navController: NavController) {
        this.navController = navController

        activity.setSupportActionBar(layout.toolbar)

        activity.setupActionBarWithNavController(navController)

        layout.expand(false)
    }
}
