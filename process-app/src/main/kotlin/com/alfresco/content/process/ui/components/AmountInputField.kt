package com.alfresco.content.process.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.alfresco.content.data.payloads.FieldsData

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
                Text(text = fieldsData.currency ?: "$")
            }
        }

        else -> {
            {
                Text(text = "$")
            }
        }
    }

    InputFieldWithLeading(
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
