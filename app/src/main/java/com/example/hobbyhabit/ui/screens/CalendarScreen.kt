package com.example.hobbyhabit.ui.screens

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.hobbyhabit.data.local.Event
import com.example.hobbyhabit.data.local.EventSource
import com.example.hobbyhabit.ui.viewmodel.HobbyViewModel
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale

// Map Ticketmaster category names to emoji icons as fallback images
fun categoryEmoji(category: String): String = when {
    category.contains("Theatre", ignoreCase = true) ||
            category.contains("Arts", ignoreCase = true)    -> "🎭"
    category.contains("Music", ignoreCase = true)   -> "🎵"
    category.contains("Comedy", ignoreCase = true)  -> "🎤"
    category.contains("Dance", ignoreCase = true)   -> "💃"
    category.contains("Film", ignoreCase = true)    -> "🎬"
    category.contains("Sports", ignoreCase = true)  -> "⚽"
    category.contains("Family", ignoreCase = true)  -> "👨‍👩‍👧"
    category.contains("Education", ignoreCase = true) -> "📚"
    else -> "📅"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: HobbyViewModel) {
    val month       by viewModel.calendarMonth.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()
    val allEvents   by viewModel.allEvents.collectAsState()

    val markedDays    = viewModel.daysWithEvents(month, allEvents)
    val dayEvents     = selectedDay?.let { viewModel.eventsForDay(it, allEvents) } ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Month navigator
            item {
                MonthNavigator(
                    month    = month,
                    onPrev   = { viewModel.prevMonth() },
                    onNext   = { viewModel.nextMonth() }
                )
            }

            // Day-of-week header
            item {
                DayOfWeekHeader()
            }

            // Calendar grid
            item {
                CalendarGrid(
                    month       = month,
                    markedDays  = markedDays,
                    selectedDay = selectedDay,
                    onDayClick  = { viewModel.selectDay(it) }
                )
            }

            // Selected day events
            item {
                AnimatedVisibility(
                    visible = selectedDay != null,
                    enter   = expandVertically(),
                    exit    = shrinkVertically()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        selectedDay?.let { day ->
                            Text(
                                text = day.format(java.time.format.DateTimeFormatter
                                    .ofPattern("EEEE, MMMM d")),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        if (dayEvents.isEmpty()) {
                            Text("No events on this day.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Event cards for selected day
            if (selectedDay != null && dayEvents.isNotEmpty()) {
                items(dayEvents, key = { "cal_event_${it.id}" }) { event ->
                    CalendarEventCard(
                        event    = event,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            // If no day selected, show upcoming events
            if (selectedDay == null) {
                item {
                    Text("All upcoming events",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                }
                val now = System.currentTimeMillis()
                val upcoming = allEvents.filter { it.dateTime >= now }.sortedBy { it.dateTime }
                if (upcoming.isEmpty()) {
                    item {
                        Text("No upcoming events.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp))
                    }
                } else {
                    items(upcoming, key = { "upcoming_${it.id}" }) { event ->
                        CalendarEventCard(
                            event    = event,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Month navigator row ────────────────────────────────────────────────────

@Composable
private fun MonthNavigator(month: YearMonth, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous month")
        }
        Text(
            text = month.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy")),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next month")
        }
    }
}

// ── Day-of-week header (Mon Tue Wed …) ────────────────────────────────────

@Composable
private fun DayOfWeekHeader() {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp)) {
        days.forEach { day ->
            Text(
                text      = day,
                modifier  = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style     = MaterialTheme.typography.labelSmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ── Calendar grid ──────────────────────────────────────────────────────────

@Composable
private fun CalendarGrid(
    month: YearMonth,
    markedDays: Set<LocalDate>,
    selectedDay: LocalDate?,
    onDayClick: (LocalDate) -> Unit
) {
    val firstDay  = month.atDay(1)
    // Monday = 1, shift so Monday is column 0
    val startOffset = (firstDay.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
    val daysInMonth = month.lengthOfMonth()
    val totalCells  = startOffset + daysInMonth
    // pad to complete the last week
    val gridSize    = if (totalCells % 7 == 0) totalCells else totalCells + (7 - totalCells % 7)

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier
            .fillMaxWidth()
            .height((gridSize / 7 * 52).dp)
            .padding(horizontal = 4.dp),
        userScrollEnabled = false
    ) {
        items(gridSize) { index ->
            val dayNumber = index - startOffset + 1
            if (dayNumber < 1 || dayNumber > daysInMonth) {
                Box(modifier = Modifier.aspectRatio(1f))
            } else {
                val date    = month.atDay(dayNumber)
                val isToday = date == LocalDate.now()
                val isSel   = date == selectedDay
                val hasEvt  = date in markedDays

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isSel   -> MaterialTheme.colorScheme.primary
                                isToday -> MaterialTheme.colorScheme.primaryContainer
                                else    -> Color.Transparent
                            }
                        )
                        .clickable { onDayClick(date) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text  = dayNumber.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isToday || isSel) FontWeight.Bold else FontWeight.Normal,
                            color = when {
                                isSel   -> MaterialTheme.colorScheme.onPrimary
                                isToday -> MaterialTheme.colorScheme.primary
                                else    -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                        if (hasEvt) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSel) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.tertiary
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Event card in calendar ─────────────────────────────────────────────────

@Composable
fun CalendarEventCard(event: Event, modifier: Modifier = Modifier) {
    val fmt     = SimpleDateFormat("HH:mm", Locale.getDefault())
    val context = LocalContext.current

    Card(
        modifier  = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image: user photo > category emoji fallback
            EventThumbnail(event = event, modifier = Modifier.size(64.dp))

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(event.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold)

                Spacer(Modifier.height(2.dp))

                Text(fmt.format(Date(event.dateTime)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)

                if (event.location.isNotBlank()) {
                    Text("📍 ${event.location}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Source badge
                Text(
                    text = when (event.source) {
                        EventSource.TICKETMASTER -> "🎟 Ticketmaster"
                        EventSource.USER         -> "✍️ Manual"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Event thumbnail — image fallback chain ─────────────────────────────────
// Priority: user photo → category emoji → generic placeholder

@Composable
fun EventThumbnail(event: Event, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (!event.imageUri.isNullOrBlank()) {
            // 1. User-uploaded photo
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(Uri.parse(event.imageUri))
                    .crossfade(true)
                    .build(),
                contentDescription = "Event photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
            )
        } else {
            // 2. Category emoji fallback
            val emoji = categoryEmoji(event.name + " " + event.location)
            Text(emoji, fontSize = 28.sp, textAlign = TextAlign.Center)
        }
    }
}