package com.pandora.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PandoraColorScheme = darkColorScheme(
    primary          = Color(0xFFE040FB),
    onPrimary        = Color.White,
    secondary        = Color(0xFF00BCD4),
    onSecondary      = Color.White,
    tertiary         = Color(0xFFFFD700),
    background       = Color(0xFF0A0A0F),
    surface          = Color(0xFF12121A),
    onBackground     = Color.White,
    onSurface        = Color.White,
    error            = Color(0xFFFF1744),
    outline          = Color(0xFF2A2A3E),
)

@Composable
fun PandoraTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = PandoraColorScheme, content = content)
}
