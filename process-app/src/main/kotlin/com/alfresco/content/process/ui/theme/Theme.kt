package com.alfresco.content.process.ui.theme

import android.app.Activity
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color.White,
)
private val LightColorScheme = lightColorScheme(
    primary = AlfrescoBlue700, // Replace with your alfresco_blue_700 color
    surface = Color.White,
    onSurface = Color(0xFF212121), // Replace with your alfresco_gray_900 color
    background = Color.White,
    onBackground = Color.Black,
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
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

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
private fun isNightMode() = when (AppCompatDelegate.getDefaultNightMode()) {
    AppCompatDelegate.MODE_NIGHT_NO -> false
    AppCompatDelegate.MODE_NIGHT_YES -> true
    else -> isSystemInDarkTheme()
}
