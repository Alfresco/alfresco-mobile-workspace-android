package com.alfresco.content.process.ui.composeviews

import android.view.LayoutInflater
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.alfresco.content.process.R

@Composable
fun ProcessAttachedFiles() {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            // Inflate your XML layout here
            LayoutInflater.from(context).inflate(R.layout.fragment_attach_files, null)
        },
    )
}

@Composable
fun BackButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
    }
}
