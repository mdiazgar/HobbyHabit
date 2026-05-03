package com.example.hobbyhabit.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "events",
    foreignKeys = [
        ForeignKey(
            entity = Hobby::class,
            parentColumns = ["id"],
            childColumns = ["hobbyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("hobbyId")]
)
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hobbyId: Int,
    val name: String,
    val location: String = "",
    val dateTime: Long,                    // epoch millis — used for calendar filtering
    val durationMinutes: Int? = null,
    val url: String? = null,
    val source: EventSource = EventSource.USER,
    val imageUri: String? = null,          // user-uploaded photo URI (local)
    val isCustomCategory: Boolean = false, // true for manually created events
    val createdAt: Long = System.currentTimeMillis()
)