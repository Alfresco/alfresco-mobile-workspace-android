package com.alfresco.content.process.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CustomLinearProgressIndicator(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .padding(padding), // Adjust padding as needed
    ) {
        LinearProgressIndicator(
            color = Color.Blue, // Set your desired color here
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}
