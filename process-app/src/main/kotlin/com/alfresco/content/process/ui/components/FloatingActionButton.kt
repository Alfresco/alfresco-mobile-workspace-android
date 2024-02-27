package com.alfresco.content.process.ui.components

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun FloatingActionButton() {
    ExtendedFloatingActionButton(
        onClick = {

        },
        containerColor = MaterialTheme.colorScheme.primary,
        icon = { Icon(Icons.Filled.PlaylistAdd, "Extended floating action button.") },
        text = { Text(text = "Actions") },
        modifier = Modifier.navigationBarsPadding()
    )

}