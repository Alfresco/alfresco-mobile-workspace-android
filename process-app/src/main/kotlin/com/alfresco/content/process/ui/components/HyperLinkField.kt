package com.alfresco.content.process.ui.components

import android.content.Intent
import android.provider.MediaStore.Audio.AudioColumns.TITLE_KEY
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat.startActivity
import com.alfresco.content.common.SharedURLParser
import com.alfresco.content.common.SharedURLParser.Companion.ID_KEY
import com.alfresco.content.common.SharedURLParser.Companion.MODE_KEY
import com.alfresco.content.data.payloads.FieldsData
import com.alfresco.content.process.R
import com.alfresco.content.viewer.ViewerActivity

@Composable
fun HyperLinkField(
    fieldsData: FieldsData = FieldsData(),
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

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
                val urlData =
                    SharedURLParser().getEntryIdFromShareURL(fieldsData.hyperlinkUrl ?: "", true)

                if (urlData.second.isNotEmpty()) {
                    context.startActivity(
                        Intent(context, ViewerActivity::class.java)
                            .putExtra(ID_KEY, urlData.second)
                            .putExtra(
                                MODE_KEY,
                                if (urlData.first) SharedURLParser.VALUE_SHARE else SharedURLParser.VALUE_REMOTE,
                            )
                            .putExtra(TITLE_KEY, "Preview"),
                    )
                } else {
                    uriHandler.openUri(fieldsData.hyperlinkUrl ?: "")
                }
            },
        colors = OutlinedTextFieldDefaults.colors(
            disabledLabelColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.onSurface,
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurface,
        ),
        maxLines = 1,
        textFieldValue = fieldsData.displayText,
        fieldsData = fieldsData,
        keyboardOptions = keyboardOptions,
        leadingIcon = leadingIcon,
        isEnabled = false,
    )
}

@Preview
@Composable
fun HyperLinkFieldPreview() {
    HyperLinkField()
}
