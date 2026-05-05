package com.example.hobbyhabit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hobbyhabit.data.local.Event
import com.example.hobbyhabit.data.local.Session
import com.example.hobbyhabit.ui.viewmodel.HobbyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    hobbyId: Int,
    viewModel: HobbyViewModel,
    onBack: () -> Unit
) {
    val hobby        by viewModel.getHobbyById(hobbyId).collectAsState(initial = null)
    val sessions     by viewModel.getSessionsForHobby(hobbyId).collectAsState(initial = emptyList())
    val events       by viewModel.getEventsForHobby(hobbyId).collectAsState(initial = emptyList())
    // Use the same flow as HobbyDetailScreen so progress bar is always identical
    val weeklyCount  by viewModel.getTotalWeeklyActivity(hobbyId).collectAsState(initial = 0)
    val context      = LocalContext.current

    // ── Combine sessions + past events for all stats ──────────────────
    val now        = System.currentTimeMillis()
    val pastEvents = events.filter { it.dateTime <= now }  // only events that have happened

    // "Sessions Logged" = manual sessions + USER-sourced past events
    val userEvents     = pastEvents.filter { it.source == com.example.hobbyhabit.data.local.EventSource.USER }
    val tmEvents       = pastEvents.filter { it.source == com.example.hobbyhabit.data.local.EventSource.TICKETMASTER }

    val sessionsLogged = sessions.size + userEvents.size
    val eventsAttended = tmEvents.size

    // Total time = sessions + all past events
    val totalMins      = sessions.sumOf { it.durationMinutes } +
            pastEvents.sumOf { it.durationMinutes ?: 0 }

    val totalActivities = sessionsLogged + eventsAttended
    val avgMins         = if (totalActivities == 0) 0 else totalMins / totalActivities

    val streak     = viewModel.currentStreak(sessions, events, hobby?.weeklyGoal ?: 1)
    val bestStreak = viewModel.bestStreak(sessions, events, hobby?.weeklyGoal ?: 1)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${hobby?.name ?: ""} Stats") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Streak card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("🔥", fontSize = 40.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "$streak week streak",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "Best: $bestStreak weeks",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            item {
                Text("Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
            }

            // Stats grid
            item {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(Modifier.weight(1f), "Sessions Logged", "$sessionsLogged", "📋")
                    StatCard(Modifier.weight(1f), "Events Attended", "$eventsAttended", "🎟️")
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(Modifier.weight(1f), "Total Time", formatMinutes(totalMins), "⏱️")
                    StatCard(Modifier.weight(1f), "Avg Activity", formatMinutes(avgMins), "📊")
                }
            }

            // Share card
            item {
                hobby?.let { h ->
                    ShareProgressCard(
                        hobby           = h,
                        weeklyCount     = weeklyCount,
                        streak          = streak,
                        totalMins       = totalMins,
                        totalActivities = sessionsLogged,
                        onShare         = {
                            shareTextProgress(
                                context       = context,
                                hobbyName     = h.name,
                                streak        = streak,
                                weeklyCount   = weeklyCount,
                                weeklyGoal    = h.weeklyGoal,
                                totalSessions = sessionsLogged
                            )
                        }
                    )
                }
            }


            // Monthly breakdown — combines sessions + events
            item {
                Spacer(Modifier.height(4.dp))
                Text("Monthly Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
            }

            val monthlyData = getMonthlyBreakdown(sessions, pastEvents, userEvents)
            if (monthlyData.isEmpty()) {
                item {
                    Text("No activity yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(monthlyData.size) { idx ->
                    val (label, count, mins) = monthlyData[idx]
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium)
                            Column(horizontalAlignment = Alignment.End) {
                                Text("$count activities",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(formatMinutes(mins),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier, label: String, value: String, emoji: String) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, fontSize = 24.sp)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun formatMinutes(mins: Int): String = when {
    mins == 0  -> "—"
    mins < 60  -> "${mins}m"
    else       -> "${mins / 60}h ${mins % 60}m"
}

private data class MonthStat(val label: String, val count: Int, val totalMins: Int)

private fun getMonthlyBreakdown(
    sessions: List<Session>,
    pastEvents: List<Event>,
    userEvents: List<Event>  // USER-sourced events count as sessions
): List<MonthStat> {
    val zoneId = java.time.ZoneId.systemDefault()

    fun Long.toYearMonth(): java.time.YearMonth {
        val date = java.time.Instant.ofEpochMilli(this).atZone(zoneId).toLocalDate()
        return java.time.YearMonth.of(date.year, date.monthValue)
    }

    // Sessions + user events grouped by month (both count as "did the activity")
    val sessionsByMonth   = sessions.groupBy { it.timestamp.toYearMonth() }
    val userEventsByMonth = userEvents.groupBy { it.dateTime.toYearMonth() }
    // All past events for time totals
    val allEventsByMonth  = pastEvents.groupBy { it.dateTime.toYearMonth() }

    val allMonths = (sessionsByMonth.keys + userEventsByMonth.keys + allEventsByMonth.keys).toSet()

    return allMonths
        .sortedDescending()
        .take(6)
        .map { month ->
            val monthSessions   = sessionsByMonth[month] ?: emptyList()
            val monthUserEvents = userEventsByMonth[month] ?: emptyList()
            val monthAllEvents  = allEventsByMonth[month] ?: emptyList()
            MonthStat(
                label     = month.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy")),
                count     = monthSessions.size + monthUserEvents.size,
                totalMins = monthSessions.sumOf { it.durationMinutes } +
                        monthAllEvents.sumOf { it.durationMinutes ?: 0 }
            )
        }
}