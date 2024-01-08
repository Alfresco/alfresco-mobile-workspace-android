package com.alfresco.content.process.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.alfresco.content.data.payloads.FieldsData
import com.alfresco.content.process.R
import com.alfresco.content.process.ui.theme.AlfrescoError

@Composable
fun InputField(
    maxLines: Int = 1,
    textFieldValue: String? = null,
    onValueChanged: (String) -> Unit = { },
    fieldsData: FieldsData = FieldsData(),
    keyboardOptions: KeyboardOptions,
    isError: Boolean = false,
    errorMessage: String = "",
) {
    var selectionState by remember { mutableIntStateOf(0) }
    // State to keep track of focus state
    var focusState by remember { mutableStateOf(false) }

    val keyboardActions = KeyboardActions(
        onDone = {
            // Handle the action when the "Done" button on the keyboard is pressed
        },
    )

    val onFocusChanged: (FocusState) -> Unit = {
        focusState = it.isFocused
    }

    val modifiedModifier: Modifier = Modifier
        .fillMaxWidth()
        .padding(start = 16.dp, end = 16.dp, top = 12.dp) // Add padding or other modifiers as needed
        .onFocusChanged(onFocusChanged)

    val adjustedModifier = if (maxLines > 1) {
        modifiedModifier.height(100.dp)
    } else {
        modifiedModifier
    }

    val customTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary, // Change focused border color
        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface, // Change unfocused border color
        errorBorderColor = AlfrescoError, // Change error border color
    )

    val labelWithAsterisk = buildAnnotatedString {
        append(fieldsData.name)
        if (fieldsData.required) {
            withStyle(style = SpanStyle(color = AlfrescoError)) {
                append(" *") // Adding a red asterisk for mandatory fields
            }
        }
    }

    OutlinedTextField(
        colors = customTextFieldColors,
        value = textFieldValue ?: "", // Initial value of the text field
        onValueChange = { newValue ->
            val newText = if (fieldsData.maxLength > 0) {
                if (newValue.length <= fieldsData.maxLength) {
                    newValue
                } else {
                    newValue.take(fieldsData.maxLength)
                }
            } else {
                newValue
            }
            // Calculate the selection range based on the cursor position
            val cursorPosition = if (selectionState > newText.length) newText.length else selectionState

            if (textFieldValue != newText) {
                // Set the cursor position after updating the text
                onValueChanged(newText)
                selectionState = cursorPosition
            }
        },
        modifier = adjustedModifier,
        label = {
            Text(
                text = labelWithAsterisk,
                modifier = Modifier.padding(end = 4.dp),
            )
        }, // Label for the text field
        placeholder = { Text(fieldsData.placeHolder ?: "") }, // Placeholder text
        maxLines = maxLines, // Set the maximum number of lines to the specified value
        keyboardOptions = keyboardOptions, // Set keyboard type
        keyboardActions = keyboardActions,
        isError = isError,
        trailingIcon = {
            if (focusState && !textFieldValue.isNullOrEmpty()) {
                val iconSize = with(LocalDensity.current) { 24.dp.toPx() }
                if (isError) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = errorMessage,
                        tint = AlfrescoError,
                    )
                } else {
                    IconButton(
                        onClick = {
                            onValueChanged("")
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = stringResource(R.string.accessibility_clear_text),
                        )
                    }
                }
            }
        },
        supportingText = {
            if (focusState) {
                Text(
                    text = errorMessage,
                    color = AlfrescoError,
                    textAlign = TextAlign.Start,
                    overflow = TextOverflow.Clip,
                )
            }
        },
    )
}

@Composable
fun InputFieldWithLeading(
    maxLines: Int = 1,
    textFieldValue: String? = null,
    onValueChanged: (String) -> Unit = { },
    fieldsData: FieldsData = FieldsData(),
    keyboardOptions: KeyboardOptions,
    leadingIcon: @Composable () -> Unit = {},
    isError: Boolean = false,
    errorMessage: String = "",
) {
    var selectionState by remember { mutableIntStateOf(0) }
    // State to keep track of focus state
    var focusState by remember { mutableStateOf(false) }

    val keyboardActions = KeyboardActions(
        onDone = {
            // Handle the action when the "Done" button on the keyboard is pressed
        },
    )

    val onFocusChanged: (FocusState) -> Unit = {
        focusState = it.isFocused
    }

    val modifiedModifier: Modifier = Modifier
        .fillMaxWidth()
        .padding(start = 16.dp, end = 16.dp, top = 12.dp) // Add padding or other modifiers as needed
        .onFocusChanged(onFocusChanged)

    val adjustedModifier = if (maxLines > 1) {
        modifiedModifier.height(100.dp)
    } else {
        modifiedModifier
    }

    val customTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary, // Change focused border color
        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface, // Change unfocused border color
        errorBorderColor = AlfrescoError, // Change error border color
    )

    val labelWithAsterisk = buildAnnotatedString {
        append(fieldsData.name)
        if (fieldsData.required) {
            withStyle(style = SpanStyle(color = AlfrescoError)) {
                append(" *") // Adding a red asterisk for mandatory fields
            }
        }
    }

    OutlinedTextField(
        colors = customTextFieldColors,
        value = textFieldValue ?: "", // Initial value of the text field
        onValueChange = { newValue ->
            val newText = if (fieldsData.maxLength > 0) {
                if (newValue.length <= fieldsData.maxLength) {
                    newValue
                } else {
                    newValue.take(fieldsData.maxLength)
                }
            } else {
                newValue
            }
            // Calculate the selection range based on the cursor position
            val cursorPosition = if (selectionState > newText.length) newText.length else selectionState

            if (textFieldValue != newText) {
                // Set the cursor position after updating the text
                onValueChanged(newText)
                selectionState = cursorPosition
            }
        },
        modifier = adjustedModifier,
        label = {
            Text(
                text = labelWithAsterisk,
                modifier = Modifier.padding(end = 4.dp),
            )
        }, // Label for the text field
        placeholder = { Text(fieldsData.placeHolder ?: "") }, // Placeholder text
        maxLines = maxLines, // Set the maximum number of lines to the specified value
        keyboardOptions = keyboardOptions, // Set keyboard type
        keyboardActions = keyboardActions,
        isError = isError,
        leadingIcon = leadingIcon,
        trailingIcon = {
            if (focusState && !textFieldValue.isNullOrEmpty()) {
                val iconSize = with(LocalDensity.current) { 24.dp.toPx() }
                if (isError) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = errorMessage,
                        tint = AlfrescoError,
                    )
                } else {
                    IconButton(
                        onClick = {
                            onValueChanged("")
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = stringResource(R.string.accessibility_clear_text),
                        )
                    }
                }
            }
        },
        supportingText = {
            if (focusState) {
                Text(
                    text = errorMessage,
                    color = AlfrescoError,
                    textAlign = TextAlign.Start,
                    overflow = TextOverflow.Clip,
                )
            }
        },
    )
}
