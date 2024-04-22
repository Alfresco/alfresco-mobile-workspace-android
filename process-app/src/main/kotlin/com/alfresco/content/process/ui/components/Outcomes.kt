package com.alfresco.content.process.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.airbnb.mvrx.compose.collectAsState
import com.alfresco.content.data.OptionsModel
import com.alfresco.content.data.payloads.FieldType
import com.alfresco.content.process.ui.fragments.FormViewModel
import com.alfresco.content.process.ui.fragments.ProcessFragment

@Composable
fun Outcomes(outcomes: List<OptionsModel>, viewModel: FormViewModel, fragment: ProcessFragment) {
    val state by viewModel.collectAsState()
    outcomes.forEach {
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            onClick = {
                val uploadList = state.formFields.filter { it.type == FieldType.UPLOAD.value() }

                val contentList = uploadList.flatMap { it.getContentList() }.filter { !it.isUpload }

                if (contentList.isNotEmpty()) {
                    viewModel.optionsModel = it
                    fragment.confirmContentQueuePrompt()
                } else {
                    viewModel.performOutcomes(it)
                }
            },
            shape = RoundedCornerShape(6.dp),
            enabled = state.enabledOutcomes,
            colors = ButtonDefaults.buttonColors(
                contentColor = Color.White,
            ),
        ) {
            Text(it.name)
        }
    }
}
