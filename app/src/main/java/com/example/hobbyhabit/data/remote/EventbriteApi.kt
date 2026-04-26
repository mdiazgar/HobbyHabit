package com.example.hobbyhabit.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface EventbriteApi {

    @GET("events/search/")
    suspend fun searchEvents(
        @Header("Authorization") token: String,
        @Query("q") query: String,
        @Query("location.address") location: String? = null,
        @Query("location.within") within: String = "20km",
        @Query("expand") expand: String = "venue"
    ): Response<EventbriteResponse>
}
