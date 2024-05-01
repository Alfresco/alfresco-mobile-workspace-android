package com.alfresco.content.process.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alfresco.content.data.Entry
import com.alfresco.content.data.payloads.FieldsData
import com.alfresco.content.process.R
import com.alfresco.content.process.ui.theme.AlfrescoBlue300
import com.alfresco.content.process.ui.theme.AlfrescoError

@Composable
fun AttachFilesField(
    contents: List<Entry> = emptyList(),
    fieldsData: FieldsData = FieldsData(),
    onUserTap: (Boolean) -> Unit = { },
    errorData: Pair<Boolean, String> = Pair(false, ""),
) {
    val labelWithAsterisk = buildAnnotatedString {
        append(fieldsData.name)
        if (fieldsData.required) {
            withStyle(style = SpanStyle(color = AlfrescoError)) {
                append(" *") // Adding a red asterisk for mandatory fields
            }
        }
    }

    val contentValue = if (contents.isEmpty()) {
        stringResource(id = R.string.no_attachments)
    } else {
        stringResource(id = R.string.text_multiple_attachment, contents.size)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, bottom = 0.dp, start = 16.dp, end = 16.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = labelWithAsterisk,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .align(alignment = Alignment.CenterVertically),
            )

            IconButton(onClick = {
                onUserTap(true)
            }) {
                Icon(
                    imageVector = Icons.Default.Attachment,
                    tint = AlfrescoBlue300,
                    contentDescription = "",
                )
            }
        }
        Text(
            text = contentValue,
            style = TextStyle(
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 12.sp,
            ),
            modifier = Modifier
                .padding(start = 4.dp, top = 0.dp)
                .align(alignment = Alignment.Start),
        )
    }
}

@Preview
@Composable
fun AttachFilesFieldPreview() {
    AttachFilesField()
}
