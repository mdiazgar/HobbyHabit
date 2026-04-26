package com.example.hobbyhabit.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

// Ticketmaster Discovery API v2
// Docs: https://developer.ticketmaster.com/products-and-docs/apis/discovery-api/v2/
interface TicketmasterApi {

    @GET("events.json")
    suspend fun searchEvents(
        @Query("apikey") apiKey: String,
        @Query("keyword") keyword: String,          // e.g. "pottery"
        @Query("latlong") latlong: String? = null,  // e.g. "42.35,-71.06"
        @Query("radius") radius: String = "50",
        @Query("unit") unit: String = "miles",
        @Query("size") size: Int = 10,
        @Query("sort") sort: String = "date,asc"
    ): Response<TicketmasterResponse>
}