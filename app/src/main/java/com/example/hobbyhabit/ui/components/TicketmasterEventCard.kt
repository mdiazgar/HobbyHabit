package com.example.hobbyhabit.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.hobbyhabit.data.remote.TicketmasterEvent
import com.example.hobbyhabit.ui.viewmodel.EventViewModel

@Composable
fun TicketmasterEventCard(
    event: TicketmasterEvent,
    viewModel: EventViewModel
) {
    val context = LocalContext.current

    val venue = event.embedded?.venues?.firstOrNull()
    val venueName = venue?.name
    val venueLocation = listOfNotNull(
        venue?.city?.name,
        venue?.state?.name
    ).joinToString(", ")

    val date = event.dates?.start?.localDate
    val time = event.dates?.start?.localTime?.take(5)
    val dateString = listOfNotNull(date, time).joinToString(" at ")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                event.url?.let { url ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                }
                viewModel.onEventClicked(event)
            }
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

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { viewModel.onEventClicked(event) }
            ) {
                Text("Save to Hobby")
            }
        }
    }
}