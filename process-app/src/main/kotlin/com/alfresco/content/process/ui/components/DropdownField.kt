package com.alfresco.content.process.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.alfresco.content.component.ComponentBuilder
import com.alfresco.content.component.ComponentData
import com.alfresco.content.data.payloads.FieldsData
import com.alfresco.content.process.R

@Composable
fun DropdownField(
    nameText: String = "",
    queryText: String = "",
    onValueChanged: (Pair<String, String>) -> Unit = { },
    fieldsData: FieldsData = FieldsData(),
) {
    val keyboardOptions = KeyboardOptions.Default.copy(
        imeAction = ImeAction.Next,
        keyboardType = KeyboardType.Text,
    )

    val isError = nameText.isNotEmpty() && nameText.length < fieldsData.minLength

    val errorMessage = if (isError) {
        stringResource(R.string.error_required_field, fieldsData.minLength)
    } else {
        ""
    }

    val textFieldColors = if (nameText.isEmpty()) {
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

    val context = LocalContext.current

    InputField(
        colors = textFieldColors,
        modifier = Modifier
            .inputField()
            .clickable {
                val componentData = ComponentData.with(
                    fieldsData,
                    nameText,
                    queryText,
                )
                ComponentBuilder(context, componentData)
                    .onApply { name, query, _ ->
                        onValueChanged(Pair(name, query))
                    }
                    .onReset { name, query, _ ->
                        onValueChanged(Pair(fieldsData.placeHolder ?: "", ""))
                    }
                    .onCancel {
                        onValueChanged(Pair(nameText, queryText))
                    }
                    .show()
            },
        maxLines = 1,
        textFieldValue = nameText,
        fieldsData = fieldsData,
        keyboardOptions = keyboardOptions,
        isError = isError,
        errorMessage = errorMessage,
        isEnabled = false,
    )
}

@Preview
@Composable
fun DropdownFieldPreview() {
    DropdownField()
}
