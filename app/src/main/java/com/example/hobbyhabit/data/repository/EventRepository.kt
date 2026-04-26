package com.example.hobbyhabit.data.repository

import com.example.hobbyhabit.data.remote.EventbriteApi
import com.example.hobbyhabit.data.remote.EventbriteEvent
import com.example.hobbyhabit.data.local.Event
import com.example.hobbyhabit.data.local.EventStatus
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
class EventRepository(private val api: EventbriteApi) {

    suspend fun searchEvents(
        token: String,
        query: String,
        lat: Double?,
        lng: Double?
    ): Result<List<Event>> {   // ✅ RETURN LOCAL MODEL
        return try {
            val response = api.searchEvents(
                token = "Bearer $token",
                query = query,
                location = null

            )

            if (response.isSuccessful) {
                val events = response.body()?.events
                    ?.mapNotNull { it.toLocalEvent() } // ✅ CONVERT HERE
                    ?: emptyList()

                Result.success(events)
            } else {
                Result.failure(Exception("API error: ${response.code()}"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔁 API → Local conversion
    private fun EventbriteEvent.toLocalEvent(): Event? {
        return Event(
            id = this.id ?: return null,
            name = this.name?.text ?: "No title",
            location = this.venue?.address?.localized_address_display ?: "Unknown",
            dateTime = parseDate(this.start?.local),
            durationMinutes = null,
            url = this.url,
            status = EventStatus.UPCOMING
        )
    }

    // 🕒 Date parser
    private fun parseDate(dateString: String?): Long {
        return try {
            if (dateString == null) return System.currentTimeMillis()

            val formatter = DateTimeFormatter.ISO_DATE_TIME
            val localDateTime = LocalDateTime.parse(dateString, formatter)

            localDateTime
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}