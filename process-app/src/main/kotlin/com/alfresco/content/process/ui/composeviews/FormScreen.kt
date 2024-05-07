package com.alfresco.content.process.ui.composeviews

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.airbnb.mvrx.compose.collectAsState
import com.alfresco.content.data.DefaultOutcomesID
import com.alfresco.content.data.OptionsModel
import com.alfresco.content.process.R
import com.alfresco.content.process.ui.components.FloatingActionButton
import com.alfresco.content.process.ui.fragments.FormViewModel
import com.alfresco.content.process.ui.fragments.FormViewState
import com.alfresco.content.process.ui.fragments.ProcessFragment

@Composable
fun FormScreen(navController: NavController, viewModel: FormViewModel, fragment: ProcessFragment) {
    val snackbarHostState = remember { SnackbarHostState() }

    val state by viewModel.collectAsState()

    val task = state.parent.taskEntry

    val customOutcomes = when {
        state.formFields.isEmpty() -> emptyList()
        state.processOutcomes.isEmpty() -> defaultOutcomes(state)
        task.assignee?.id == 0 -> {
            listOf(
                OptionsModel(
                    id = DefaultOutcomesID.DEFAULT_CLAIM.value(),
                    name = stringResource(id = R.string.action_menu_claim),
                ),
            )
        }

        else -> customOutcomes(state)
    }

    when {
        customOutcomes.size < 3 -> {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
            ) { padding ->
                val colorScheme = MaterialTheme.colorScheme
                Surface(
                    modifier = androidx.compose.ui.Modifier
                        .padding(padding)
                        .statusBarsPadding(),
                    color = colorScheme.background,
                    contentColor = colorScheme.onBackground,
                ) {
                    FormDetailScreen(viewModel, customOutcomes, navController, fragment, snackbarHostState)
                }
            }
        }

        else -> {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                floatingActionButton = { FloatingActionButton(customOutcomes, fragment, viewModel) },
                floatingActionButtonPosition = FabPosition.End,
            ) { padding ->
                val colorScheme = MaterialTheme.colorScheme
                Surface(
                    modifier = androidx.compose.ui.Modifier
                        .padding(padding)
                        .statusBarsPadding(),
                    color = colorScheme.background,
                    contentColor = colorScheme.onBackground,
                ) {
                    FormDetailScreen(viewModel, emptyList(), navController, fragment, snackbarHostState)
                }
            }
        }
    }
}

@Composable
private fun defaultOutcomes(state: FormViewState): List<OptionsModel> {
    if (state.parent.taskEntry.endDate != null) {
        return emptyList()
    }

    return when (state.parent.processInstanceId) {
        null -> {
            listOf(
                OptionsModel(
                    id = DefaultOutcomesID.DEFAULT_START_WORKFLOW.value(),
                    name = stringResource(id = R.string.action_start_workflow),
                ),
            )
        }

        else -> {
            listOf(
                OptionsModel(
                    id = DefaultOutcomesID.DEFAULT_SAVE.value(),
                    name = stringResource(id = R.string.action_text_save),
                ),
                OptionsModel(
                    id = DefaultOutcomesID.DEFAULT_COMPLETE.value(),
                    name = stringResource(id = R.string.text_complete),
                ),
            )
        }
    }
}

@Composable
private fun customOutcomes(state: FormViewState): List<OptionsModel> {
    return if (state.parent.processInstanceId == null) {
        state.processOutcomes
    } else {
        listOf(
            OptionsModel(
                id = DefaultOutcomesID.DEFAULT_SAVE.value(),
                name = stringResource(id = R.string.action_text_save),
            ),
        ) + state.processOutcomes
    }
}
