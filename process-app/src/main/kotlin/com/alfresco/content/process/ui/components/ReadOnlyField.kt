package com.alfresco.content.process.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.alfresco.content.data.payloads.FieldsData
import com.alfresco.content.process.R
import com.alfresco.content.process.ui.utils.inputField

@Composable
fun ReadOnlyField(
    textFieldValue: String = "",
    fieldsData: FieldsData = FieldsData(),
    onUserTap: (Boolean) -> Unit = { },
) {
    val keyboardOptions = KeyboardOptions.Default.copy(
        imeAction = ImeAction.Next,
        keyboardType = KeyboardType.Text,
    )

    var textValue = textFieldValue

    when (fieldsData.value) {
        is List<*> -> {
            val contents = fieldsData.getContentList()
            textValue = if (contents.isEmpty()) {
                stringResource(id = R.string.no_attachments)
            } else {
                stringResource(id = R.string.text_multiple_attachment, contents.size)
            }
        }
    }

    InputField(
        colors = OutlinedTextFieldDefaults.colors(
            disabledBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
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
    )
}

@Preview
@Composable
fun ReadOnlyFieldPreview() {
    ReadOnlyField()
}
