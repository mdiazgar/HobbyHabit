package com.example.hobbyhabit.ui.screens

import android.content.Intent
import android.net.Uri
import android.location.Geocoder
import androidx.compose.foundation.clickable
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.hobbyhabit.BuildConfig
import com.example.hobbyhabit.data.remote.TicketmasterEvent
import com.example.hobbyhabit.ui.viewmodel.EventUiState
import com.example.hobbyhabit.ui.viewmodel.EventViewModel
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    hobbyName: String,
    category: String,
    viewModel: EventViewModel,
    onBack: () -> Unit
) {
    val context  = LocalContext.current
    val scope    = rememberCoroutineScope()
    val uiState  by viewModel.uiState.collectAsState()

    // ── Yasemin's register flow state ─────────────────────────────────
    val selectedEvent      by viewModel.selectedEvent.collectAsState()
    val showRegisterDialog by viewModel.showRegisterDialog.collectAsState()
    val showHobbyPicker    by viewModel.showHobbyPicker.collectAsState()
    val hobbies            by viewModel.hobbies.collectAsState()

    // ── Location state ─────────────────────────────────────────────────
    var locationStatus     by remember { mutableStateOf("Detecting location...") }
    var hasLocation        by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var cityInput          by remember { mutableStateOf("") }
    var cityError          by remember { mutableStateOf("") }

    fun searchWithCoords(lat: Double?, lng: Double?, statusText: String, located: Boolean) {
        hasLocation    = located
        locationStatus = statusText
        viewModel.searchEvents(BuildConfig.TICKETMASTER_TOKEN, category, lat, lng)
    }

    fun fetchGpsLocation() {
        val client = LocationServices.getFusedLocationProviderClient(context)
        client.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                searchWithCoords(loc.latitude, loc.longitude, "Showing events near you", true)
            } else {
                hasLocation    = false
                locationStatus = "Location unavailable — tap to set city"
                viewModel.searchEvents(BuildConfig.TICKETMASTER_TOKEN, category, null, null)
            }
        }.addOnFailureListener {
            hasLocation    = false
            locationStatus = "Location unavailable — tap to set city"
            viewModel.searchEvents(BuildConfig.TICKETMASTER_TOKEN, category, null, null)
        }
    }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) fetchGpsLocation()
        else {
            hasLocation    = false
            locationStatus = "Location denied — tap to set city"
            viewModel.searchEvents(BuildConfig.TICKETMASTER_TOKEN, category, null, null)
        }
    }

    LaunchedEffect(Unit) {
        if (uiState == EventUiState.Idle) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            if (hasPermission) fetchGpsLocation()
            else locationLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$hobbyName Events") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Tappable location banner
            Surface(
                color    = if (hasLocation) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth().clickable { showLocationDialog = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = if (hasLocation) Icons.Default.LocationOn
                            else Icons.Default.LocationOff,
                            contentDescription = null, modifier = Modifier.size(16.dp),
                            tint = if (hasLocation) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(locationStatus, style = MaterialTheme.typography.bodySmall,
                            color = if (hasLocation) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.Search, contentDescription = "Change location",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Events list
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is EventUiState.Idle, is EventUiState.Loading -> {
                        Column(modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            CircularProgressIndicator()
                            Text("Finding events near you...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    is EventUiState.Success -> {
                        if (state.events.isEmpty()) {
                            Column(modifier = Modifier.align(Alignment.Center).padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("No events found nearby.",
                                    style = MaterialTheme.typography.titleMedium)
                                OutlinedButton(onClick = { showLocationDialog = true }) {
                                    Text("Try a different city")
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(vertical = 16.dp)
                            ) {
                                items(state.events,
                                    key = { it.id ?: it.hashCode().toString() }) { event ->
                                    TicketmasterEventCard(
                                        event     = event,
                                        onClickUrl = {
                                            event.url?.let { url ->
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                context.startActivity(intent)
                                            }
                                        },
                                        onRegister = { viewModel.onEventClicked(event) }
                                    )
                                }
                            }
                        }
                    }
                    is EventUiState.Error -> {
                        Column(modifier = Modifier.fillMaxSize().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center) {
                            Text("Could not load events",
                                style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            Text(state.message, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = {
                                viewModel.searchEvents(BuildConfig.TICKETMASTER_TOKEN, category)
                            }) { Text("Retry") }
                        }
                    }
                }
            }
        }
    }

    // ── Step 1: Did you register? ──────────────────────────────────────
    if (showRegisterDialog && selectedEvent != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDialog() },
            title = { Text("Did you register?") },
            text  = { Text("Did you register for ${selectedEvent?.name}?") },
            confirmButton = {
                Button(onClick = { viewModel.confirmRegisterEvent() }) { Text("Yes") }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.dismissDialog() }) { Text("No") }
            }
        )
    }

    // ── Step 2: Pick which hobby to add it to ──────────────────────────
    if (showHobbyPicker) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissHobbyPicker() },
            title = { Text("Add to which hobby?") },
            text  = {
                if (hobbies.isEmpty()) {
                    Text("No hobbies found. Create one first.")
                } else {
                    LazyColumn {
                        items(hobbies) { hobby ->
                            ListItem(
                                headlineContent = { Text(hobby.name) },
                                supportingContent = { Text(hobby.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.registerTicketmasterEvent(hobby) }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissHobbyPicker() }) { Text("Cancel") }
            }
        )
    }

    // ── City picker dialog ─────────────────────────────────────────────
    if (showLocationDialog) {
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = { Text("Set location") },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Enter a city to find events near it, or use your GPS location.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = cityInput, onValueChange = { cityInput = it; cityError = "" },
                        label = { Text("City") },
                        placeholder = { Text("e.g. Boston, New York, Chicago") },
                        isError = cityError.isNotBlank(),
                        supportingText = if (cityError.isNotBlank()) {{ Text(cityError) }} else null,
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick  = {
                            showLocationDialog = false; cityInput = ""; cityError = ""
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.ACCESS_COARSE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPermission) fetchGpsLocation()
                            else locationLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                        }
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null,
                            modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Use my GPS location")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (cityInput.isBlank()) { cityError = "Please enter a city"; return@Button }
                    scope.launch {
                        try {
                            val geocoder = Geocoder(context)
                            val results  = withContext(Dispatchers.IO) {
                                @Suppress("DEPRECATION")
                                geocoder.getFromLocationName(cityInput, 1)
                            }
                            if (!results.isNullOrEmpty()) {
                                val loc = results[0]
                                showLocationDialog = false
                                searchWithCoords(loc.latitude, loc.longitude,
                                    "Showing events near ${cityInput.trim()}", true)
                                cityInput = ""
                            } else {
                                cityError = "City not found — try a different name"
                            }
                        } catch (e: Exception) {
                            cityError = "Could not find city — check your connection"
                        }
                    }
                }) { Text("Search") }
            },
            dismissButton = {
                TextButton(onClick = { showLocationDialog = false; cityError = "" }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ── Ticketmaster event card ─────────────────────────────────────────────────

@Composable
fun TicketmasterEventCard(
    event: TicketmasterEvent,
    onClickUrl: () -> Unit,
    onRegister: () -> Unit
) {
    val venue        = event.embedded?.venues?.firstOrNull()
    val venueName    = venue?.name
    val venueLocation = listOfNotNull(venue?.city?.name, venue?.state?.name).joinToString(", ")
    val date         = event.dates?.start?.localDate
    val time         = event.dates?.start?.localTime?.take(5)
    val dateString   = listOfNotNull(date, time).joinToString(" at ")

    Card(
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(event.name ?: "Unnamed Event",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            if (dateString.isNotBlank()) {
                Text("📅 $dateString", style = MaterialTheme.typography.bodySmall)
            }
            if (!venueName.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(4.dp))
                    Text(listOfNotNull(venueName, venueLocation.ifBlank { null }).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Open in browser
                OutlinedButton(
                    onClick  = onClickUrl,
                    modifier = Modifier.weight(1f)
                ) { Text("View") }
                // Register → triggers the "did you register?" flow
                Button(
                    onClick  = onRegister,
                    modifier = Modifier.weight(1f)
                ) { Text("I Registered ✓") }
            }
        }
    }
}