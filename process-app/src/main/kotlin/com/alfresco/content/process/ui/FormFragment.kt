package com.alfresco.content.process.ui

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksActivityViewModel
import com.alfresco.content.data.TaskRepository
import com.alfresco.content.data.payloads.FieldType
import com.alfresco.content.process.FormViewModel
import com.alfresco.content.process.FormViewState
import com.alfresco.content.process.ui.components.CustomLinearProgressIndicator
import com.alfresco.content.process.ui.components.TextInputField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormFragment(navController: NavController) {
    val context = LocalContext.current
    // This will get or create a ViewModel scoped to the Activity.
    val viewModel: FormViewModel = mavericksActivityViewModel()
    val state by viewModel.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Workflow form")
                },
                navigationIcon = {
                    BackButton(onClick = { (context as Activity).finish() })
                },
                actions = {
                    // Add actions if any
                },
            )
        },
        content = { padding ->

            val colorScheme = MaterialTheme.colorScheme

            Surface(
                color = colorScheme.background,
                contentColor = colorScheme.onBackground,
            ) {
                if (state.requestStartForm is Loading) {
                    CustomLinearProgressIndicator(padding)
                }
                FormDetailScreen(padding, state, viewModel)
            }
        },
    )
}

@Composable
fun BackButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FormDetailScreen(padding: PaddingValues, state: FormViewState, viewModel: FormViewModel) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                // Hide the keyboard on click outside of input fields
                keyboardController?.hide()
                focusManager.clearFocus()
            }
            .padding(padding),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,

    ) {
        state.formFields.forEach { field ->
            when (field.type) {
                FieldType.TEXT.value() -> {
                    TextInputField(
                        value = field.value as? String,
                        onValueChanged = { newValue ->
                            viewModel.updateFieldValue(field.id, newValue, state)
                        },
                        field,
                    )
                }
                FieldType.MULTI_LINE_TEXT.value() -> {
                    TextInputField(
                        value = field.value as? String,
                        onValueChanged = { newValue ->
                            viewModel.updateFieldValue(field.id, newValue, state)
                        },
                        field,
                        maxLines = 4,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewProcessDetailScreen() {
    val state = FormViewState()
    FormDetailScreen(PaddingValues(16.dp), state, FormViewModel(state, LocalContext.current, TaskRepository()))
}
