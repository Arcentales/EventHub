package com.Arcentales.eventhub.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary          = Blue500,
    onPrimary        = androidx.compose.ui.graphics.Color.White,
    primaryContainer = Blue500.copy(alpha = 0.1f),
    secondary        = Cyan500,
    background       = androidx.compose.ui.graphics.Color(0xFFF8FAFC),
    surface          = androidx.compose.ui.graphics.Color.White,
    onBackground     = Slate900,
    onSurface        = Slate900,
    error            = Red500,
    outline          = Slate200
)

private val DarkColorScheme = darkColorScheme(
    primary          = Blue500,
    onPrimary        = androidx.compose.ui.graphics.Color.White,
    primaryContainer = Blue500.copy(alpha = 0.15f),
    secondary        = Cyan500,
    background       = DarkBg,
    surface          = DarkSurf,
    onBackground     = androidx.compose.ui.graphics.Color.White,
    onSurface        = androidx.compose.ui.graphics.Color.White,
    error            = Red500,
    outline          = Navy700
)

@Composable
fun EventHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,   // desactivado para usar colores propios
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
