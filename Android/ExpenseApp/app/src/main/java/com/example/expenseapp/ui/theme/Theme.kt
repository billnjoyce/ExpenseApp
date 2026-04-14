package com.example.expenseapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary          = Color(0xFF007AFF),
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFD6E8FF),
    secondary        = Color(0xFF6750A4),
    onSecondary      = Color.White,
    surface          = Color(0xFFFAFAFA),
    background       = Color(0xFFF2F2F7),
    error            = Color(0xFFFF3B30),
)

private val DarkColors = darkColorScheme(
    primary          = Color(0xFF0A84FF),
    onPrimary        = Color.White,
    primaryContainer = Color(0xFF003D7A),
    secondary        = Color(0xFFB39DDB),
    surface          = Color(0xFF1C1C1E),
    background       = Color(0xFF000000),
    error            = Color(0xFFFF453A),
)

@Composable
fun ExpenseAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content:   @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography  = Typography(),
        content     = content
    )
}
