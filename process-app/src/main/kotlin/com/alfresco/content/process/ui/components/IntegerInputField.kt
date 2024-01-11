package com.alfresco.content.process.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.alfresco.content.data.payloads.FieldsData
import com.alfresco.content.process.R

@Composable
fun IntegerInputField(
    textFieldValue: String? = null,
    onValueChanged: (String) -> Unit = { },
    fieldsData: FieldsData = FieldsData(),
) {
    val keyboardOptions = KeyboardOptions.Default.copy(
        imeAction = ImeAction.Next,
        keyboardType = KeyboardType.Number,
    )

    var isError = false
    var errorMessage = ""

    if (!textFieldValue.isNullOrEmpty()) {
        val minValue = fieldsData.minValue?.toInt() ?: 0
        val maxValue = fieldsData.maxValue?.toInt() ?: 0

        if (textFieldValue.toInt() < minValue) {
            isError = true
            errorMessage = stringResource(R.string.error_min_value, minValue)
        }

        if (textFieldValue.toInt() > maxValue) {
            isError = true
            errorMessage = stringResource(R.string.error_max_value, maxValue)
        }
    }

    InputField(
        maxLines = 1,
        textFieldValue = textFieldValue,
        onValueChanged = onValueChanged,
        fieldsData = fieldsData,
        keyboardOptions = keyboardOptions,
        isError = isError,
        errorMessage = errorMessage,
    )
}

@Preview
@Composable
fun IntegerInputFieldPreview() {
    IntegerInputField()
}
