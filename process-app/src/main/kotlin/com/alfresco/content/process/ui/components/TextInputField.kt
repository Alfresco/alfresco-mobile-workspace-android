package com.alfresco.content.process.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alfresco.content.data.payloads.FieldsData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextInputField(
    value: String? = null,
    onValueChanged: (String) -> Unit = { },
    fieldsData: FieldsData = FieldsData(),
    maxLines: Int = 1,
) {
    var selectionState by remember { mutableIntStateOf(0) }

    var textFieldValue by remember { mutableStateOf("") }
    // State to keep track of focus state
    val focusState = remember { mutableStateOf(false) }
    textFieldValue = value ?: ""

    val isSingleLine = maxLines == 1

    val modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp) // Add padding or other modifiers as needed
        .onFocusChanged {
            focusState.value = it.isFocused
        }

    val modifiedModifier = if (!isSingleLine) {
        modifier.height(100.dp) // Modify the height when x is true
    } else {
        modifier // Keep the original modifier when x is false
    }

    OutlinedTextField(
        value = textFieldValue, // Initial value of the text field
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

            if (value != newText) {
                // Set the cursor position after updating the text
                onValueChanged(newText)
                selectionState = cursorPosition
            }
        },
        modifier = modifiedModifier,
        label = { Text(fieldsData.name) }, // Label for the text field
        placeholder = { Text(fieldsData.placeHolder ?: "") }, // Placeholder text
        singleLine = isSingleLine,
        maxLines = maxLines, // Set the maximum number of lines to 1
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.None), // Set keyboard type
        trailingIcon = {
            if (focusState.value && textFieldValue.isNotEmpty()) {
                val iconSize = with(LocalDensity.current) { 24.dp.toPx() }
                IconButton(
                    onClick = {
                        textFieldValue = ""
                        onValueChanged("")
                    },
                ) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Clear Text",
                    )
                }
            }
        },
    )
}

@Preview
@Composable
fun TextInputFieldPreview() {
    TextInputField()
}
