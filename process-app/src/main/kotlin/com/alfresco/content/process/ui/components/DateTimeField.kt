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
import com.alfresco.content.DATE_FORMAT_4
import com.alfresco.content.component.DatePickerBuilder
import com.alfresco.content.data.payloads.FieldsData
import com.alfresco.content.process.R
import inputField

@Composable
fun DateTimeField(
    dateTimeValue: String = "",
    onValueChanged: (String) -> Unit = { },
    fieldsData: FieldsData = FieldsData(),
) {
    val keyboardOptions = KeyboardOptions.Default.copy(
        imeAction = ImeAction.Next,
        keyboardType = KeyboardType.Text,
    )

    val isError = dateTimeValue.isNotEmpty() && dateTimeValue.length < fieldsData.minLength

    val errorMessage = if (isError) {
        stringResource(R.string.error_min_length, fieldsData.minLength)
    } else {
        ""
    }

    val textFieldColors = if (dateTimeValue.isEmpty()) {
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
                DatePickerBuilder(
                    context = context,
                    fromDate = "",
                    isFrom = true,
                    isFutureDate = true,
                    dateFormat = DATE_FORMAT_4,
                    fieldsData = fieldsData,
                )
                    .onSuccess { date ->
                        onValueChanged(date)
                    }
                    .onFailure {}
                    .show()
            },
        maxLines = 1,
        textFieldValue = dateTimeValue,
        onValueChanged = onValueChanged,
        fieldsData = fieldsData,
        keyboardOptions = keyboardOptions,
        isError = isError,
        errorMessage = errorMessage,
        isEnabled = false,
    )
}

@Preview
@Composable
fun DateTimeFieldPreview() {
    DateTimeField()
}
