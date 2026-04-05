package com.example.hobbyhabit.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hobbies")
data class Hobby(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val weeklyGoal: Int,
    val createdAt: Long = System.currentTimeMillis()
)
