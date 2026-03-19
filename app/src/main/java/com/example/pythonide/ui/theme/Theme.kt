package com.example.pythonide.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val PythonIdeColorScheme = darkColorScheme(
    primary = Accent,
    secondary = AccentSoft,
    tertiary = Success,
    background = Background,
    surface = Surface,
    onPrimary = TextPrimary,
    onSecondary = TextPrimary,
    onTertiary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = Error
)

@Composable
fun PythonIdeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PythonIdeColorScheme,
        content = content
    )
}
