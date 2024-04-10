package com.alfresco.content.process.ui.composeviews

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.airbnb.mvrx.Mavericks
import com.alfresco.content.data.Entry
import com.alfresco.content.data.ProcessEntry
import com.alfresco.content.data.payloads.FieldType
import com.alfresco.content.data.payloads.FieldsData
import com.alfresco.content.data.payloads.UploadData
import com.alfresco.content.process.R
import com.alfresco.content.process.ui.components.AmountInputField
import com.alfresco.content.process.ui.components.AttachFilesField
import com.alfresco.content.process.ui.components.AttachFolderField
import com.alfresco.content.process.ui.components.CheckBoxField
import com.alfresco.content.process.ui.components.DateTimeField
import com.alfresco.content.process.ui.components.DropdownField
import com.alfresco.content.process.ui.components.HyperLinkField
import com.alfresco.content.process.ui.components.IntegerInputField
import com.alfresco.content.process.ui.components.MultiLineInputField
import com.alfresco.content.process.ui.components.PeopleField
import com.alfresco.content.process.ui.components.ReadOnlyField
import com.alfresco.content.process.ui.components.SingleLineInputField
import com.alfresco.content.process.ui.fragments.FormViewModel
import com.alfresco.content.process.ui.fragments.FormViewState
import com.alfresco.content.process.ui.utils.amountInputError
import com.alfresco.content.process.ui.utils.booleanInputError
import com.alfresco.content.process.ui.utils.dateTimeInputError
import com.alfresco.content.process.ui.utils.dropDownRadioInputError
import com.alfresco.content.process.ui.utils.integerInputError
import com.alfresco.content.process.ui.utils.multiLineInputError
import com.alfresco.content.process.ui.utils.singleLineInputError
import com.alfresco.content.process.ui.utils.userGroupInputError

@Composable
fun FormScrollContent(field: FieldsData, viewModel: FormViewModel, state: FormViewState, navController: NavController) {
    val context = LocalContext.current
    when (field.type) {
        FieldType.TEXT.value() -> {
            var textFieldValue by remember { mutableStateOf(field.value as? String ?: "") }
            var errorData by remember { mutableStateOf(Pair(false, "")) }

            SingleLineInputField(
                textFieldValue = textFieldValue,
                onValueChanged = { newText ->
                    textFieldValue = newText
                    errorData = singleLineInputError(newText, field, context)
                    viewModel.updateFieldValue(field.id, newText, state, errorData)
                },
                errorData = errorData,
                fieldsData = field,
            )
        }

        FieldType.MULTI_LINE_TEXT.value() -> {
            var textFieldValue by remember { mutableStateOf(field.value as? String ?: "") }
            var errorData by remember { mutableStateOf(Pair(false, "")) }

            MultiLineInputField(
                textFieldValue = textFieldValue,
                onValueChanged = { newText ->
                    textFieldValue = newText
                    errorData = multiLineInputError(newText, field, context)
                    viewModel.updateFieldValue(field.id, newText, state, errorData)
                },
                errorData = errorData,
                fieldsData = field,
            )
        }

        FieldType.INTEGER.value() -> {
            var textFieldValue by remember { mutableStateOf(field.value as? String ?: "") }
            var errorData by remember { mutableStateOf(field.errorData) }

            IntegerInputField(
                textFieldValue = textFieldValue,
                onValueChanged = { newText ->
                    textFieldValue = newText
                    errorData = integerInputError(newText, field, context)
                    viewModel.updateFieldValue(field.id, newText, state, errorData)
                },
                errorData = errorData,
                fieldsData = field,
            )
        }

        FieldType.AMOUNT.value() -> {
            var textFieldValue by remember { mutableStateOf(field.value as? String ?: "") }
            var errorData by remember { mutableStateOf(Pair(false, "")) }

            AmountInputField(
                textFieldValue = textFieldValue,
                onValueChanged = { newText ->
                    textFieldValue = newText
                    errorData = amountInputError(textFieldValue, field, context)
                    viewModel.updateFieldValue(field.id, newText, state, errorData)
                },
                errorData = errorData,
                fieldsData = field,
            )
        }

        FieldType.BOOLEAN.value() -> {
            var checkedValue by remember { mutableStateOf(field.value as? Boolean ?: false) }
            var errorData by remember { mutableStateOf(Pair(false, "")) }

            CheckBoxField(
                title = stringResource(id = R.string.title_workflow),
                checkedValue = checkedValue,
                onCheckChanged = { newChecked ->
                    checkedValue = newChecked
                    errorData = booleanInputError(newChecked, field, context)
                    viewModel.updateFieldValue(field.id, newChecked, state, errorData)
                },
                errorData = errorData,
                fieldsData = field,
            )
        }

        FieldType.DATETIME.value(), FieldType.DATE.value() -> {
            var textFieldValue by remember { mutableStateOf(field.value as? String ?: "") }
            var errorData by remember { mutableStateOf(Pair(false, "")) }

            DateTimeField(
                dateTimeValue = textFieldValue,
                onValueChanged = { newText ->
                    textFieldValue = newText
                    errorData = dateTimeInputError(newText, field, context)
                    viewModel.updateFieldValue(field.id, newText, state, errorData)
                },
                errorData = errorData,
                fieldsData = field,
            )
        }

        FieldType.DROPDOWN.value(), FieldType.RADIO_BUTTONS.value() -> {
            var textFieldValue by remember { mutableStateOf(field.value as? String ?: "") }
            var textFieldQuery by remember { mutableStateOf(field.options.find { it.name == textFieldValue }?.id ?: "") }
            var errorData by remember { mutableStateOf(Pair(false, "")) }

            DropdownField(
                nameText = textFieldValue,
                queryText = textFieldQuery,
                onValueChanged = { (newText, newQuery) ->
                    textFieldValue = newText
                    textFieldQuery = newQuery
                    errorData = dropDownRadioInputError(newText, field, context)
                    viewModel.updateFieldValue(field.id, newText, state, errorData)
                },

                errorData = errorData,
                fieldsData = field,
            )
        }

        FieldType.READONLY_TEXT.value(), FieldType.READONLY.value() -> {
            ReadOnlyField(
                viewModel = viewModel,
                fieldsData = field,
                onUserTap = {
                    if (it && field.value is List<*>) {
                        val bundle = Bundle().apply {
                            putParcelable(
                                Mavericks.KEY_ARG,
                                UploadData(
                                    field = field,
                                    process = state.parent,
                                ),
                            )
                        }
                        navController.navigate(
                            R.id.action_nav_process_form_to_nav_attach_files,
                            bundle,
                        )
                    }
                },
            )
        }

        FieldType.PEOPLE.value(), FieldType.FUNCTIONAL_GROUP.value() -> {
            var userDetailValue by remember { mutableStateOf(field.getUserGroupDetails(viewModel.getAPSUser())) }
            var errorData by remember { mutableStateOf(Pair(false, "")) }

            PeopleField(
                userDetail = userDetailValue,
                onAssigneeSelected = { userDetails ->
                    userDetailValue = userDetails
                    errorData = userGroupInputError(userDetails, field, context)
                    viewModel.updateFieldValue(field.id, userDetails, state, errorData)
                },
                fieldsData = field,
                errorData = errorData,
                processEntry = ProcessEntry.withProcess(state.parent, field.type),
            )
        }

        FieldType.HYPERLINK.value() -> {
            HyperLinkField(field)
        }

        FieldType.UPLOAD.value() -> {
            val listContents = (field.value as? List<*>)?.mapNotNull { it as? Entry } ?: emptyList()

            AttachFilesField(
                contents = listContents,
                fieldsData = field,
                onUserTap = {
                    if (it) {
                        viewModel.selectedField = field

                        val bundle = Bundle().apply {
                            putParcelable(
                                Mavericks.KEY_ARG,
                                UploadData(
                                    field = field,
                                    process = state.parent,
                                ),
                            )
                        }
                        navController.navigate(
                            R.id.action_nav_process_form_to_nav_attach_files,
                            bundle,
                        )
                    }
                },
            )
        }

        FieldType.SELECT_FOLDER.value() -> {
            AttachFolderField(
                fieldsData = field,
                navController = navController,
                onUserTap = {
                    if (it) {
                        viewModel.selectedField = field
                    }
                },
            )
        }
    }
}
