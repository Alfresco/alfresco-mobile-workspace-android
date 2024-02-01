package com.alfresco.content.process.ui.components

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alfresco.content.data.UserGroupDetails
import com.alfresco.content.getLocalizedName
import com.alfresco.content.process.ui.theme.SeparateColorGray
import com.alfresco.content.process.ui.theme.chipBackgroundColorGray
import com.alfresco.content.process.ui.theme.chipColorGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputChip(
    context: Context,
    userDetail: UserGroupDetails,
) {
    InputChip(
        modifier = Modifier.padding(vertical = 8.dp),
        onClick = {
        },
        label = { Text(context.getLocalizedName(userDetail.name)) },
        selected = true,
        shape = RoundedCornerShape(24.dp),
        border = InputChipDefaults.inputChipBorder(
            selectedBorderWidth = 0.dp,

        ),
        colors = InputChipDefaults.inputChipColors(
            labelColor = SeparateColorGray,
            selectedLabelColor = SeparateColorGray,
            selectedLeadingIconColor = SeparateColorGray,
            selectedContainerColor = chipBackgroundColorGray,
        ),
        leadingIcon = {
            Text(
                color = SeparateColorGray,
                modifier = Modifier
                    .padding(16.dp)
                    .drawBehind {
                        drawCircle(
                            color = chipColorGray,
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
