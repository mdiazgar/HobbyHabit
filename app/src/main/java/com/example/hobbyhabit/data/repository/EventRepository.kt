package com.example.hobbyhabit.data.repository

import com.example.hobbyhabit.data.remote.EventbriteApi
import com.example.hobbyhabit.data.remote.EventbriteEvent

class EventRepository(private val api: EventbriteApi) {

    suspend fun searchEvents(
        token: String,
        query: String,
        lat: Double?,
        lng: Double?
    ): Result<List<EventbriteEvent>> {
        return try {
            val response = api.searchEvents(
                token = "Bearer $token",
                query = query,
                lat = lat,
                lng = lng
            )
            if (response.isSuccessful) {
                Result.success(response.body()?.events ?: emptyList())
            } else {
                Result.failure(Exception("API error: " + response.code()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
