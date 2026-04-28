package com.example.hobbyhabit.ui.screens

import androidx.compose.foundation.clickable
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(hobbyName: String, viewModel: EventViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val selectedEvent by viewModel.selectedEvent.collectAsState()
    val showDialog by viewModel.showRegisterDialog.collectAsState()
    fun fetchWithLocation() {
        val client = LocationServices.getFusedLocationProviderClient(context)
        client.lastLocation.addOnSuccessListener { loc ->
            viewModel.searchEvents(BuildConfig.TICKETMASTER_TOKEN, hobbyName,
                loc?.latitude, loc?.longitude)
        }.addOnFailureListener {
            viewModel.searchEvents(BuildConfig.TICKETMASTER_TOKEN, hobbyName)
        }
    }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) fetchWithLocation()
        else viewModel.searchEvents(BuildConfig.TICKETMASTER_TOKEN, hobbyName)
    }
    val showPicker by viewModel.showHobbyPicker.collectAsState()

    LaunchedEffect(Unit) {
        if (uiState == EventUiState.Idle) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            if (hasPermission) fetchWithLocation()
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
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is EventUiState.Idle,
                is EventUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is EventUiState.Success -> {
                    if (state.events.isEmpty()) {
                        Text("No $hobbyName events found nearby.",
                            modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            items(state.events, key = { it.id ?: it.hashCode().toString() }) { event ->
                                EventCard(event, viewModel)
                            }
                        }
                    }
                }
                is EventUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Could not load events",
                            style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(state.message, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = {
                            viewModel.searchEvents(BuildConfig.TICKETMASTER_TOKEN, hobbyName)
                        }) { Text("Retry") }
                    }
                }
            }
            if (showDialog && selectedEvent != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissDialog() },
                    title = { Text("Register Event?") },
                    text = {
                        Text("Did you register for ${selectedEvent?.name}?")
                    },
                    confirmButton = {
                        Button(onClick = {
                            viewModel.confirmRegisterEvent()
                        }) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        Button(onClick = {
                            viewModel.dismissDialog()
                        }) {
                            Text("No")
                        }
                    }
                )
            }
            if (showPicker) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissHobbyPicker() },
                    title = { Text("Choose Hobby") },
                    text = {
                        val hobbies by viewModel.hobbies.collectAsState()

                        if (hobbies.isEmpty()) {
                            Text("No hobbies found. Create one first.")
                        } else {
                            LazyColumn {
                                items(hobbies) { hobby ->
                                    Text(
                                        text = hobby.name,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.saveEventToHobby(hobby)
                                            }
                                            .padding(12.dp)
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = { viewModel.dismissHobbyPicker() }) {
                            Text("Close")
                        }
                    }
                )
            }

        }
    }
}

@Composable
fun EventCard(event: TicketmasterEvent, viewModel: EventViewModel) {
    val venue = event.embedded?.venues?.firstOrNull()
    val venueName = venue?.name
    val venueLocation = listOfNotNull(venue?.city?.name, venue?.state?.name).joinToString(", ")
    val date = event.dates?.start?.localDate
    val time = event.dates?.start?.localTime?.take(5)
    val dateString = listOfNotNull(date, time).joinToString(" at ")
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val url = event.url

                if (url != null) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                }

                // 2. THEN SHOW DIALOG
                viewModel.onEventClicked(event)            }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(event.name ?: "Unnamed Event")
            Spacer(Modifier.height(6.dp))

            if (dateString.isNotBlank()) {
                Text("📅 $dateString")
            }

            if (!venueName.isNullOrBlank()) {
                Text("$venueName · $venueLocation")
            }
        }
    }
}