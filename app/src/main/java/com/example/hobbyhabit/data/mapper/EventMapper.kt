package com.example.hobbyhabit.data.mapper

import com.example.hobbyhabit.data.local.Event
import com.example.hobbyhabit.data.local.EventSource
import com.example.hobbyhabit.data.remote.TicketmasterEvent

fun TicketmasterEvent.toEvent(hobbyId: Int): Event {
    return Event(
        hobbyId = hobbyId,
        name = this.name ?: "",
        location = this.embedded?.venues?.firstOrNull()?.name ?: "",
        dateTime = this.dates?.start?.let {
            val date = it.localDate
            val time = it.localTime ?: "00:00:00"
            java.time.LocalDateTime.parse("${date}T$time")
                .atZone(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        } ?: System.currentTimeMillis(),
        durationMinutes = null,
        url = this.url,
        source = EventSource.TICKETMASTER
    )
}