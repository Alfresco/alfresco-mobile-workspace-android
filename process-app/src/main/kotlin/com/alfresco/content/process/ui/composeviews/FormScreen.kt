package com.alfresco.content.process.ui.composeviews

import android.app.Activity
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.compose.collectAsState
import com.alfresco.content.data.OptionsModel
import com.alfresco.content.process.R
import com.alfresco.content.process.ui.components.FloatingActionButton
import com.alfresco.content.process.ui.components.updateProcessList
import com.alfresco.content.process.ui.fragments.FormViewModel

@Composable
fun FormScreen(navController: NavController, viewModel: FormViewModel) {
    val state by viewModel.collectAsState()
    val context = LocalContext.current

    if (state.requestStartWorkflow is Success) {
        viewModel.updateProcessList()
        (context as Activity).finish()
    }

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
            Scaffold() { padding ->
                val colorScheme = MaterialTheme.colorScheme
                // Wrap the content in a Column with verticalScroll
                Surface(
                    modifier = androidx.compose.ui.Modifier
                        .padding(padding)
                        .statusBarsPadding(),
                    color = colorScheme.background,
                    contentColor = colorScheme.onBackground,
                ) {
                    FormDetailScreen(state, viewModel, customOutcomes, navController)
                }
            }
        }

        else -> {
            Scaffold(
                floatingActionButton = { FloatingActionButton(customOutcomes, state.enabledOutcomes, viewModel) },
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
                    FormDetailScreen(state, viewModel, emptyList(), navController)
                }
            }
        }
    }
}
