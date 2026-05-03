package com.example.hobbyhabit.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.BarChart
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.hobbyhabit.data.local.Event
import com.example.hobbyhabit.data.local.EventSource
import com.example.hobbyhabit.data.local.Session
import com.example.hobbyhabit.ui.viewmodel.HobbyViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

private sealed class HistoryItem {
    abstract val timestamp: Long
    data class LoggedSession(val session: Session) : HistoryItem() {
        override val timestamp: Long get() = session.timestamp
    }
    data class LoggedPastEvent(val event: Event) : HistoryItem() {
        override val timestamp: Long get() = event.dateTime
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HobbyDetailScreen(
    hobbyId: Int,
    viewModel: HobbyViewModel,
    onBack: () -> Unit,
    onFindEvents: (String, String) -> Unit,
    onViewStats: () -> Unit = {}
) {
    val hobby          by viewModel.getHobbyById(hobbyId).collectAsState(initial = null)
    val sessions       by viewModel.getSessionsForHobby(hobbyId).collectAsState(initial = emptyList())
    val weeklyCount    by viewModel.getTotalWeeklyActivity(hobbyId).collectAsState(initial = 0)
    val events         by viewModel.getEventsForHobby(hobbyId).collectAsState(initial = emptyList())
    val editingSession by viewModel.editingSession

    var showAddEventDialog    by rememberSaveable { mutableStateOf(false) }
    var showEditSessionDialog by rememberSaveable { mutableStateOf(false) }
    var eventToEdit           by remember { mutableStateOf<Event?>(null) }
    var currentTimeMillis     by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMillis = System.currentTimeMillis()
            delay(30_000L)
        }
    }

    val futureEvents = remember(events, currentTimeMillis) {
        events.filter { it.dateTime >= currentTimeMillis }.sortedBy { it.dateTime }
    }
    val pastEvents = remember(events, currentTimeMillis) {
        events.filter { it.dateTime < currentTimeMillis }.sortedByDescending { it.dateTime }
    }
    val historyItems = remember(sessions, pastEvents) {
        (sessions.map { HistoryItem.LoggedSession(it) } +
                pastEvents.map { HistoryItem.LoggedPastEvent(it) })
            .sortedByDescending { it.timestamp }
    }

    val context = LocalContext.current

    fun getCurrentWeekRange(): String {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysFromMonday = (dayOfWeek - Calendar.MONDAY + 7) % 7
        calendar.add(Calendar.DAY_OF_MONTH, -daysFromMonday)
        val monday = calendar.time
        calendar.add(Calendar.DAY_OF_MONTH, 6)
        val sunday = calendar.time
        val formatter = SimpleDateFormat("MMM d", Locale.getDefault())
        return "${formatter.format(monday)} – ${formatter.format(sunday)}"
    }

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
                    IconButton(onClick = onViewStats) {
                        Icon(Icons.Default.BarChart, contentDescription = "Stats")
                    }
                    IconButton(onClick = { hobby?.let { onFindEvents(it.name, it.category) } }) {
                        Icon(Icons.Default.Event, contentDescription = "Find Events")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    eventToEdit = null
                    showAddEventDialog = true
                },
                icon = { Icon(Icons.Default.AddCircle, contentDescription = null) },
                text = { Text("Log Event") }
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
                            Text(getCurrentWeekRange(), style = MaterialTheme.typography.labelLarge)
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
                                Text("Goal reached! Now find or log an event.",
                                    color = MaterialTheme.colorScheme.tertiary,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            item {
                Text("Session History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
            }

            if (historyItems.isEmpty()) {
                item {
                    Text("No sessions or past events yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(
                    items = historyItems,
                    key = { item ->
                        when (item) {
                            is HistoryItem.LoggedSession   -> "session_${item.session.id}"
                            is HistoryItem.LoggedPastEvent -> "past_event_${item.event.id}"
                        }
                    }
                ) { item ->
                    when (item) {
                        is HistoryItem.LoggedSession -> SessionItem(
                            session  = item.session,
                            onDelete = {
                                viewModel.deleteSession(it)
                                Toast.makeText(context, "Session deleted", Toast.LENGTH_SHORT).show()
                            },
                            onEdit   = {
                                viewModel.startEditingSession(it)
                                showEditSessionDialog = true
                            }
                        )
                        is HistoryItem.LoggedPastEvent -> EventItem(
                            event    = item.event,
                            onDelete = {
                                viewModel.deleteEvent(it)
                                Toast.makeText(context, "Event deleted", Toast.LENGTH_SHORT).show()
                            },
                            onEdit   = { eventToEdit = it }
                        )
                    }
                }
            }

            item {
                Text("Upcoming Events",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
            }

            if (futureEvents.isEmpty()) {
                item {
                    Text("No upcoming events.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(futureEvents, key = { "future_event_${it.id}" }) { event ->
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

    // Add event dialog — now includes image picker
    if (showAddEventDialog) {
        EventFormDialog(
            title           = "Log Event",
            initialName     = "",
            initialLocation = "",
            initialDateTime = null,
            initialDuration = "",
            initialUrl      = "",
            initialImageUri = null,
            confirmLabel    = "Save",
            onDismiss       = { showAddEventDialog = false },
            onConfirm       = { name, location, dateTimeMillis, durationMinutes, url, imageUri ->
                viewModel.addManualEvent(
                    hobbyId         = hobbyId,
                    name            = name,
                    location        = location,
                    dateTime        = dateTimeMillis,
                    durationMinutes = durationMinutes,
                    url             = url,
                    imageUri        = imageUri
                )
                Toast.makeText(
                    context,
                    if (dateTimeMillis >= System.currentTimeMillis()) "Upcoming event added"
                    else "Event added to Session History",
                    Toast.LENGTH_SHORT
                ).show()
                showAddEventDialog = false
            }
        )
    }

    // Edit session dialog
    if (showEditSessionDialog && editingSession != null) {
        SessionFormDialog(
            editingSession = editingSession!!,
            onDismiss = {
                showEditSessionDialog = false
                viewModel.startEditingSession(null)
            },
            onConfirm = { updatedSession ->
                viewModel.updateSession(updatedSession)
                Toast.makeText(context, "Session updated", Toast.LENGTH_SHORT).show()
                showEditSessionDialog = false
                viewModel.startEditingSession(null)
            }
        )
    }

    // Edit event dialog — now includes image picker
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
            initialImageUri = ev.imageUri,
            confirmLabel    = "Save",
            onDismiss       = { eventToEdit = null },
            onConfirm       = { name, location, dateTimeMillis, durationMinutes, url, imageUri ->
                viewModel.updateEvent(
                    ev.copy(
                        name            = name,
                        location        = location,
                        dateTime        = dateTimeMillis,
                        durationMinutes = durationMinutes,
                        url             = url,
                        imageUri        = imageUri
                    )
                )
                Toast.makeText(
                    context,
                    if (dateTimeMillis >= System.currentTimeMillis()) "Event moved to Upcoming Events"
                    else "Event moved to Session History",
                    Toast.LENGTH_SHORT
                ).show()
                eventToEdit = null
            }
        )
    }
}

// ── Session card ────────────────────────────────────────────────────────────

@Composable
fun SessionItem(session: Session, onDelete: (Session) -> Unit, onEdit: (Session) -> Unit) {
    val fmt      = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault())
    var expanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (session.notes.isNotBlank()) session.notes else "No notes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f))
                Text("${session.durationMinutes} min",
                    style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
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

// ── Event card with thumbnail ───────────────────────────────────────────────

@Composable
fun EventItem(event: Event, onDelete: (Event) -> Unit, onEdit: (Event) -> Unit) {
    val fmt      = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault())
    var expanded by remember { mutableStateOf(false) }
    val context  = LocalContext.current

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Thumbnail — shows user photo if available, else emoji
            if (!event.imageUri.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(Uri.parse(event.imageUri)).crossfade(true).build(),
                    contentDescription = "Event photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(Modifier.size(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(event.name, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(fmt.format(Date(event.dateTime)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (event.location.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(event.location, style = MaterialTheme.typography.bodyMedium,
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
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

// ── Event form dialog — Add or Edit, includes image picker ──────────────────

@Composable
fun EventFormDialog(
    title: String,
    initialName: String,
    initialLocation: String,
    initialDateTime: LocalDateTime?,
    initialDuration: String,
    initialUrl: String,
    initialImageUri: String? = null,       // ← new
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long, Int?, String?, String?) -> Unit  // ← imageUri added
) {
    var name             by remember { mutableStateOf(initialName) }
    var location         by remember { mutableStateOf(initialLocation) }
    var duration         by remember { mutableStateOf(initialDuration) }
    var url              by remember { mutableStateOf(initialUrl) }
    var selectedDateTime by remember { mutableStateOf(initialDateTime) }
    var imageUri         by remember { mutableStateOf(initialImageUri) }

    val context = LocalContext.current

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { imageUri = it.toString() } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Event name") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true, isError = name.isBlank())

                OutlinedTextField(value = location, onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)

                OutlinedTextField(
                    value = duration,
                    onValueChange = { if (it.all(Char::isDigit)) duration = it },
                    label = { Text("Duration in minutes (optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(), singleLine = true)

                OutlinedTextField(value = url, onValueChange = { url = it },
                    label = { Text("URL (optional)") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)

                // Date & Time picker
                Button(
                    onClick = {
                        val baseDate = selectedDateTime?.toLocalDate() ?: LocalDate.now()
                        DatePickerDialog(context,
                            { _, year, month, day ->
                                val selectedDate = LocalDate.of(year, month + 1, day)
                                TimePickerDialog(context,
                                    { _, hour, minute ->
                                        selectedDateTime = selectedDate.atTime(hour, minute)
                                    },
                                    selectedDateTime?.hour ?: 12,
                                    selectedDateTime?.minute ?: 0, true).show()
                            },
                            baseDate.year, baseDate.monthValue - 1, baseDate.dayOfMonth).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(selectedDateTime?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
                        ?: "Select Date & Time")
                }

                // ── Image picker ───────────────────────────────────────
                if (!imageUri.isNullOrBlank()) {
                    // Show thumbnail + remove option
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(Uri.parse(imageUri)).crossfade(true).build(),
                            contentDescription = "Event photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Column {
                            Text("Photo attached",
                                style = MaterialTheme.typography.bodySmall)
                            TextButton(onClick = { imageUri = null }) {
                                Text("Remove photo")
                            }
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { imagePicker.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text("Add photo (optional)")
                    }
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
                onConfirm(
                    name.trim(), location.trim(),
                    dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    duration.toIntOrNull()?.takeIf { it > 0 },
                    url.trim().takeIf { it.isNotBlank() },
                    imageUri   // ← passed through
                )
            }) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ── Session edit dialog ─────────────────────────────────────────────────────

@Composable
fun SessionFormDialog(
    editingSession: Session,
    onDismiss: () -> Unit,
    onConfirm: (Session) -> Unit
) {
    var notes            by remember { mutableStateOf(editingSession.notes) }
    var duration         by remember { mutableStateOf(editingSession.durationMinutes.toString()) }
    var selectedDateTime by remember {
        mutableStateOf(
            LocalDateTime.ofInstant(
                Instant.ofEpochMilli(editingSession.timestamp), ZoneId.systemDefault()
            )
        )
    }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Session") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = notes, onValueChange = { notes = it },
                    label = { Text("Notes") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
                OutlinedTextField(
                    value = duration,
                    onValueChange = { if (it.all(Char::isDigit)) duration = it },
                    label = { Text("Duration (minutes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                Button(
                    onClick = {
                        val baseDate = selectedDateTime.toLocalDate()
                        DatePickerDialog(context,
                            { _, year, month, day ->
                                val selectedDate = LocalDate.of(year, month + 1, day)
                                TimePickerDialog(context,
                                    { _, hour, minute ->
                                        selectedDateTime = selectedDate.atTime(hour, minute)
                                    },
                                    selectedDateTime.hour, selectedDateTime.minute, true).show()
                            },
                            baseDate.year, baseDate.monthValue - 1, baseDate.dayOfMonth).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(selectedDateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(editingSession.copy(
                    durationMinutes = duration.toIntOrNull()?.takeIf { it > 0 } ?: 30,
                    notes           = notes.trim(),
                    timestamp       = selectedDateTime.atZone(ZoneId.systemDefault())
                        .toInstant().toEpochMilli()
                ))
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}