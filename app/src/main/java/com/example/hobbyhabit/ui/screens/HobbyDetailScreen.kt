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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.hobbyhabit.data.local.Session
import com.example.hobbyhabit.ui.viewmodel.HobbyViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.example.hobbyhabit.data.local.Event
import com.example.hobbyhabit.data.local.EventSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HobbyDetailScreen(
    hobbyId: Int,
    viewModel: HobbyViewModel,
    onBack: () -> Unit,
    onFindEvents: (String, String) -> Unit   // hobbyName, category
) {

    val hobby by viewModel.getHobbyById(hobbyId).collectAsState(initial = null)
    val sessions by viewModel.getSessionsForHobby(hobbyId).collectAsState(initial = emptyList())
    val weeklyCount by viewModel.getTotalWeeklyActivity(hobbyId)
        .collectAsState(initial = 0)
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val events by viewModel.getEventsForHobby(hobbyId)
        .collectAsState(initial = emptyList())

    val now = System.currentTimeMillis()

    val upcomingEvents = events.filter { it.dateTime > now }
    val pastEvents = events.filter { it.dateTime <= now }
    fun handleDelete(session: Session) {
        viewModel.deleteSession(session) // call ViewModel function
        Toast.makeText(context, "Session deleted", Toast.LENGTH_SHORT).show()
    }

    fun handleEdit(session: Session) {
        viewModel.startEditingSession(session) // you can open your edit dialog
    }
    //Top navigation of page
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(hobby?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        hobby?.let { onFindEvents(it.name, it.category) }
                    }) {
                        Icon(Icons.Default.Event, contentDescription = "Find Events")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showDialog = true },
                icon = { Icon(Icons.Default.AddCircle, contentDescription = null) },
                text = { Text("Log Session") }
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
            //progress card
            item {
                hobby?.let { h ->
                    val progress = (weeklyCount.toFloat() / h.weeklyGoal).coerceIn(0f, 1f)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        //Top green card of progress
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Category: ${h.category}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(Modifier.height(4.dp))
                            Text("This Week", style = MaterialTheme.typography.labelLarge)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "$weeklyCount / ${h.weeklyGoal} sessions",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth(),
                                color = if (progress >= 1f) MaterialTheme.colorScheme.tertiary
                                else MaterialTheme.colorScheme.primary
                            )
                            if (progress >= 1f) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Goal reached! Now find an event.",
                                    color = MaterialTheme.colorScheme.tertiary,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
            //session section
            item {
                Text(
                    "Session History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            //Session listings
            if (sessions.isEmpty() && upcomingEvents.isEmpty() && pastEvents.isEmpty()) {
                item {
                    Text(
                        "No activity yet — log a session or register for an event!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(sessions, key = { "session_${it.id}" }) { session ->
                    SessionItem(
                        session = session,
                        onDelete = { viewModel.deleteSession(it) },
                        onEdit = {
                            viewModel.startEditingSession(it)
                            showDialog = true
                        }
                    )
                }
                if (pastEvents.isNotEmpty()) {
                    item {
                        Text(
                            "Past Events",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    items(pastEvents, key = { "past_event_${it.id}" }) { event ->
                        EventItem(
                            event = event,
                            onDelete = { viewModel.deleteEvent(it) }
                        )
                    }
                }

            }
            //  EVENTS SECTION
            item {
                Text(
                    "Upcoming Events",
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (upcomingEvents.isEmpty()) {
                item {
                    Text("No upcoming events")
                }
            } else {
                items(upcomingEvents, key = { "upcoming_event_${it.id}" }) { event ->
                    EventItem(
                        event = event,
                        onDelete = { viewModel.deleteEvent(it) }
                    )
                }
            }
        }
    }
    val editingSession by viewModel.editingSession

    if (showDialog) {
        LogSessionDialog(
            session = editingSession,
            onDismiss = {
                showDialog = false
                viewModel.startEditingSession(null)
            },
            onConfirm = { duration, notes, dateTime ->
                if (editingSession != null) {
                    val updated = editingSession!!.copy(
                        durationMinutes = duration,
                        notes = notes,
                        timestamp = dateTime?.atZone(java.time.ZoneId.systemDefault())?.toInstant()
                            ?.toEpochMilli()
                            ?: System.currentTimeMillis()
                    )
                    viewModel.updateSession(updated)
                } else {
                    viewModel.logSession(hobbyId, duration, notes)
                }
                showDialog = false
                viewModel.startEditingSession(null)
            }
        )
    }
}

// Session card
@Composable
fun SessionItem(
    session: Session,
    onDelete: (Session) -> Unit,
    onEdit: (Session) -> Unit
) {
    val fmt = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault())
    var expanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {

            // TOP ROW: Notes + duration + menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = if (session.notes.isNotBlank()) session.notes else "No notes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${session.durationMinutes} min",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                expanded = false
                                onEdit(session)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                expanded = false
                                onDelete(session)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // DATE (consistent with events)
            Text(
                text = session.timestamp?.let { fmt.format(Date(it)) } ?: "No date",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Log Session dialog
@Composable
fun LogSessionDialog(
    session: Session? = null,
    onDismiss: () -> Unit,
    onConfirm: (Int, String, LocalDateTime?) -> Unit
) {
    var duration by remember { mutableStateOf(session?.durationMinutes?.toString() ?: "60") }
    var notes by remember { mutableStateOf(session?.notes ?: "") }
    var selectedDateTime by remember {
        mutableStateOf(
            session?.timestamp?.let {
                LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(it),
                    java.time.ZoneId.systemDefault()
                )
            }
        )
    }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (session == null) "Log Session" else "Edit Session") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    isError = notes.isBlank()
                )
                OutlinedTextField(
                    value = duration,
                    onValueChange = { if (it.all(Char::isDigit)) duration = it },
                    label = { Text("Duration (minutes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Button(
                    onClick = {
                        if (notes.isBlank()) {
                            Toast.makeText(context, "Please enter notes first", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val now = LocalDate.now()
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                val pickedDate = LocalDate.of(year, month + 1, day)
                                TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        selectedDateTime = pickedDate.atTime(hour, minute)
                                    },
                                    12, 0, true
                                ).show()
                            },
                            now.year, now.monthValue - 1, now.dayOfMonth
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
                    if (notes.isBlank()) {
                        Toast.makeText(context, "Notes are required", Toast.LENGTH_SHORT).show()
                        return@TextButton
                    }
                    onConfirm(duration.toIntOrNull() ?: 30, notes, selectedDateTime)
                }
            ) {
                Text(if (session == null) "Log" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EventItem(
    event: Event,
    onDelete: (Event) -> Unit
) {

    Card(modifier = Modifier.fillMaxWidth()) {

        Column(modifier = Modifier.padding(14.dp)) {

            // TOP ROW: TITLE + DELETE
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = event.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = { onDelete(event) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }

            Spacer(Modifier.height(6.dp))

            val fmt = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault())

            Text(
                text = fmt.format(Date(event.dateTime)),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(4.dp))

            // SOURCE LABEL (small but consistent)
            Text(
                text = when (event.source) {
                    EventSource.TICKETMASTER -> "Ticketmaster Event"
                    EventSource.USER -> "Manual Event"
                },
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}