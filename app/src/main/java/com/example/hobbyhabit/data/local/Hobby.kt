package com.example.hobbyhabit.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hobbies")
data class Hobby(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,           // display name e.g. "Theatre & Arts"
    val category: String,       // Ticketmaster classification e.g. "Arts & Theatre"
    val weeklyGoal: Int,
    val createdAt: Long = System.currentTimeMillis()
)
