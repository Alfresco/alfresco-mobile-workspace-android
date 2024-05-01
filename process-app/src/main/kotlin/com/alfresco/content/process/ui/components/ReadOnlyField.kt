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
import com.alfresco.content.DATE_FORMAT_2_1
import com.alfresco.content.DATE_FORMAT_3
import com.alfresco.content.DATE_FORMAT_8
import com.alfresco.content.data.payloads.FieldType
import com.alfresco.content.data.payloads.FieldsData
import com.alfresco.content.getLocalFormattedDate
import com.alfresco.content.getLocalFormattedDate1
import com.alfresco.content.getLocalizedName
import com.alfresco.content.process.R
import com.alfresco.content.process.ui.fragments.FormViewModel
import com.alfresco.content.process.ui.utils.inputField
import com.alfresco.content.updateDateFormat

@Composable
fun ReadOnlyField(
    viewModel: FormViewModel? = null,
    fieldsData: FieldsData = FieldsData(),
    onUserTap: (Boolean) -> Unit = {},
) {
    val keyboardOptions = KeyboardOptions.Default.copy(
        imeAction = ImeAction.Next,
        keyboardType = KeyboardType.Text,
    )

    var dateFormat: String? = null

    val textValue = when (fieldsData.params?.field?.type?.lowercase()) {
        FieldType.PEOPLE.value(), FieldType.FUNCTIONAL_GROUP.value() -> {
            LocalContext.current.getLocalizedName(fieldsData.getUserGroupDetails(viewModel?.getAPSUser())?.name ?: "")
        }

        FieldType.UPLOAD.value() -> {
            val contents = fieldsData.getContentList()
            if (contents.isEmpty()) {
                stringResource(id = R.string.no_attachments)
            } else {
                stringResource(id = R.string.text_multiple_attachment, contents.size)
            }
        }

        FieldType.DATE.value() -> {
            val date = fieldsData.value as? String ?: ""
            if (date.isNotEmpty()) {
                dateFormat = updateDateFormat(fieldsData.params?.field?.dateDisplayFormat) ?: DATE_FORMAT_2_1
                date.getLocalFormattedDate(DATE_FORMAT_3, dateFormat)
            } else {
                date
            }
        }

        FieldType.DATETIME.value() -> {
            val dateTime = fieldsData.value as? String ?: ""
            if (dateTime.isNotEmpty()) {
                dateFormat = updateDateFormat(fieldsData.params?.field?.dateDisplayFormat) ?: DATE_FORMAT_8
                dateTime.getLocalFormattedDate1(DATE_FORMAT_3, dateFormat)
            } else {
                dateTime
            }
        }

        else -> {
            when (fieldsData.value) {
                is Double -> {
                    (fieldsData.value as? Double ?: 0).toInt().toString()
                }

                is Int -> {
                    (fieldsData.value as? Int ?: 0).toString()
                }

                else -> {
                    fieldsData.value as? String ?: ""
                }
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
                onUserTap(true)
            },
        maxLines = 1,
        textFieldValue = textValue,
        fieldsData = fieldsData,
        keyboardOptions = keyboardOptions,
        isEnabled = false,
        dateFormat = dateFormat,
    )
}

@Preview
@Composable
fun ReadOnlyFieldPreview() {
    ReadOnlyField()
}
