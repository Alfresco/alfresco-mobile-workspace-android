package com.alfresco.content.process.ui

import ComposeTopBar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.alfresco.content.process.ui.components.AmountInputField
import com.alfresco.content.process.ui.components.CheckBoxField
import com.alfresco.content.process.ui.components.CustomLinearProgressIndicator
import com.alfresco.content.process.ui.components.DateTimeField
import com.alfresco.content.process.ui.components.IntegerInputField
import com.alfresco.content.process.ui.components.MultiLineInputField
import com.alfresco.content.process.ui.components.SingleLineInputField

@Composable
fun FormFragment(navController: NavController) {
    // This will get or create a ViewModel scoped to the Activity.
    val viewModel: FormViewModel = mavericksActivityViewModel()
    val state by viewModel.collectAsState()

    Scaffold(
        topBar = { ComposeTopBar() },
        content = { padding ->

            val colorScheme = MaterialTheme.colorScheme
            // Wrap the content in a Column with verticalScroll
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .statusBarsPadding(),
            ) {
                Surface(
                    color = colorScheme.background,
                    contentColor = colorScheme.onBackground,
                ) {
                    if (state.requestStartForm is Loading) {
                        CustomLinearProgressIndicator(padding)
                    }
                    FormDetailScreen(padding, state, viewModel)
                }
            }
        },
    )
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
            },
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,

    ) {
        state.formFields.forEach { field ->
            when (field.type) {
                FieldType.TEXT.value() -> {
                    var textFieldValue by remember { mutableStateOf(field.value as? String ?: "") }
                    SingleLineInputField(
                        textFieldValue = textFieldValue,
                        onValueChanged = { newText ->
                            textFieldValue = newText
                            viewModel.updateFieldValue(field.id, newText, state)
                        },
                        field,
                    )
                }

                FieldType.MULTI_LINE_TEXT.value() -> {
                    var textFieldValue by remember { mutableStateOf(field.value as? String ?: "") }
                    MultiLineInputField(
                        textFieldValue = textFieldValue,
                        onValueChanged = { newText ->
                            textFieldValue = newText
                            viewModel.updateFieldValue(field.id, newText, state)
                        },
                        field,
                    )
                }

                FieldType.INTEGER.value() -> {
                    var textFieldValue by remember { mutableStateOf(field.value as? String ?: "") }
                    IntegerInputField(
                        textFieldValue = textFieldValue,
                        onValueChanged = { newText ->
                            textFieldValue = newText
                            viewModel.updateFieldValue(field.id, newText, state)
                        },
                        field,
                    )
                }

                FieldType.AMOUNT.value() -> {
                    var textFieldValue by remember { mutableStateOf(field.value as? String ?: "") }
                    AmountInputField(
                        textFieldValue = textFieldValue,
                        onValueChanged = { newText ->
                            textFieldValue = newText
                            viewModel.updateFieldValue(field.id, newText, state)
                        },
                        field,
                    )
                }

                FieldType.BOOLEAN.value() -> {
                    var checkedValue by remember { mutableStateOf(field.value as? Boolean ?: false) }
                    CheckBoxField(
                        checkedValue = checkedValue,
                        onCheckChanged = { newChecked ->
                            checkedValue = newChecked
                        },
                        field,
                    )
                }

                FieldType.DATETIME.value(), FieldType.DATE.value() -> {
                    var textFieldValue by remember { mutableStateOf(field.value as? String ?: "") }
                    DateTimeField(
                        dateTimeValue = textFieldValue,
                        onValueChanged = { newText ->
                            textFieldValue = newText
                        },
                        field,
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
