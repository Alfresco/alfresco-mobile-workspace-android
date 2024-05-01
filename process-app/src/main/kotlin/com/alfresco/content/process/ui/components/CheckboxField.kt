package com.alfresco.content.process.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.alfresco.content.component.ComponentBuilder
import com.alfresco.content.component.ComponentData
import com.alfresco.content.component.ComponentType
import com.alfresco.content.data.payloads.FieldsData
import com.alfresco.content.process.R
import com.alfresco.content.process.ui.theme.AlfrescoBlue300
import com.alfresco.content.process.ui.theme.AlfrescoError

@Composable
fun CheckBoxField(
    title: String = "",
    checkedValue: Boolean = false,
    onCheckChanged: (Boolean) -> Unit = { },
    fieldsData: FieldsData = FieldsData(),
    errorData: Pair<Boolean, String> = Pair(false, ""),
) {
    val context = LocalContext.current

    val minimumLineLength = 2 // Change this to your desired value

    var showReadMoreButtonState by remember { mutableStateOf(false) }

    var visibleText by remember { mutableStateOf("") }
    val spaceAsteric = " *"
    val readMore = stringResource(id = R.string.suffix_view_all)
    val suffix = spaceAsteric + readMore
    var lineCount = 1

    val labelWithAsterisk = customLabel(visibleText, showReadMoreButtonState, fieldsData, spaceAsteric, readMore)

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
                modifier = Modifier
                    .align(alignment = Alignment.Top),
                checked = checkedValue,
                onCheckedChange = { isChecked ->
                    onCheckChanged(isChecked)
                },
            )
            ClickableText(
                maxLines = minimumLineLength,
                text = labelWithAsterisk,
                style = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                onClick = {
                    labelWithAsterisk.getStringAnnotations(it, it).firstOrNull()?.let {
                        ComponentBuilder(
                            context,
                            ComponentData(
                                name = title,
                                query = "",
                                value = fieldsData.name,
                                selector = ComponentType.VIEW_TEXT.value,
                            ),
                        )
                            .onApply { name, query, _ ->
                            }
                            .onReset { name, query, _ ->
                            }
                            .onCancel {
                            }
                            .show()
                    }
                },
                modifier = Modifier
                    .padding(end = 4.dp, top = if (lineCount == 1) 0.dp else 6.dp)
                    .align(alignment = if (lineCount == 1) Alignment.CenterVertically else Alignment.Top)
                    .clearAndSetSemantics { },
                onTextLayout = { textLayoutResult: TextLayoutResult ->
                    lineCount = textLayoutResult.lineCount
                    if (textLayoutResult.lineCount > minimumLineLength - 1 && !showReadMoreButtonState) {
                        val endIndex = textLayoutResult.getLineEnd(minimumLineLength - 1)
                        visibleText = with(labelWithAsterisk) {
                            this.substring(0, endIndex = endIndex - suffix.length)
                        }
                        showReadMoreButtonState = true
                    }
                },
            )
        }

        if (errorData.first) {
            Text(
                text = errorData.second,
                color = AlfrescoError,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 0.dp), // Adjust padding as needed
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Start,
            )
        }
    }
}

@Composable
private fun customLabel(visibleText: String, showReadMoreButtonState: Boolean, fieldsData: FieldsData, spaceAsteric: String, readMore: String) =
    if (visibleText.isNotEmpty() && showReadMoreButtonState) {
        buildAnnotatedString {
            val labelReadMore = (visibleText) + spaceAsteric + readMore

            val startIndexReadMore = labelReadMore.indexOf(readMore)
            val endIndexReadMore = startIndexReadMore + readMore.length

            val startIndexAsteric = labelReadMore.indexOf(spaceAsteric)
            val endIndexAsteric = startIndexAsteric + spaceAsteric.length

            append(labelReadMore)

            if (fieldsData.required) {
                addStyle(
                    style = SpanStyle(color = AlfrescoError),
                    start = startIndexAsteric,
                    end = endIndexAsteric,
                )
            }
            addStyle(
                style = SpanStyle(color = AlfrescoBlue300),
                start = startIndexReadMore + 1,
                end = endIndexReadMore,
            )

            addStringAnnotation(
                "tagMinified",
                readMore,
                start = startIndexReadMore,
                end = endIndexReadMore,
            )
        }
    } else {
        buildAnnotatedString {
            append(fieldsData.name)
            if (fieldsData.required) {
                withStyle(style = SpanStyle(color = AlfrescoError)) {
                    append(" *") // Adding a red asterisk for mandatory fields
                }
            }
        }
    }
