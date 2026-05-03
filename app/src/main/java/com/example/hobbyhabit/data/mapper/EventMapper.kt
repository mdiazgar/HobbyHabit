package com.example.hobbyhabit.data.mapper

import com.example.hobbyhabit.data.local.Event
import com.example.hobbyhabit.data.local.EventSource
import com.example.hobbyhabit.data.remote.TicketmasterEvent
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun TicketmasterEvent.toEvent(hobbyId: Int): Event {
    val venue    = this.embedded?.venues?.firstOrNull()
    val location = listOfNotNull(venue?.name, venue?.city?.name, venue?.state?.name)
        .joinToString(", ")

    // Convert "2026-05-17" + "19:00:00" to epoch millis
    val dateTimeMillis = try {
        val dateStr = this.dates?.start?.localDate ?: ""
        val timeStr = this.dates?.start?.localTime?.take(8) ?: "00:00:00"
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        fmt.timeZone = TimeZone.getDefault()
        fmt.parse("$dateStr $timeStr")?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }

    return Event(
        hobbyId         = hobbyId,
        name            = this.name ?: "Unnamed Event",
        location        = location,
        dateTime        = dateTimeMillis,
        url             = this.url,
        source          = EventSource.TICKETMASTER,
        isCustomCategory = false
    )
}