package com.alfresco.content.process.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alfresco.content.data.payloads.FieldsData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextInputField(
    value: String? = null,
    onValueChange: (String) -> Unit = { },
    fieldsData: FieldsData = FieldsData(),
    maxLines: Int = 1,
) {
    OutlinedTextField(
        value = value ?: "", // Initial value of the text field
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), // Add padding or other modifiers as needed
        label = { Text(fieldsData.name) }, // Label for the text field
        placeholder = { Text(fieldsData.placeHolder ?: "") }, // Placeholder text
        maxLines = maxLines, // Set the maximum number of lines to 1
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text), // Set keyboard type
        // Other optional parameters like keyboardOptions, keyboardActions, etc., can be added as needed
    )
}

@Preview
@Composable
fun TextInputFieldPreview() {
    TextInputField()
}
