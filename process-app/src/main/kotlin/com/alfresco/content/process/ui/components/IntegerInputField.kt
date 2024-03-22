package com.alfresco.content.process.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.alfresco.content.data.payloads.FieldsData

@Composable
fun IntegerInputField(
    textFieldValue: String? = null,
    onValueChanged: (String) -> Unit = { },
    fieldsData: FieldsData = FieldsData(),
    errorData: Pair<Boolean, String> = Pair(false, ""),
) {
    val keyboardOptions = KeyboardOptions.Default.copy(
        imeAction = ImeAction.Next,
        keyboardType = KeyboardType.Number,
    )

    val isError by remember { mutableStateOf(errorData.first) }
    val errorMessage by remember { mutableStateOf(errorData.second) }

    InputField(
        modifier = Modifier.inputField(),
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
