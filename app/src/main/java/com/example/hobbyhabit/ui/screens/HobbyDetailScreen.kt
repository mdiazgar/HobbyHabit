package com.example.hobbyhabit.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import java.time.Instant
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
    val hobby          by viewModel.getHobbyById(hobbyId).collectAsState(initial = null)
    val sessions       by viewModel.getSessionsForHobby(hobbyId).collectAsState(initial = emptyList())
    val weeklyCount    by viewModel.getTotalWeeklyActivity(hobbyId).collectAsState(initial = 0)
    val events         by viewModel.getEventsForHobby(hobbyId).collectAsState(initial = emptyList())
    val editingSession by viewModel.editingSession

    // FAB now opens a combined dialog: log session OR add event
    var showLogDialog  by rememberSaveable { mutableStateOf(false) }
    var showAddMode    by rememberSaveable { mutableStateOf(true) } // true = session, false = event
    var eventToEdit    by remember { mutableStateOf<Event?>(null) }

    val context = LocalContext.current

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
                    IconButton(onClick = { hobby?.let { onFindEvents(it.name, it.category) } }) {
                        Icon(Icons.Default.Event, contentDescription = "Find Events")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.startEditingSession(null)
                    showAddMode = true
                    showLogDialog = true
                },
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
            // Progress card
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
                            Text("Category: ${h.category}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(Modifier.height(4.dp))
                            Text("This Week", style = MaterialTheme.typography.labelLarge)
                            Spacer(Modifier.height(4.dp))
                            Text("$weeklyCount / ${h.weeklyGoal} sessions",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth(),
                                color = if (progress >= 1f) MaterialTheme.colorScheme.tertiary
                                else MaterialTheme.colorScheme.primary
                            )
                            if (progress >= 1f) {
                                Spacer(Modifier.height(6.dp))
                                Text("Goal reached! Now find or add an event.",
                                    color = MaterialTheme.colorScheme.tertiary,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            // Session History
            item {
                Text("Session History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
            }

            if (sessions.isEmpty()) {
                item {
                    Text("No sessions yet — tap Log Session to start!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(sessions, key = { "session_${it.id}" }) { session ->
                    SessionItem(
                        session  = session,
                        onDelete = {
                            viewModel.deleteSession(it)
                            Toast.makeText(context, "Session deleted", Toast.LENGTH_SHORT).show()
                        },
                        onEdit   = {
                            viewModel.startEditingSession(it)
                            showAddMode = true
                            showLogDialog = true
                        }
                    )
                }
            }

            // Events History (all events, past and upcoming together)
            item {
                Text("Events",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
            }

            if (events.isEmpty()) {
                item {
                    Text("No events yet — find or add one!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(events.sortedByDescending { it.dateTime },
                    key = { "event_${it.id}" }) { event ->
                    EventItem(
                        event    = event,
                        onDelete = {
                            viewModel.deleteEvent(it)
                            Toast.makeText(context, "Event deleted", Toast.LENGTH_SHORT).show()
                        },
                        onEdit   = { eventToEdit = it }
                    )
                }
            }
        }
    }

    // Combined Log Session / Add Event dialog
    if (showLogDialog) {
        val isEditing = editingSession != null
        LogOrAddDialog(
            editingSession = editingSession,
            onDismiss = {
                showLogDialog = false
                viewModel.startEditingSession(null)
            },
            onLogSession = { duration, notes, dateTime ->
                if (isEditing) {
                    viewModel.updateSession(
                        editingSession!!.copy(
                            durationMinutes = duration,
                            notes           = notes,
                            timestamp       = dateTime
                                ?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
                                ?: System.currentTimeMillis()
                        )
                    )
                } else {
                    viewModel.logSession(hobbyId, duration, notes)
                }
                showLogDialog = false
                viewModel.startEditingSession(null)
            },
            onAddEvent = { name, location, dateTimeMillis, durationMinutes, url ->
                viewModel.addManualEvent(
                    hobbyId         = hobbyId,
                    name            = name,
                    location        = location,
                    dateTime        = dateTimeMillis,
                    durationMinutes = durationMinutes,
                    url             = url
                )
                Toast.makeText(context, "Event added", Toast.LENGTH_SHORT).show()
                showLogDialog = false
            }
        )
    }

    // Edit event dialog
    eventToEdit?.let { ev ->
        EventFormDialog(
            title           = "Edit Event",
            initialName     = ev.name,
            initialLocation = ev.location,
            initialDateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(ev.dateTime), ZoneId.systemDefault()
            ),
            initialDuration = ev.durationMinutes?.toString() ?: "",
            initialUrl      = ev.url ?: "",
            confirmLabel    = "Save",
            onDismiss       = { eventToEdit = null },
            onConfirm       = { name, location, dateTimeMillis, durationMinutes, url ->
                viewModel.updateEvent(
                    ev.copy(
                        name            = name,
                        location        = location,
                        dateTime        = dateTimeMillis,
                        durationMinutes = durationMinutes,
                        url             = url
                    )
                )
                Toast.makeText(context, "Event updated", Toast.LENGTH_SHORT).show()
                eventToEdit = null
            }
        )
    }
}

// ── Log / Edit session dialog ───────────────────────────────────────────────

@Composable
fun LogOrAddDialog(
    editingSession: Session?,
    onDismiss: () -> Unit,
    onLogSession: (Int, String, LocalDateTime?) -> Unit,
    onAddEvent: (String, String, Long, Int?, String?) -> Unit  // kept for signature compatibility
) {
    val isEditing = editingSession != null
    var notes            by remember { mutableStateOf(editingSession?.notes ?: "") }
    var duration         by remember { mutableStateOf(editingSession?.durationMinutes?.toString() ?: "60") }
    var location         by remember { mutableStateOf("") }
    var url              by remember { mutableStateOf("") }
    var selectedDateTime by remember {
        mutableStateOf(
            editingSession?.timestamp?.let {
                LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
            }
        )
    }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Edit Session" else "Log Session") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = notes, onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(), maxLines = 3
                )
                OutlinedTextField(
                    value = duration,
                    onValueChange = { if (it.all(Char::isDigit)) duration = it },
                    label = { Text("Duration (minutes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                OutlinedTextField(
                    value = location, onValueChange = { location = it },
                    label = { Text("Location (optional)") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                OutlinedTextField(
                    value = url, onValueChange = { url = it },
                    label = { Text("URL (optional)") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                Button(
                    onClick = {
                        val now = LocalDate.now()
                        DatePickerDialog(context,
                            { _, year, month, day ->
                                val d = LocalDate.of(year, month + 1, day)
                                TimePickerDialog(context,
                                    { _, h, m -> selectedDateTime = d.atTime(h, m) },
                                    selectedDateTime?.hour ?: 12,
                                    selectedDateTime?.minute ?: 0, true).show()
                            },
                            selectedDateTime?.year ?: now.year,
                            (selectedDateTime?.monthValue ?: now.monthValue) - 1,
                            selectedDateTime?.dayOfMonth ?: now.dayOfMonth).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(selectedDateTime?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
                        ?: "Select Date & Time")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onLogSession(duration.toIntOrNull() ?: 30, notes, selectedDateTime)
            }) { Text(if (isEditing) "Save" else "Log") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ── Session card — dropdown ⋮ with Edit and Delete ──────────────────────────

@Composable
fun SessionItem(
    session: Session,
    onDelete: (Session) -> Unit,
    onEdit: (Session) -> Unit
) {
    val fmt      = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault())
    var expanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (session.notes.isNotBlank()) session.notes else "No notes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text("${session.durationMinutes} min",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium)
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(text = { Text("Edit") },
                            onClick = { expanded = false; onEdit(session) })
                        DropdownMenuItem(text = { Text("Delete") },
                            onClick = { expanded = false; onDelete(session) })
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(fmt.format(Date(session.timestamp)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ── Event card — dropdown ⋮ with Edit and Delete ────────────────────────────

@Composable
fun EventItem(
    event: Event,
    onDelete: (Event) -> Unit,
    onEdit: (Event) -> Unit
) {
    val fmt      = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault())
    var expanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(event.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text(fmt.format(Date(event.dateTime)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (event.location.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(event.location,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    event.durationMinutes?.let {
                        Spacer(Modifier.height(4.dp))
                        Text("$it minutes", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    event.url?.takeIf { it.isNotBlank() }?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(it, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = when (event.source) {
                            EventSource.TICKETMASTER -> "Ticketmaster Event"
                            EventSource.USER         -> "Manual Event"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(text = { Text("Edit") },
                            onClick = { expanded = false; onEdit(event) })
                        DropdownMenuItem(text = { Text("Delete") },
                            onClick = { expanded = false; onDelete(event) })
                    }
                }
            }
        }
    }
}

// ── Edit event dialog ───────────────────────────────────────────────────────

@Composable
fun EventFormDialog(
    title: String,
    initialName: String,
    initialLocation: String,
    initialDateTime: LocalDateTime?,
    initialDuration: String,
    initialUrl: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long, Int?, String?) -> Unit
) {
    var name             by remember { mutableStateOf(initialName) }
    var location         by remember { mutableStateOf(initialLocation) }
    var duration         by remember { mutableStateOf(initialDuration) }
    var url              by remember { mutableStateOf(initialUrl) }
    var selectedDateTime by remember { mutableStateOf(initialDateTime) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Event name") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true, isError = name.isBlank())
                OutlinedTextField(value = location, onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = duration,
                    onValueChange = { if (it.all(Char::isDigit)) duration = it },
                    label = { Text("Duration in minutes (optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = url, onValueChange = { url = it },
                    label = { Text("URL (optional)") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                Button(onClick = {
                    val today = LocalDate.now()
                    DatePickerDialog(context,
                        { _, year, month, day ->
                            val d = LocalDate.of(year, month + 1, day)
                            TimePickerDialog(context,
                                { _, h, m -> selectedDateTime = d.atTime(h, m) },
                                selectedDateTime?.hour ?: 12,
                                selectedDateTime?.minute ?: 0, true).show()
                        },
                        selectedDateTime?.year ?: today.year,
                        (selectedDateTime?.monthValue ?: today.monthValue) - 1,
                        selectedDateTime?.dayOfMonth ?: today.dayOfMonth).show()
                }, modifier = Modifier.fillMaxWidth()) {
                    Text(selectedDateTime?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
                        ?: "Select Date & Time")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isBlank()) {
                    Toast.makeText(context, "Event name is required", Toast.LENGTH_SHORT).show()
                    return@TextButton
                }
                val dt = selectedDateTime
                if (dt == null) {
                    Toast.makeText(context, "Please select date and time", Toast.LENGTH_SHORT).show()
                    return@TextButton
                }
                onConfirm(name.trim(), location.trim(),
                    dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    duration.toIntOrNull()?.takeIf { it > 0 },
                    url.trim().takeIf { it.isNotBlank() })
            }) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}