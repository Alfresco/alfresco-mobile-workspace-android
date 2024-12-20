package com.alfresco.content.process.ui.components

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alfresco.content.data.UserGroupDetails
import com.alfresco.content.getLocalizedName
import com.alfresco.content.process.R
import com.alfresco.content.process.ui.theme.SeparateColorGrayDT
import com.alfresco.content.process.ui.theme.SeparateColorGrayLT
import com.alfresco.content.process.ui.theme.chipBackgroundColorGrayDT
import com.alfresco.content.process.ui.theme.chipBackgroundColorGrayLT
import com.alfresco.content.process.ui.theme.chipColorGrayDT
import com.alfresco.content.process.ui.theme.chipColorGrayLT
import com.alfresco.content.process.ui.theme.isNightMode
import com.alfresco.content.process.ui.utils.trailingIconColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputChip(
    context: Context,
    userDetail: UserGroupDetails,
    onValueChanged: (UserGroupDetails?) -> Unit = { },
) {
    val isNightMode = isNightMode()
    InputChip(
        modifier = Modifier.padding(vertical = 8.dp),
        trailingIcon = {
            IconButton(
                onClick = {
                    onValueChanged(null)
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = stringResource(R.string.accessibility_clear_text),
                    tint = trailingIconColor(),
                )
            }
        },
        onClick = {
        },
        label = {
            if (userDetail.groupName.isNotEmpty()) {
                Text(context.getLocalizedName(userDetail.groupName))
            } else {
                Text(context.getLocalizedName(userDetail.name))
            }
        },
        selected = true,
        shape = RoundedCornerShape(24.dp),
        border = InputChipDefaults.inputChipBorder(
            selectedBorderWidth = 0.dp,
            enabled = true, // or false depending on your use case
            selected = false // or true if the chip is selected

        ),
        colors = getInputChipColors(),
        leadingIcon = {
            Text(
                color = if (isNightMode) SeparateColorGrayDT else SeparateColorGrayLT,
                modifier = Modifier
                    .padding(16.dp)
                    .drawBehind {
                        drawCircle(
                            color = if (isNightMode) chipColorGrayDT else chipColorGrayLT,
                            radius = this.size.maxDimension,
                        )
                    },
                text = context.getLocalizedName(userDetail.nameInitial),
                fontSize = 12.sp,
            )
        },
    )
}

@Preview
@Composable
fun InputChipPreview() {
    InputChip(LocalContext.current, UserGroupDetails())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun getInputChipColors() = if (isNightMode()) {
    InputChipDefaults.inputChipColors(
        labelColor = SeparateColorGrayDT,
        selectedLabelColor = SeparateColorGrayDT,
        selectedLeadingIconColor = SeparateColorGrayDT,
        selectedContainerColor = chipBackgroundColorGrayDT,
    )
} else
    InputChipDefaults.inputChipColors(
        labelColor = SeparateColorGrayLT,
        selectedLabelColor = SeparateColorGrayLT,
        selectedLeadingIconColor = SeparateColorGrayLT,
        selectedContainerColor = chipBackgroundColorGrayLT,
    )
