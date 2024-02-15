package com.alfresco.content.process.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.alfresco.content.data.ProcessEntry
import com.alfresco.content.data.TaskRepository
import com.alfresco.content.data.UserGroupDetails
import com.alfresco.content.data.payloads.FieldType
import com.alfresco.content.process.FormViewModel
import com.alfresco.content.process.FormViewState
import com.alfresco.content.process.R
import com.alfresco.content.process.ui.components.AmountInputField
import com.alfresco.content.process.ui.components.CheckBoxField
import com.alfresco.content.process.ui.components.DateTimeField
import com.alfresco.content.process.ui.components.DropdownField
import com.alfresco.content.process.ui.components.HyperLinkField
import com.alfresco.content.process.ui.components.IntegerInputField
import com.alfresco.content.process.ui.components.MultiLineInputField
import com.alfresco.content.process.ui.components.PeopleField
import com.alfresco.content.process.ui.components.ReadOnlyField
import com.alfresco.content.process.ui.components.SingleLineInputField

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FormDetailScreen(state: FormViewState, viewModel: FormViewModel) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val formList by remember(state.formFields) {
        mutableStateOf(state.formFields.map { it })
    }

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            items(items = formList) { field ->
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
                            title = stringResource(id = R.string.title_workflow),
                            checkedValue = checkedValue,
                            onCheckChanged = { newChecked ->
                                checkedValue = newChecked
                                viewModel.updateFieldValue(field.id, newChecked, state)
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
                                viewModel.updateFieldValue(field.id, newText, state)
                            },
                            field,
                        )
                    }

                    FieldType.DROPDOWN.value(), FieldType.RADIO_BUTTONS.value() -> {
                        var textFieldValue by remember { mutableStateOf(field.placeHolder ?: "") }
                        var textFieldQuery by remember { mutableStateOf("") }
                        DropdownField(
                            nameText = textFieldValue,
                            queryText = textFieldQuery,
                            onValueChanged = { (newText, newQuery) ->
                                textFieldValue = newText
                                textFieldQuery = newQuery
                                viewModel.updateFieldValue(field.id, newText, state)
                            },
                            fieldsData = field,
                        )
                    }

                    FieldType.READONLY_TEXT.value(), FieldType.READONLY.value() -> {
                        val textFieldValue by remember { mutableStateOf(field.value as? String ?: "") }
                        ReadOnlyField(
                            textFieldValue = textFieldValue,
                            fieldsData = field,
                        )
                    }

                    FieldType.PEOPLE.value(), FieldType.FUNCTIONAL_GROUP.value() -> {
                        var userDetailValue by remember { mutableStateOf(field.value as? UserGroupDetails) }
                        PeopleField(
                            userDetail = userDetailValue,
                            onAssigneeSelected = { userDetails ->
                                userDetailValue = userDetails
                                viewModel.updateFieldValue(field.id, userDetails, state)
                            },
                            fieldsData = field,
                            processEntry = ProcessEntry.withProcess(state.parent, field.type),
                        )
                    }

                    FieldType.HYPERLINK.value() -> {
                        HyperLinkField(field)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewProcessDetailScreen() {
    val state = FormViewState()
    FormDetailScreen(state, FormViewModel(state, LocalContext.current, TaskRepository()))
}
