package com.alfresco.content.process.ui.components

import ComposeTopBar
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksActivityViewModel
import com.alfresco.content.data.OptionsModel
import com.alfresco.content.process.FormViewModel
import com.alfresco.content.process.R
import com.alfresco.content.process.ui.FormDetailScreen

@Composable
fun FormScreen(navController: NavController) {
    // This will get or create a ViewModel scoped to the Activity.
    val viewModel: FormViewModel = mavericksActivityViewModel()
    val state by viewModel.collectAsState()

    val customOutcomes = when {
        state.formFields.isNotEmpty() && state.processOutcomes.isEmpty() -> {
            listOf(
                OptionsModel(name = stringResource(id = R.string.action_start_workflow)),
            )
        }

        else -> {
            state.processOutcomes
        }
    }

    when {
        customOutcomes.size < 3 -> {
            Scaffold(
                topBar = { ComposeTopBar() },
            ) { padding ->
                val colorScheme = MaterialTheme.colorScheme
                // Wrap the content in a Column with verticalScroll
                Surface(
                    modifier = androidx.compose.ui.Modifier
                        .padding(padding)
                        .statusBarsPadding(),
                    color = colorScheme.background,
                    contentColor = colorScheme.onBackground,
                ) {
                    if (state.requestStartForm is Loading) {
                        CustomLinearProgressIndicator(padding)
                    }
                    FormDetailScreen(state, viewModel, customOutcomes)
                }
            }
        }

        else -> {
            Scaffold(
                topBar = { ComposeTopBar() },
                floatingActionButton = { FloatingActionButton(customOutcomes, state.enabledOutcomes) },
                floatingActionButtonPosition = FabPosition.End,
            ) { padding ->
                val colorScheme = MaterialTheme.colorScheme
                // Wrap the content in a Column with verticalScroll
                Surface(
                    modifier = androidx.compose.ui.Modifier
                        .padding(padding)
                        .statusBarsPadding(),
                    color = colorScheme.background,
                    contentColor = colorScheme.onBackground,
                ) {
                    if (state.requestStartForm is Loading) {
                        CustomLinearProgressIndicator(padding)
                    }
                    FormDetailScreen(state, viewModel, emptyList())
                }
            }
        }
    }
}
