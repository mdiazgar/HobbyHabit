package com.example.hobbyhabit.data.repository

import com.example.hobbyhabit.data.remote.TicketmasterApi
import com.example.hobbyhabit.data.remote.TicketmasterEvent

class EventRepository(private val api: TicketmasterApi) {

    private fun getMockEvents(category: String): List<TicketmasterEvent> {
        return listOf(
            TicketmasterEvent(id = "1", name = "$category Event — This Weekend",
                url = "https://www.ticketmaster.com", dates = null, embedded = null),
            TicketmasterEvent(id = "2", name = "Local $category Show",
                url = "https://www.ticketmaster.com", dates = null, embedded = null),
            TicketmasterEvent(id = "3", name = "$category Festival Near You",
                url = "https://www.ticketmaster.com", dates = null, embedded = null)
        )
    }

    suspend fun searchEvents(
        apiKey: String,
        category: String,       // Ticketmaster classificationName
        lat: Double?,
        lng: Double?
    ): Result<List<TicketmasterEvent>> {
        return try {
            val latlong = if (lat != null && lng != null) "$lat,$lng" else null
            val response = api.searchEvents(
                apiKey = apiKey,
                classificationName = category,
                latlong = latlong
            )
            if (response.isSuccessful) {
                val events = response.body()?.embedded?.events ?: emptyList()
                if (events.isEmpty()) Result.success(getMockEvents(category))
                else Result.success(events)
            } else {
                Result.success(getMockEvents(category))
            }
        } catch (e: Exception) {
            Result.success(getMockEvents(category))
        }
    }
}
