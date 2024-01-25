package com.alfresco.content.process.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.alfresco.content.data.payloads.FieldType
import com.alfresco.content.data.payloads.FieldsData
import com.alfresco.content.process.R
import trailingIconColor

@Composable
fun TrailingInputField(
    focusState: Boolean = false,
    textValue: String? = null,
    errorMessage: String = "",
    isError: Boolean = false,
    fieldsData: FieldsData = FieldsData(),
    onValueChanged: (String) -> Unit = { },
) {
    if (fieldsData.type == FieldType.DATETIME.value() || fieldsData.type == FieldType.DATE.value()) {
        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = stringResource(R.string.accessibility_date_icon),
            tint = trailingIconColor(),
        )
    } else {
        if (focusState && !textValue.isNullOrEmpty()) {
            if (isError) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = errorMessage,
                    tint = MaterialTheme.colorScheme.error,
                )
            } else {
                IconButton(
                    onClick = {
                        onValueChanged("")
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = stringResource(R.string.accessibility_clear_text),
                        tint = trailingIconColor(),
                    )
                }
            }
        }
    }
}
