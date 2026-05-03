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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hobbyhabit.ui.viewmodel.HobbyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    hobbyId: Int,
    viewModel: HobbyViewModel,
    onBack: () -> Unit
) {
    val hobby    by viewModel.getHobbyById(hobbyId).collectAsState(initial = null)
    val sessions by viewModel.getSessionsForHobby(hobbyId).collectAsState(initial = emptyList())
    val events   by viewModel.getEventsForHobby(hobbyId).collectAsState(initial = emptyList())
    val context  = LocalContext.current

    val totalMins  = viewModel.totalMinutes(sessions)
    val avgMins    = viewModel.avgMinutes(sessions)
    val streak     = viewModel.currentStreak(sessions, events, hobby?.weeklyGoal ?: 1)
    val bestStreak = viewModel.bestStreak(sessions, events, hobby?.weeklyGoal ?: 1)

    var showShareCard by remember { mutableStateOf(false) }

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
            // Streak card — centred
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🔥", fontSize = 40.sp)

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = "$streak week streak",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Text(
                            text = "Best: $bestStreak weeks",
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

            item {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(Modifier.weight(1f), "Total Sessions", "${sessions.size}", "📋")
                    StatCard(Modifier.weight(1f), "Events Attended", "${events.size}", "🎟️")
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(Modifier.weight(1f), "Total Time", formatMinutes(totalMins), "⏱️")
                    StatCard(Modifier.weight(1f), "Avg Session", formatMinutes(avgMins), "📊")
                }
            }

            // Share card
            item {
                hobby?.let { h ->
                    ShareProgressCard(
                        hobby        = h,
                        sessions     = sessions,
                        weeklyCount  = sessions.size, // simplified for stats screen
                        streak       = streak,
                        onShare      = {
                            shareTextProgress(
                                context        = context,
                                hobbyName      = h.name,
                                streak         = streak,
                                weeklyCount    = sessions.size,
                                weeklyGoal     = h.weeklyGoal,
                                totalSessions  = sessions.size
                            )
                        }
                    )
                }
            }

            // Monthly breakdown
            item {
                Spacer(Modifier.height(4.dp))
                Text("Monthly Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
            }

            val monthlyData = getMonthlyBreakdown(sessions)
            if (monthlyData.isEmpty()) {
                item {
                    Text("No sessions yet.",
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
                                Text("$count sessions",
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
    mins < 60 -> "${mins}m"
    else      -> "${mins / 60}h ${mins % 60}m"
}

private data class MonthStat(val label: String, val count: Int, val totalMins: Int)

private fun getMonthlyBreakdown(sessions: List<com.example.hobbyhabit.data.local.Session>): List<MonthStat> {
    val zoneId  = java.time.ZoneId.systemDefault()
    val grouped = sessions.groupBy { session ->
        val date = java.time.Instant.ofEpochMilli(session.timestamp)
            .atZone(zoneId).toLocalDate()
        java.time.YearMonth.of(date.year, date.monthValue)
    }
    return grouped.entries
        .sortedByDescending { it.key }
        .take(6)
        .map { (month, s) ->
            MonthStat(
                label     = month.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy")),
                count     = s.size,
                totalMins = s.sumOf { it.durationMinutes }
            )
        }
}