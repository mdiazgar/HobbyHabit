package com.example.hobbyhabit.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DeepOceanColors = darkColorScheme(
    primary             = Color(0xFF00D4FF),  // progress bar fill, FAB color,
    // "View on Ticketmaster" text, active nav icon

    onPrimary           = Color(0xFF003040),  // text/icon ON TOP of primary colored surfaces
    primaryContainer    = Color(0xFF004D66),  // location banner background (primaryContainer)
    onPrimaryContainer  = Color(0xFF80E8FF),  // text inside the location banner

    secondary           = Color(0xFF4A9BAA),  // secondary buttons, chips (rarely used in your app)
    onSecondary         = Color(0xFF001F26),  // text on top of secondary surfaces

    tertiary            = Color(0xFF00E5A0),  // "Goal reached!" text, progress bar at 100%,
    // the green detail card text in HobbyDetailScreen

    onTertiary          = Color(0xFF003826),  // text on top of tertiary surfaces
    tertiaryContainer   = Color(0xFF00533A),  // background of tertiary containers (unused currently)
    onTertiaryContainer = Color(0xFF60F0C0),  // text inside tertiary containers (unused currently)

    background          = Color(0xFFC1E2E4),  // the screen background behind all cards
    onBackground        = Color(0xFFE0F4F8),  // main body text on the background

    surface             = Color(0xFF71A3CA),  // every Card() background (HobbyCard, EventCard,
    // SessionItem, EventItem)

    onSurface           = Color(0xFFE0F4F8),  // primary text inside cards — hobby name,
    // event title, session notes

    surfaceVariant      = Color(0xFF162330),  // location banner when no GPS, dialog backgrounds,
    // progress bar track (unfilled portion)

    onSurfaceVariant    = Color(0xFF6B9BAA),  // muted text — "3/4 sessions this week",
    // venue name, date text, placeholder text

    outline             = Color(0xFF2A4A5A),  // OutlinedTextField borders, OutlinedButton borders,
    // card divider lines

    outlineVariant      = Color(0xFF1A3040),  // subtle dividers, less prominent borders
)

@Composable
fun HobbyHabitTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DeepOceanColors,
        content = content
    )
}