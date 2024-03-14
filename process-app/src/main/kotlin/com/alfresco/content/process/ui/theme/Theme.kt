package com.alfresco.content.process.ui.theme

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AlfrescoBlue700,
    onSurface = White60,
    onSurfaceVariant = White60,
    onBackground = Color.White,
    background = designDefaultDarkBackgroundColor,
    error = AlfrescoError,
)
private val LightColorScheme = lightColorScheme(
    primary = AlfrescoBlue700,
    onSurface = AlfrescoGray900,
    onSurfaceVariant = AlfrescoGray90030,
    outline = AlfrescoGray90030,
    error = AlfrescoError,
)

@Composable
fun AlfrescoBaseTheme(
    darkTheme: Boolean = isNightMode(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val statusBarColor = if (darkTheme) {
        // Set status bar color for dark theme
        designDefaultDarkBackgroundColor
    } else {
        // Set status bar color for light theme
        AlfrescoGray900 // Replace with your desired light theme status bar color
    }

    val colorScheme = when {
        darkTheme -> DarkColorScheme.copy(
            secondary = MaterialTheme.colorScheme.primary, //
        )

        else -> LightColorScheme.copy(
            secondary = MaterialTheme.colorScheme.primary, //
        )
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = statusBarColor.toArgb()
            window.navigationBarColor = AlfrescoGray900.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
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
