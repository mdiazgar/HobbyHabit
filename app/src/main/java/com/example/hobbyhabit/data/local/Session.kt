package com.example.hobbyhabit.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sessions",
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
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hobbyId: Int,
    val durationMinutes: Int,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
