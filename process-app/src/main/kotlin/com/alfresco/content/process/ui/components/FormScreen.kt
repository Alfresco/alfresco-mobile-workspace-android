package com.alfresco.content.process.ui.components

import ComposeTopBar
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksActivityViewModel
import com.alfresco.content.process.FormViewModel
import com.alfresco.content.process.ui.FormDetailScreen

@Composable
fun FormScreen(navController: NavController) {
    // This will get or create a ViewModel scoped to the Activity.
    val viewModel: FormViewModel = mavericksActivityViewModel()
    val state by viewModel.collectAsState()

    Scaffold(
        topBar = { ComposeTopBar() },
        content = { padding ->

            val colorScheme = MaterialTheme.colorScheme
            // Wrap the content in a Column with verticalScroll
            Surface(
                modifier = Modifier
                    .padding(padding)
                    .statusBarsPadding(),
                color = colorScheme.background,
                contentColor = colorScheme.onBackground,
            ) {
                if (state.requestStartForm is Loading) {
                    CustomLinearProgressIndicator(padding)
                }
                FormDetailScreen(state, viewModel)
            }

        },
    )
}
