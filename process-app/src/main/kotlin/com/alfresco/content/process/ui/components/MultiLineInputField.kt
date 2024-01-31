package com.alfresco.content.process.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.alfresco.content.data.payloads.FieldsData
import com.alfresco.content.process.R
import inputField

@Composable
fun MultiLineInputField(
    textFieldValue: String? = null,
    onValueChanged: (String) -> Unit = { },
    fieldsData: FieldsData = FieldsData(),
) {
    val keyboardOptions = KeyboardOptions.Default.copy(
        imeAction = ImeAction.None,
        keyboardType = KeyboardType.Text,
    )

    val isError = !textFieldValue.isNullOrEmpty() && textFieldValue.length < fieldsData.minLength

    val errorMessage = if (isError) {
        stringResource(R.string.error_min_length, fieldsData.minLength)
    } else {
        ""
    }

    InputField(
        modifier = Modifier.inputField(),
        maxLines = 5,
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
fun MultiLineInputFieldPreview() {
    SingleLineInputField()
}
