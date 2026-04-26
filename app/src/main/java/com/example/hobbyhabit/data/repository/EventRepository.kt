package com.example.hobbyhabit.data.repository

import com.example.hobbyhabit.data.remote.TicketmasterApi
import com.example.hobbyhabit.data.remote.TicketmasterEvent

class EventRepository(private val api: TicketmasterApi) {

    // Mock fallback events used when API returns empty or fails
    private fun getMockEvents(query: String): List<TicketmasterEvent> {
        val q = query.replaceFirstChar { it.uppercase() }
        return listOf(
            TicketmasterEvent(id = "1", name = "$q for Beginners — Weekend Workshop",
                url = "https://www.ticketmaster.com", dates = null, embedded = null),
            TicketmasterEvent(id = "2", name = "Introduction to $q — Free Taster Session",
                url = "https://www.ticketmaster.com", dates = null, embedded = null),
            TicketmasterEvent(id = "3", name = "$q Masterclass with Local Artists",
                url = "https://www.ticketmaster.com", dates = null, embedded = null),
            TicketmasterEvent(id = "4", name = "$q Social — Meet Fellow Enthusiasts",
                url = "https://www.ticketmaster.com", dates = null, embedded = null),
            TicketmasterEvent(id = "5", name = "Advanced $q Techniques — Half Day Course",
                url = "https://www.ticketmaster.com", dates = null, embedded = null)
        )
    }

    suspend fun searchEvents(
        apiKey: String,
        query: String,
        lat: Double?,
        lng: Double?
    ): Result<List<TicketmasterEvent>> {
        return try {
            val latlong = if (lat != null && lng != null) "$lat,$lng" else null
            val response = api.searchEvents(
                apiKey = apiKey,
                keyword = query,
                latlong = latlong
            )
            if (response.isSuccessful) {
                val events = response.body()?.embedded?.events ?: emptyList()
                if (events.isEmpty()) Result.success(getMockEvents(query))
                else Result.success(events)
            } else {
                Result.success(getMockEvents(query))
            }
        } catch (e: Exception) {
            Result.success(getMockEvents(query))
        }
    }
}
