package com.example.hobbyhabit.data.local

import androidx.room.TypeConverter

class EventConverters {

    // EventSource
    @TypeConverter
    fun fromEventSource(value: EventSource): String {
        return value.name
    }

    @TypeConverter
    fun toEventSource(value: String): EventSource {
        return EventSource.valueOf(value)
    }

    // EventStatus
    @TypeConverter
    fun fromEventStatus(value: EventStatus): String {
        return value.name
    }

    @TypeConverter
    fun toEventStatus(value: String): EventStatus {
        return EventStatus.valueOf(value)
    }
}