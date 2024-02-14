package com.alfresco.content.process.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.alfresco.content.data.payloads.FieldsData

@Composable
fun ReadOnlyField(
    textFieldValue: String = "",
    fieldsData: FieldsData = FieldsData(),
) {
    val keyboardOptions = KeyboardOptions.Default.copy(
        imeAction = ImeAction.Next,
        keyboardType = KeyboardType.Text,
    )

    val textFieldColors = if (textFieldValue.isEmpty()) {
        OutlinedTextFieldDefaults.colors(
            disabledBorderColor = MaterialTheme.colorScheme.primary,
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledPlaceholderColor = MaterialTheme.colorScheme.primary,
        )
    } else
        OutlinedTextFieldDefaults.colors(
            disabledBorderColor = MaterialTheme.colorScheme.primary,
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledPlaceholderColor = MaterialTheme.colorScheme.primary,
            disabledLabelColor = MaterialTheme.colorScheme.primary,
        )

    InputField(
        colors = textFieldColors,
        modifier = Modifier
            .inputField(),
        maxLines = 1,
        textFieldValue = textFieldValue,
        fieldsData = fieldsData,
        keyboardOptions = keyboardOptions,
        isEnabled = false,
    )
}

@Preview
@Composable
fun ReadOnlyFieldPreview() {
    ReadOnlyField()
}
