package com.example.hobbyhabit.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary          = Color(0xFFFFC0CB),
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFFFE4EC),
    secondary        = Color(0xFF705C40),
    tertiary         = Color(0xFF1B7A4A),
    background       = Color(0xFFF6F8F5),
    surface          = Color.White
)

@Composable
fun HobbyHabitTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
