package com.alfresco.content.process

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alfresco.content.process.ui.components.FormScreen

@Composable
fun NavigationComponent() {
    val navController = rememberNavController()

    Surface(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = "first_screen") {
            composable("first_screen") {
                // Replace with the content of your first fragment
                FormScreen(navController)
            }
            // Add more composable entries for other fragments in your navigation graph
        }
    }
}
