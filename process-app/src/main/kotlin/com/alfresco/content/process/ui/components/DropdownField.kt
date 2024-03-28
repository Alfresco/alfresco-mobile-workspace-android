package com.alfresco.content.process.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.alfresco.content.component.ComponentBuilder
import com.alfresco.content.component.ComponentData
import com.alfresco.content.data.payloads.FieldsData
import com.alfresco.content.process.ui.utils.inputField

@Composable
fun DropdownField(
    nameText: String = "",
    queryText: String = "",
    onValueChanged: (Pair<String, String>) -> Unit = { },
    fieldsData: FieldsData = FieldsData(),
    errorData: Pair<Boolean, String> = Pair(false, ""),
) {
    val keyboardOptions = KeyboardOptions.Default.copy(
        imeAction = ImeAction.Next,
        keyboardType = KeyboardType.Text,
    )

    val context = LocalContext.current

    InputField(
        colors = OutlinedTextFieldDefaults.colors(
            disabledBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
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
        isError = errorData.first,
        errorMessage = errorData.second,
        isEnabled = false,
    )
}

@Preview
@Composable
fun DropdownFieldPreview() {
    DropdownField()
}
