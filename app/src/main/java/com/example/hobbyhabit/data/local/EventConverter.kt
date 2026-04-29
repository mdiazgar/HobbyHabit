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

}