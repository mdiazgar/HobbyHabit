package com.example.hobbyhabit.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class Event(
    @PrimaryKey val id: String, // from Eventbrite
    val name: String,
    val location: String,
    val dateTime: Long,
    val durationMinutes: Int?,
    val url: String?,
    val status: EventStatus
)

enum class EventStatus {
    UPCOMING,
    COMPLETED,
    MISSED
}