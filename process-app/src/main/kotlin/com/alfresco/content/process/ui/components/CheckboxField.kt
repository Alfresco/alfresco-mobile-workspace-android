package com.alfresco.content.process.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.alfresco.content.data.payloads.FieldsData
import com.alfresco.content.process.R
import com.alfresco.content.process.ui.theme.AlfrescoError

@Composable
fun CheckBoxField(
    checkedValue: Boolean = false,
    onCheckChanged: (Boolean) -> Unit = {},
    fieldsData: FieldsData = FieldsData(),
) {
    val labelWithAsterisk = buildAnnotatedString {
        append(fieldsData.name)
        if (fieldsData.required) {
            withStyle(style = SpanStyle(color = AlfrescoError)) {
                append(" *") // Adding a red asterisk for mandatory fields
            }
        }
    }

    var showError by remember { mutableStateOf(false) }

    var expandedState by remember { mutableStateOf(false) }
    var showReadMoreButtonState by remember { mutableStateOf(false) }
    val minimumLineLength = 2   // Change this to your desired value
    val maxLines = if (expandedState) Int.MAX_VALUE else minimumLineLength

    val textLayoutHandler = remember {
        TextLayoutHandler(minimumLineLength) { isEllipsized ->
            showReadMoreButtonState = isEllipsized
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            Checkbox(
                modifier = Modifier.align(alignment = Alignment.Top),
                checked = checkedValue,
                onCheckedChange = { isChecked ->
                    onCheckChanged(isChecked)
                    if (fieldsData.required) {
                        showError = !isChecked
                    }
                },
            )
            Text(
                maxLines = minimumLineLength,
                overflow = TextOverflow.Ellipsis,
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            textDecoration = TextDecoration.LineThrough,
                            color = Color.Gray
                        )
                    ) {
                        append(labelWithAsterisk)
                    }
                },
                modifier = Modifier
                    .padding(end = 4.dp, top = 6.dp)
                    .align(alignment = Alignment.Top),
                onTextLayout = { textLayoutResult: TextLayoutResult ->
                    textLayoutHandler.handleTextLayout(textLayoutResult)
                }
            )
            /*if (showReadMoreButtonState) {
                Text(
                    text = if (expandedState) "Read Less" else "Read More",
                    color = Color.Gray,
                    modifier = Modifier.clickable {
                        expandedState = !expandedState
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }*/
        }

        if (showError) {
            Text(
                text = stringResource(R.string.error_required_field),
                color = AlfrescoError,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 0.dp), // Adjust padding as needed
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Start,
            )
        }
    }
}
