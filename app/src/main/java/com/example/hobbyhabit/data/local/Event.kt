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
    val status: EventStatus,

    val source: EventSource = EventSource.TICKETMASTER
)

enum class EventStatus {
    UPCOMING,
    COMPLETED,
    MISSED
}

enum class EventSource {
    USER,
    TICKETMASTER
}