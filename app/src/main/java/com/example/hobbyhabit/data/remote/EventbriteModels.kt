package com.example.hobbyhabit.data.remote

data class EventbriteResponse(
    val events: List<EventbriteEvent>?
)

data class EventbriteEvent(
    val id: String?,
    val name: EventName?,
    val start: EventTime?,
    val venue: Venue?,
    val url: String?
)

data class EventName(val text: String?)
data class EventTime(val local: String?)
data class Venue(val name: String?, val address: VenueAddress?)
data class VenueAddress(val localized_address_display: String?)
