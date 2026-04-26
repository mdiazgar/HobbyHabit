package com.example.hobbyhabit.ui.screens

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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.hobbyhabit.BuildConfig
import com.example.hobbyhabit.data.local.Event
import com.example.hobbyhabit.ui.viewmodel.EventUiState
import com.example.hobbyhabit.ui.viewmodel.EventViewModel
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    hobbyName: String?,
    viewModel: EventViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val query = hobbyName?.takeIf { it.isNotBlank() } ?: "events"

    fun fetchWithLocation() {
        val client = LocationServices.getFusedLocationProviderClient(context)

        client.lastLocation.addOnSuccessListener { loc ->
            viewModel.searchEvents(
                BuildConfig.EVENTBRITE_TOKEN,
                query,
                loc?.latitude,
                loc?.longitude
            )
        }.addOnFailureListener {
            viewModel.searchEvents(
                BuildConfig.EVENTBRITE_TOKEN,
                query,
                null,
                null
            )
        }
    }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) fetchWithLocation()
        else viewModel.searchEvents(BuildConfig.EVENTBRITE_TOKEN, query, null, null)
    }

    LaunchedEffect(Unit) {
        if (uiState == EventUiState.Idle) {

            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) fetchWithLocation()
            else locationLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${hobbyName ?: "All"} Events") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            when (val state = uiState) {

                is EventUiState.Idle,
                is EventUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is EventUiState.Success -> {

                    if (state.events.isEmpty()) {
                        Text(
                            "No events found nearby.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            items(
                                state.events,
                                key = { it.id }
                            ) { event ->
                                EventCard(event)
                            }
                        }
                    }
                }

                is EventUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Could not load events")
                        Spacer(Modifier.height(8.dp))
                        Text(state.message)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = {
                            viewModel.searchEvents(
                                BuildConfig.EVENTBRITE_TOKEN,
                                query,
                                null,
                                null
                            )
                        }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventCard(event: Event) {

    val fmt = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault())

    val dateText = if (event.dateTime > 0) {
        fmt.format(Date(event.dateTime))
    } else {
        "Date not available"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = event.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Date: $dateText",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.width(4.dp))

                Text(
                    text = event.location,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}