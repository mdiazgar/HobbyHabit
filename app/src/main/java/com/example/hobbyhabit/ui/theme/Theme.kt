package com.example.hobbyhabit.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary             = Color(0xFF7C5CBF),  // progress bar + buttons
    onPrimary           = Color(0xFFEDE3FF),  // text on primary
    primaryContainer    = Color(0xFF2A1F45),  // location banner bg, containers
    onPrimaryContainer  = Color(0xFFC4A8F5),  // text inside primaryContainer

    tertiary            = Color(0xFF1D9E75),  // "Goal reached!" + full bar
    onTertiary          = Color(0xFFE1F5EE),

    background          = Color(0xFF0E0C14),  // page background
    onBackground        = Color(0xFFF0EBF8),  // main text

    surface             = Color(0xFF13111A),  // card background
    onSurface           = Color(0xFFF0EBF8),  // text on cards

    surfaceVariant      = Color(0xFF1C1825),  // nested surfaces, location banner
    onSurfaceVariant    = Color(0xFF9B8FB0),  // muted text — "3/4 sessions"
)

@Composable
fun HobbyHabitTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        content = content
    )
}
