package com.alfresco.content.process.ui.components

import android.os.SystemClock
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity

@Composable
fun SafeClick(
    onClick: () -> Unit,
    interval: Int = 1000,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current.density
    val defaultIntervalMillis = (interval * density).toLong()

    // Remember the last time clicked using the state
    var lastTimeClicked by remember { mutableStateOf(0L) }

    Modifier.clickable {
        val currentTime = SystemClock.elapsedRealtime()
        if (currentTime - lastTimeClicked >= defaultIntervalMillis) {
            lastTimeClicked = currentTime
            onClick()
        }
    }.then(modifier)
}
