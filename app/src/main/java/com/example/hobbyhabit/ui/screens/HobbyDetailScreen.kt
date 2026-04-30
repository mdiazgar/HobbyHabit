package com.example.hobbyhabit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import com.example.hobbyhabit.data.local.Event
import com.example.hobbyhabit.data.local.EventSource
import com.example.hobbyhabit.data.local.Session
import com.example.hobbyhabit.ui.viewmodel.HobbyViewModel
import com.example.hobbyhabit.ui.theme.BlushPink
import com.example.hobbyhabit.ui.theme.CreamPeach
import com.example.hobbyhabit.ui.theme.WarmGray
import com.example.hobbyhabit.ui.theme.DustyRose
import com.example.hobbyhabit.ui.theme.SageGreen

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
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val events by viewModel.getEventsForHobby(hobbyId).collectAsState(initial = emptyList())
    val now = System.currentTimeMillis()
    val upcomingEvents = events.filter { it.dateTime > now }
    val pastEvents = events.filter { it.dateTime <= now }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background, // SoftCream
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        hobby?.name ?: "",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,    // DarkSlate
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer, // CreamPeach
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showDialog = true },
                icon = { Icon(Icons.Default.AddCircle, contentDescription = null) },
                text = { Text("Log Session") },
                containerColor = MaterialTheme.colorScheme.primary,   // DeepSage
                contentColor = MaterialTheme.colorScheme.onPrimary
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

            // Progress summary card
            item {
                hobby?.let { h ->
                    val progress = (weeklyCount.toFloat() / h.weeklyGoal).coerceIn(0f, 1f)
                    val isGoalReached = progress >= 1f

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            // flips to BlushPink when goal is hit
                            containerColor = if (isGoalReached) BlushPink else CreamPeach
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {

                            Text(
                                "Category: ${h.category}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant // SlateBlue
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "This Week",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface // DarkSlate
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "$weeklyCount / ${h.weeklyGoal} sessions",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface // DarkSlate
                            )
                            Spacer(Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (isGoalReached)
                                    MaterialTheme.colorScheme.tertiary  // BlushPink accent
                                else
                                    MaterialTheme.colorScheme.primary,  // DeepSage
                                trackColor = MaterialTheme.colorScheme.surfaceVariant // WarmGray
                            )
                            if (isGoalReached) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Goal reached! Now find an event. 🎉",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            // Session history header
            item {
                Text(
                    "Session History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground // DarkSlate
                )
            }

            if (sessions.isEmpty() && upcomingEvents.isEmpty() && pastEvents.isEmpty()) {
                item {
                    Text(
                        "No activity yet — log a session or register for an event!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant // SlateBlue
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
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
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

            // Upcoming events header
            item {
                Text(
                    "Upcoming Events",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (upcomingEvents.isEmpty()) {
                item {
                    Text(
                        "No upcoming events",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                        timestamp = dateTime?.atZone(java.time.ZoneId.systemDefault())
                            ?.toInstant()?.toEpochMilli()
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = CreamPeach // warm cream for session cards
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (session.notes.isNotBlank()) session.notes else "No notes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface, // DarkSlate
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${session.durationMinutes} min",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary // DeepSage
                )
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant // SlateBlue
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = { expanded = false; onEdit(session) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = { expanded = false; onDelete(session) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = session.timestamp?.let { fmt.format(Date(it)) } ?: "No date",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant // SlateBlue
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
        containerColor = CreamPeach,   // warm cream dialog background
        titleContentColor = MaterialTheme.colorScheme.onSurface,   // DarkSlate
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = { Text(if (session == null) "Log Session" else "Edit Session",
            fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Name") },
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
                            Toast.makeText(context, "Please enter notes first",
                                Toast.LENGTH_SHORT).show()
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
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary, // DeepSage
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
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
                Text(
                    if (session == null) "Log" else "Save",
                    color = MaterialTheme.colorScheme.primary, // DeepSage
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Cancel",
                    color = MaterialTheme.colorScheme.onSurfaceVariant // SlateBlue
                )
            }
        }
    )
}

// Event card
@Composable
fun EventItem(
    event: Event,
    onDelete: (Event) -> Unit
) {
    // Ticketmaster events get DustyRose, manual events get WarmGray
    val cardColor = when (event.source) {
        EventSource.TICKETMASTER -> DustyRose
        EventSource.USER -> WarmGray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface, // DarkSlate
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onDelete(event) }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant // SlateBlue
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            val fmt = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault())
            Text(
                text = fmt.format(Date(event.dateTime)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant // SlateBlue
            )

            Spacer(Modifier.height(4.dp))

            // Source badge
            Surface(
                color = when (event.source) {
                    EventSource.TICKETMASTER -> MaterialTheme.colorScheme.tertiary
                    EventSource.USER -> SageGreen
                },
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = when (event.source) {
                        EventSource.TICKETMASTER -> "Ticketmaster"
                        EventSource.USER -> "Manual"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}