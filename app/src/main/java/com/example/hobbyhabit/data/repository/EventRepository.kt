package com.example.hobbyhabit.data.repository

import com.example.hobbyhabit.data.local.Event
import com.example.hobbyhabit.data.remote.TicketmasterApi
import com.example.hobbyhabit.data.remote.TicketmasterEvent
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import com.example.hobbyhabit.data.local.EventDao

class EventRepository(
    private val eventDao: EventDao,
    private val api: TicketmasterApi
) {

    // Returns today's date in the format Ticketmaster expects: "2026-04-28T00:00:00Z"
    private fun todayAsStartDateTime(): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        fmt.timeZone = TimeZone.getTimeZone("UTC")
        return fmt.format(Date())
    }

    private fun getMockEvents(category: String): List<TicketmasterEvent> {
        return listOf(
            TicketmasterEvent(
                id = "1", name = "$category Event — This Weekend",
                url = "https://www.ticketmaster.com", dates = null, embedded = null
            ),
            TicketmasterEvent(
                id = "2", name = "Local $category Show",
                url = "https://www.ticketmaster.com", dates = null, embedded = null
            ),
            TicketmasterEvent(
                id = "3", name = "$category Festival Near You",
                url = "https://www.ticketmaster.com", dates = null, embedded = null
            )
        )
    }

    suspend fun findEvent(hobbyId: Int, name: String): Event? {
        return eventDao.findEvent(hobbyId, name)
    }


    suspend fun searchEvents(
        apiKey: String,
        category: String,
        lat: Double?,
        lng: Double?
    ): Result<List<TicketmasterEvent>> {
        return try {
            val latlong = if (lat != null && lng != null) "$lat,$lng" else null
            val response = api.searchEvents(
                apiKey = apiKey,
                classificationName = category,
                latlong = latlong,
                keyword = category,
                startDateTime = todayAsStartDateTime()  // only future events
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

    suspend fun insert(event: Event) {
        eventDao.insertEvent(event)
    }

    suspend fun delete(event: Event) {
        eventDao.deleteEvent(event)
    }
}