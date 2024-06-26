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
import com.alfresco.content.DATE_FORMAT_1
import com.alfresco.content.DATE_FORMAT_2_1
import com.alfresco.content.component.DatePickerBuilder
import com.alfresco.content.data.payloads.FieldType
import com.alfresco.content.data.payloads.FieldsData
import com.alfresco.content.getLocalFormattedDate
import com.alfresco.content.process.ui.utils.inputField
import com.alfresco.content.updateDateFormat

@Composable
fun DateTimeField(
    dateTimeValue: String = "",
    onValueChanged: (String) -> Unit = {},
    fieldsData: FieldsData = FieldsData(),
    errorData: Pair<Boolean, String> = Pair(false, ""),
) {
    val keyboardOptions = KeyboardOptions.Default.copy(
        imeAction = ImeAction.Next,
        keyboardType = KeyboardType.Text,
    )

    val context = LocalContext.current

    var dateTime = dateTimeValue

    when (fieldsData.type.lowercase()) {
        FieldType.DATE.value() -> {
            val date = fieldsData.getDate(DATE_FORMAT_1, DATE_FORMAT_2_1)
            if (date.first.isNotEmpty()) {
                val dateFormat = updateDateFormat(fieldsData.params?.field?.dateDisplayFormat) ?: DATE_FORMAT_2_1
                dateTime = date.first.getLocalFormattedDate(date.second, dateFormat)
            }
        }
    }

    InputField(
        colors = OutlinedTextFieldDefaults.colors(
            disabledBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledTextColor = MaterialTheme.colorScheme.onPrimary,
            disabledPlaceholderColor = MaterialTheme.colorScheme.onPrimary,
            disabledLabelColor = MaterialTheme.colorScheme.onPrimary,
        ),
        modifier = Modifier
            .inputField()
            .clickable {
                DatePickerBuilder(
                    context = context,
                    fromDate = "",
                    isFrom = true,
                    isFutureDate = true,
                    dateFormat = DATE_FORMAT_2_1,
                    fieldsData = fieldsData,
                )
                    .onSuccess { date ->
                        onValueChanged(date)
                    }
                    .onFailure {}
                    .show()
            },
        maxLines = 1,
        textFieldValue = dateTime,
        onValueChanged = onValueChanged,
        fieldsData = fieldsData,
        keyboardOptions = keyboardOptions,
        isError = errorData.first,
        errorMessage = errorData.second,
        isEnabled = false,
    )
}

@Preview
@Composable
fun DateTimeFieldPreview() {
    DateTimeField()
}
