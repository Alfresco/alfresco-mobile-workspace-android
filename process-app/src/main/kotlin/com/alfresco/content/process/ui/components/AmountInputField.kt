package com.alfresco.content.process.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.alfresco.content.data.payloads.FieldsData
import com.alfresco.content.process.ui.utils.inputField

@Composable
fun AmountInputField(
    textFieldValue: String? = null,
    onValueChanged: (String) -> Unit = { },
    fieldsData: FieldsData = FieldsData(),
    errorData: Pair<Boolean, String> = Pair(false, ""),
) {
    val keyboardOptions = KeyboardOptions.Default.copy(
        imeAction = ImeAction.Next,
        keyboardType = KeyboardType.Number,
    )

    val leadingIcon: @Composable () -> Unit = when {
        !fieldsData.currency.isNullOrEmpty() -> {
            {
                Text(
                    text = fieldsData.currency ?: "$",
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }

        else -> {
            {
                Text(
                    text = "$",
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }

    InputFieldWithLeading(
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedLabelColor = MaterialTheme.colorScheme.onPrimary,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onPrimary,
            unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
        ),
        modifier = Modifier.inputField(),
        maxLines = 1,
        textFieldValue = textFieldValue,
        onValueChanged = onValueChanged,
        fieldsData = fieldsData,
        keyboardOptions = keyboardOptions,
        leadingIcon = leadingIcon,
        isError = errorData.first,
        errorMessage = errorData.second,
    )
}

@Preview
@Composable
fun AmountInputFieldPreview() {
    AmountInputField()
}
