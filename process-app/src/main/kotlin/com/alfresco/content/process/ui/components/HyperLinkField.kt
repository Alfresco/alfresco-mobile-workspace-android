package com.alfresco.content.process.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.alfresco.content.data.payloads.FieldsData
import com.alfresco.content.process.R

@Composable
fun HyperLinkField(
    fieldsData: FieldsData = FieldsData(),
) {
    val uriHandler = LocalUriHandler.current

    val keyboardOptions = KeyboardOptions.Default.copy(
        imeAction = ImeAction.Next,
        keyboardType = KeyboardType.Text,
    )


    val leadingIcon: @Composable () -> Unit = {
        Icon(
            imageVector = Icons.Default.Link,
            contentDescription = stringResource(R.string.accessibility_link_icon),
            tint = trailingIconColor(),
        )
    }


    InputFieldWithLeading(
        modifier = Modifier
            .inputField()
            .clickable {
                uriHandler.openUri(fieldsData.hyperlinkUrl ?: "")
            },
        maxLines = 1,
        textFieldValue = fieldsData.displayText,
        fieldsData = fieldsData,
        keyboardOptions = keyboardOptions,
        leadingIcon = leadingIcon,
        isEnabled = false,
    )

}
