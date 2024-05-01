package com.alfresco.content.process.ui.composeviews

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.airbnb.mvrx.compose.collectAsState
import com.alfresco.content.data.OptionsModel
import com.alfresco.content.data.TaskRepository
import com.alfresco.content.process.ui.components.Outcomes
import com.alfresco.content.process.ui.fragments.FormViewModel
import com.alfresco.content.process.ui.fragments.FormViewState
import com.alfresco.content.process.ui.fragments.ProcessFragment

@SuppressLint("MutableCollectionMutableState")
@Composable
fun FormDetailScreen(viewModel: FormViewModel, outcomes: List<OptionsModel>, navController: NavController, fragment: ProcessFragment, snackbarHostState: SnackbarHostState) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val state by viewModel.collectAsState()
    val focusManager = LocalFocusManager.current

    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyUp && event.key == Key.Enter) {
                    true
                } else {
                    false
                }
            },
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f, false),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            items(
                key = {
                    it.id
                },
                items = state.formFields,
            ) { field ->
                FormScrollContent(field, viewModel, state, navController, snackbarHostState)
            }
        }

        if (outcomes.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(alignment = Alignment.CenterHorizontally),
            ) {
                Outcomes(outcomes = outcomes, viewModel, fragment)
            }
        }
    }
}

@Preview
@Composable
fun PreviewProcessDetailScreen() {
    val state = FormViewState()
    FormDetailScreen(
        FormViewModel(
            state,
            LocalContext.current,
            TaskRepository(),
        ),
        emptyList(),
        rememberNavController(),
        ProcessFragment(),
        SnackbarHostState(),
    )
}
