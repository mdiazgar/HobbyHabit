package com.example.hobbyhabit.data.repository

import com.example.hobbyhabit.data.remote.TicketmasterApi
import com.example.hobbyhabit.data.remote.TicketmasterEvent

class TicketmasterRepository(
    private val api: TicketmasterApi
) {

    suspend fun searchEvents(
        apiKey: String,
        query: String,
        lat: Double?,
        lng: Double?
    ): Result<List<TicketmasterEvent>> {

        return try {

            val latlong = if (lat != null && lng != null) {
                "$lat,$lng"
            } else null

            val response = api.searchEvents(
                apiKey = apiKey,
                keyword = query,   // IMPORTANT: must be keyword (not query)
                latlong = latlong
            )

            val events =
                response.body()?.embedded?.events ?: emptyList()

            Result.success(events)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}