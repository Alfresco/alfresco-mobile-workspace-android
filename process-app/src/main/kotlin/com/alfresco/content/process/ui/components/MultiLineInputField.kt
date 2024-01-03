package com.alfresco.content.process.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.alfresco.content.data.payloads.FieldsData

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
    InputField(4, textFieldValue, onValueChanged, fieldsData, keyboardOptions)
}

@Preview
@Composable
fun MultiLineInputFieldPreview() {
    SingleLineInputField()
}
