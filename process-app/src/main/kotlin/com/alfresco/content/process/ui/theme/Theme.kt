package com.alfresco.content.process.ui.theme

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AlfrescoBlue700,
    onSurface = White60,
    onPrimary = White60,
    onSurfaceVariant = White60,
    onBackground = Color.White,
    background = designDefaultDarkBackgroundColor,
    error = AlfrescoError,
)
private val LightColorScheme = lightColorScheme(
    primary = AlfrescoBlue700,
    onPrimary = AlfrescoGray90070,
    onSurface = AlfrescoGray900,
    onSurfaceVariant = AlfrescoGray90015,
    outline = AlfrescoGray90015,
    error = AlfrescoError,
)

@Composable
fun AlfrescoBaseTheme(
    darkTheme: Boolean = isNightMode(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme.copy(
            secondary = MaterialTheme.colorScheme.primary, //
        )

        else -> LightColorScheme.copy(
            secondary = MaterialTheme.colorScheme.primary, //
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}

@Composable
fun isNightMode() = when (AppCompatDelegate.getDefaultNightMode()) {
    AppCompatDelegate.MODE_NIGHT_NO -> false
    AppCompatDelegate.MODE_NIGHT_YES -> true
    else -> isSystemInDarkTheme()
}
