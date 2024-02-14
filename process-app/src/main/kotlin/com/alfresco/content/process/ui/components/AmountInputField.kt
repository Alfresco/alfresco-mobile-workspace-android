package com.alfresco.content.process.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.alfresco.content.data.payloads.FieldsData
import com.alfresco.content.process.R

@Composable
fun AmountInputField(
    textFieldValue: String? = null,
    onValueChanged: (String) -> Unit = { },
    fieldsData: FieldsData = FieldsData(),
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

    val errorData = isValidInput(inputText = textFieldValue, fieldsData = fieldsData)

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

@Composable
fun isValidInput(inputText: String?, fieldsData: FieldsData): Pair<Boolean, String> {
    val errorData = Pair(false, "")

    if (inputText.isNullOrEmpty()) {
        return errorData
    }

    if (inputText.toFloatOrNull() == null) {
        return Pair(true, stringResource(R.string.error_invalid_format))
    }

    val minValue = fieldsData.minValue?.toFloat() ?: 0f
    val maxValue = fieldsData.maxValue?.toFloat() ?: 0f

    if (inputText.toFloat() < minValue) {
        return Pair(true, stringResource(R.string.error_min_value, minValue.toInt()))
    }

    if (inputText.toFloat() > maxValue) {
        return Pair(true, stringResource(R.string.error_max_value, maxValue.toInt()))
    }

    return errorData
}
