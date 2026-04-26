package com.example.hobbyhabit.data.remote

import com.google.gson.annotations.SerializedName

// Ticketmaster Discovery API response structure
data class TicketmasterResponse(
    @SerializedName("_embedded") val embedded: EmbeddedEvents?
)

data class EmbeddedEvents(
    val events: List<TicketmasterEvent>?
)

data class TicketmasterEvent(
    val id: String?,
    val name: String?,
    val url: String?,
    val dates: EventDates?,
    @SerializedName("_embedded") val embedded: EventEmbedded?
)

data class EventDates(
    val start: EventStart?
)

data class EventStart(
    val localDate: String?,    // "2026-05-10"
    val localTime: String?     // "19:00:00"
)

data class EventEmbedded(
    val venues: List<TicketmasterVenue>?
)

data class TicketmasterVenue(
    val name: String?,
    val city: VenueCity?,
    val state: VenueState?,
    val address: VenueAddress?
)

data class VenueCity(val name: String?)
data class VenueState(val name: String?)
data class VenueAddress(val line1: String?)