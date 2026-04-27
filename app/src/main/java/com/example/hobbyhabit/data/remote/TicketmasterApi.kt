package com.example.hobbyhabit.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TicketmasterApi {

    @GET("events.json")
    suspend fun searchEvents(
        @Query("apikey") apiKey: String,
        @Query("classificationName") classificationName: String, // e.g. "Arts & Theatre"
        @Query("latlong") latlong: String? = null,
        @Query("radius") radius: String = "50",
        @Query("unit") unit: String = "miles",
        @Query("size") size: Int = 10,
        @Query("sort") sort: String = "date,asc"
    ): Response<TicketmasterResponse>
}