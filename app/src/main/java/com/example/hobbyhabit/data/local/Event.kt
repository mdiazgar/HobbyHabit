package com.example.hobbyhabit.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hobbyId: Int,

    val name: String,
    val location: String,
    val dateTime: Long,
    val durationMinutes: Int?,
    val url: String?,
    val source: EventSource
)


enum class EventSource {
    USER,
    TICKETMASTER
}