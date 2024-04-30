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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.alfresco.content.common.SharedURLParser
import com.alfresco.content.common.SharedURLParser.Companion.ID_KEY
import com.alfresco.content.common.SharedURLParser.Companion.MODE_KEY
import com.alfresco.content.data.payloads.FieldsData
import com.alfresco.content.process.R
import com.alfresco.content.process.ui.utils.inputField
import com.alfresco.content.process.ui.utils.trailingIconColor
import com.alfresco.content.viewer.ViewerActivity
import kotlinx.coroutines.launch
import java.net.URL

@Composable
fun HyperLinkField(
    fieldsData: FieldsData = FieldsData(),
    snackbarHostState: SnackbarHostState,
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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
                val url = fieldsData.hyperlinkUrl ?: ""
                if (isValidUrl(url)) {
                    val urlData =
                        SharedURLParser().getEntryIdFromShareURL(url, true)
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
                        uriHandler.openUri(url)
                    }
                } else {
                    scope.launch {
                        val message = context.getString(R.string.error_hyperlink_invalid_url, fieldsData.name)
                        snackbarHostState.showSnackbar(message)
                    }
                }
            },
        colors = OutlinedTextFieldDefaults.colors(
            disabledBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledTextColor = MaterialTheme.colorScheme.onPrimary,
            disabledPlaceholderColor = MaterialTheme.colorScheme.onPrimary,
            disabledLabelColor = MaterialTheme.colorScheme.onPrimary,
        ),
        maxLines = 1,
        textFieldValue = fieldsData.displayText,
        fieldsData = fieldsData,
        keyboardOptions = keyboardOptions,
        leadingIcon = leadingIcon,
        isEnabled = false,
    )
}

fun isValidUrl(url: String): Boolean {
    return try {
        URL(url).toURI()
        true
    } catch (e: Exception) {
        false
    }
}

@Preview
@Composable
fun HyperLinkFieldPreview() {
    HyperLinkField(snackbarHostState = SnackbarHostState())
}
