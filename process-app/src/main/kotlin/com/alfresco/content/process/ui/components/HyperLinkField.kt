package com.alfresco.content.process.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.UrlAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alfresco.content.data.payloads.FieldsData

@OptIn(ExperimentalTextApi::class)
@Composable
fun HyperLinkField(
    fieldsData: FieldsData = FieldsData(),
) {
    val uriHandler = LocalUriHandler.current

    val hyperlinkData = buildAnnotatedString {
        append(fieldsData.displayText)
        addStyle(
            style = SpanStyle(
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
            ),
            start = 0,
            end = fieldsData.displayText?.length ?: 0,
        )
        addUrlAnnotation(
            UrlAnnotation(fieldsData.hyperlinkUrl ?: ""),
            start = 0,
            end = fieldsData.displayText?.length ?: 0,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 0.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = fieldsData.name,
            style = TextStyle(
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
            ),
        )
        ClickableText(
            text = hyperlinkData,
            style = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
            ),
            onClick = {
                hyperlinkData
                    .getUrlAnnotations(it, it)
                    .firstOrNull()?.let { annotation ->
                        uriHandler.openUri(annotation.item.url)
                    }
            },
        )
    }
}
