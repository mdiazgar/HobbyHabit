package com.example.hobbyhabit.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

fun progressCardColor(progress: Float): Color = when {
    progress <= 0f  -> WarmGray    // 0 sessions — gray nudge
    progress >= 1f  -> BlushPink   // goal reached — pink celebration
    else            -> CreamPeach  // in progress — default cream
}

// Your original colors
val BlushPink    = Color(0xFFEDAFB8)
val CreamPeach   = Color(0xFFF7E1D7)
val WarmGray     = Color(0xFFDEDBD2)
val SageGreen    = Color(0xFFB0C4B1)
val DarkSlate    = Color(0xFF4A5759)

// Added — close to your palette's vibe
val SoftCream    = Color(0xFFF9EEE8)  // slightly warmer than CreamPeach, screen bg
val DustyRose    = Color(0xFFD4B8BC)  // between BlushPink and WarmGray
val DeepSage     = Color(0xFF7A9E8A)  // darker sage, progress bars + active icons
val SlateBlue    = Color(0xFF6B7C7E)  // between DarkSlate and WarmGray, muted text

private val BlushSageColors = lightColorScheme(

    primary             = DeepSage,        // progress bar, FAB, active nav icons
    onPrimary           = Color(0xFFFFFAF8),

    primaryContainer    = DarkSlate,       // top app bar background
    onPrimaryContainer  = CreamPeach,      // top app bar text + icons

    secondary           = DustyRose,       // secondary buttons, chips
    onSecondary         = DarkSlate,
    secondaryContainer  = BlushPink,       // goal reached card background
    onSecondaryContainer = DarkSlate,

    tertiary            = BlushPink,       // "Goal reached!" accent
    onTertiary          = DarkSlate,
    tertiaryContainer   = BlushPink,
    onTertiaryContainer = DarkSlate,

    background          = SoftCream,       // screen background
    onBackground        = DarkSlate,

    surface             = CreamPeach,      // default card background
    onSurface           = DarkSlate,

    surfaceVariant      = WarmGray,        // location banner, dialogs, progress track
    onSurfaceVariant    = SlateBlue,       // muted text — session count, venue, date

    outline             = SageGreen,       // TextField borders, OutlinedButton
    outlineVariant      = DustyRose,
)

@Composable
fun HobbyHabitTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BlushSageColors,
        content = content
    )
}