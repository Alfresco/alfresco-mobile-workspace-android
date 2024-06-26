package com.alfresco.content.process.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alfresco.content.component.searchusergroup.SearchUserGroupComponentBuilder
import com.alfresco.content.data.ProcessEntry
import com.alfresco.content.data.UserGroupDetails
import com.alfresco.content.data.payloads.FieldsData
import com.alfresco.content.process.R
import com.alfresco.content.process.ui.theme.AlfrescoBlue300
import com.alfresco.content.process.ui.theme.AlfrescoError

@Composable
fun PeopleField(
    userDetail: UserGroupDetails? = null,
    onAssigneeSelected: (UserGroupDetails?) -> Unit = {},
    fieldsData: FieldsData = FieldsData(),
    processEntry: ProcessEntry = ProcessEntry(),
    onValueChanged: (UserGroupDetails?) -> Unit = { },
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

    val context = LocalContext.current
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
                SearchUserGroupComponentBuilder(context, processEntry)
                    .onApply { userDetails ->
                        onAssigneeSelected(userDetails)
                    }
                    .onCancel {
                        onAssigneeSelected(null)
                    }
                    .show()
            }) {
                Icon(
                    painterResource(R.drawable.ic_edit_blue),
                    tint = AlfrescoBlue300,
                    contentDescription = "",
                )
            }
        }
        if (userDetail != null) {
            InputChip(context, userDetail, onValueChanged)
        }
    }
}

@Preview
@Composable
fun PeopleFieldPreview() {
    PeopleField()
}
