package com.alfresco.content.process.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.alfresco.content.data.payloads.FieldsData
import com.alfresco.content.process.R
import com.alfresco.content.process.ui.theme.AlfrescoError

@Composable
fun RadioButtonField(
    selectedOption: String = "",
    selectedQuery: String = "",
    onSelectedOption: (Pair<String, String>) -> Unit = {},
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Text(
                text = labelWithAsterisk,
                modifier = Modifier.padding(end = 4.dp),
            )
            fieldsData.options.forEach { option ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = (option.id == selectedQuery),
                        onClick = {
                            onSelectedOption(Pair(option.name, option.id))
                        },
                    )
                    Text(
                        text = option.name,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        if (showError) {
            Text(
                text = stringResource(R.string.error_required_field),
                color = AlfrescoError,
                modifier = Modifier
                    .padding(start = 16.dp, top = 0.dp), // Adjust padding as needed
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Start,
            )
        }
    }
}
