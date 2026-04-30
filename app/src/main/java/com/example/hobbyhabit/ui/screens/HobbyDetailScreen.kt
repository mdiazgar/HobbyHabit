package com.example.hobbyhabit.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.hobbyhabit.data.local.Event
import com.example.hobbyhabit.data.local.EventSource
import com.example.hobbyhabit.data.local.Session
import com.example.hobbyhabit.ui.viewmodel.HobbyViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HobbyDetailScreen(
    hobbyId: Int,
    viewModel: HobbyViewModel,
    onBack: () -> Unit,
    onFindEvents: (String, String) -> Unit
) {
    val hobby by viewModel.getHobbyById(hobbyId).collectAsState(initial = null)
    val sessions by viewModel.getSessionsForHobby(hobbyId).collectAsState(initial = emptyList())
    val weeklyCount by viewModel.getTotalWeeklyActivity(hobbyId).collectAsState(initial = 0)
    val events by viewModel.getEventsForHobby(hobbyId).collectAsState(initial = emptyList())

    var showEventDialog by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val now = System.currentTimeMillis()

    val upcomingEvents = events
        .filter { it.dateTime > now }
        .sortedBy { it.dateTime }

    val pastEvents = events
        .filter { it.dateTime <= now }
        .sortedByDescending { it.dateTime }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(hobby?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            hobby?.let { onFindEvents(it.name, it.category) }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = "Find Events"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showEventDialog = true },
                icon = {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = null
                    )
                },
                text = { Text("Add Event") }
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
            item {
                hobby?.let { h ->
                    val safeGoal = h.weeklyGoal.coerceAtLeast(1)
                    val progress = (weeklyCount.toFloat() / safeGoal).coerceIn(0f, 1f)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Category: ${h.category}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "This Week",
                                style = MaterialTheme.typography.labelLarge
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "$weeklyCount / ${h.weeklyGoal} sessions",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth(),
                                color = if (progress >= 1f) {
                                    MaterialTheme.colorScheme.tertiary
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            )

                            if (progress >= 1f) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Goal reached! Now find or add an event.",
                                    color = MaterialTheme.colorScheme.tertiary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Session History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (sessions.isEmpty() && pastEvents.isEmpty()) {
                item {
                    Text(
                        text = "No sessions or past events yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(
                    items = sessions,
                    key = { session -> "session_${session.id}" }
                ) { session ->
                    SessionItem(
                        session = session,
                        onDelete = { selectedSession ->
                            viewModel.deleteSession(selectedSession)
                            Toast.makeText(context, "Session deleted", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                items(
                    items = pastEvents,
                    key = { event -> "past_event_${event.id}" }
                ) { event ->
                    EventItem(
                        event = event,
                        onDelete = { selectedEvent ->
                            viewModel.deleteEvent(selectedEvent)
                            Toast.makeText(context, "Event deleted", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            item {
                Text(
                    text = "Upcoming Events",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (upcomingEvents.isEmpty()) {
                item {
                    Text(
                        text = "No upcoming events.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(
                    items = upcomingEvents,
                    key = { event -> "upcoming_event_${event.id}" }
                ) { event ->
                    EventItem(
                        event = event,
                        onDelete = { selectedEvent ->
                            viewModel.deleteEvent(selectedEvent)
                            Toast.makeText(context, "Event deleted", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

        }
    }

    if (showEventDialog) {
        ManualEventDialog(
            onDismiss = { showEventDialog = false },
            onConfirm = { name: String,
                          location: String,
                          dateTimeMillis: Long,
                          durationMinutes: Int?,
                          url: String? ->
                viewModel.addManualEvent(
                    hobbyId = hobbyId,
                    name = name,
                    location = location,
                    dateTime = dateTimeMillis,
                    durationMinutes = durationMinutes,
                    url = url
                )

                Toast.makeText(context, "Event added", Toast.LENGTH_SHORT).show()
                showEventDialog = false
            }
        )
    }
}

@Composable
fun SessionItem(
    session: Session,
    onDelete: (Session) -> Unit
) {
    val fmt = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault())

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (session.notes.isNotBlank()) session.notes else "No notes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = fmt.format(Date(session.timestamp)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${session.durationMinutes} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = { onDelete(session) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete"
                    )
                }
            }
        }
    }
}

@Composable
fun ManualEventDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        location: String,
        dateTimeMillis: Long,
        durationMinutes: Int?,
        url: String?
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var selectedDateTime by remember { mutableStateOf<LocalDateTime?>(null) }

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Event") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Event name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = name.isBlank()
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = duration,
                    onValueChange = { newValue ->
                        if (newValue.all(Char::isDigit)) {
                            duration = newValue
                        }
                    },
                    label = { Text("Duration in minutes (optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Button(
                    onClick = {
                        val today = LocalDate.now()

                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                val pickedDate = LocalDate.of(year, month + 1, day)

                                TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        selectedDateTime = pickedDate.atTime(hour, minute)
                                    },
                                    12,
                                    0,
                                    true
                                ).show()
                            },
                            today.year,
                            today.monthValue - 1,
                            today.dayOfMonth
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        selectedDateTime?.format(
                            DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
                        ) ?: "Select Date & Time"
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank()) {
                        Toast.makeText(context, "Event name is required", Toast.LENGTH_SHORT).show()
                        return@TextButton
                    }

                    val dateTime = selectedDateTime
                    if (dateTime == null) {
                        Toast.makeText(context, "Please select date and time", Toast.LENGTH_SHORT).show()
                        return@TextButton
                    }

                    val durationMinutes = duration.toIntOrNull()?.takeIf { it > 0 }
                    val cleanUrl = url.trim().takeIf { it.isNotBlank() }

                    onConfirm(
                        name.trim(),
                        location.trim(),
                        dateTime.toEpochMillis(),
                        durationMinutes,
                        cleanUrl
                    )
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EventItem(
    event: Event,
    onDelete: (Event) -> Unit
) {
    val fmt = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault())

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = fmt.format(Date(event.dateTime)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (event.location.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = event.location,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    event.durationMinutes?.let { minutes ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$minutes minutes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    event.url?.takeIf { it.isNotBlank() }?.let { eventUrl ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = eventUrl,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = when (event.source) {
                            EventSource.TICKETMASTER -> "Ticketmaster Event"
                            EventSource.USER -> "Manual Event"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = { onDelete(event) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete"
                    )
                }
            }
        }
    }
}

private fun LocalDateTime.toEpochMillis(): Long {
    return this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}
