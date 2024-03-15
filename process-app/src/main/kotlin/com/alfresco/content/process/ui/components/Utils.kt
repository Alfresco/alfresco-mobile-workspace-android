package com.alfresco.content.process.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun trailingIconColor() = MaterialTheme.colorScheme.onSurfaceVariant

fun Modifier.inputField() =
    this
        .fillMaxWidth()
        .padding(start = 16.dp, end = 16.dp, top = 12.dp) // Add padding or other modifiers as needed
