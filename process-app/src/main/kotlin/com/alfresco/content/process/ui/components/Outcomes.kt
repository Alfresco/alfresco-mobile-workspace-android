package com.alfresco.content.process.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alfresco.content.data.OptionsModel

@Composable
fun Outcomes(outcomes: List<OptionsModel>, enabledOutcomes: Boolean) {
    outcomes.forEach {
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            onClick = { },
            shape = RoundedCornerShape(6.dp),
            enabled = enabledOutcomes,
            colors = ButtonDefaults.buttonColors(
                contentColor = Color.White,
            ),
        ) {
            Text(it.name)
        }
    }
}
