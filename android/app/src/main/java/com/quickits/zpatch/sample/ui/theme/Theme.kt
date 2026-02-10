package com.quickits.zpatch.sample.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Teal40,
    onPrimary = Teal95,
    primaryContainer = Teal90,
    onPrimaryContainer = Teal200,

    secondary = Orange30,
    onSecondary = Orange95,
    secondaryContainer = Orange90,
    onSecondaryContainer = Orange80,

    tertiary = Indigo40,
    onTertiary = Indigo95,

    surface = Color(0xFF0F1514),
    onSurface = Color(0xFFE0E5E4),
    surfaceVariant = Color(0xFF1D2524),
    onSurfaceVariant = Color(0xFFBFC9C7),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006A63),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF74F8EE),
    onPrimaryContainer = Color(0xFF00201C),

    secondary = Color(0xFF755900),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFCC9C),
    onSecondaryContainer = Color(0xFF261A00),

    tertiary = Color(0xFF465CA8),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFDDE1FF),
    onTertiaryContainer = Color(0xFF00174E),

    surface = Color(0xFFFBFDF9),
    onSurface = Color(0xFF191C1B),
    surfaceVariant = Color(0xFFDBE5E2),
    onSurfaceVariant = Color(0xFF404947),
)

@Composable
fun ZpatchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled to use our custom Teal theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
