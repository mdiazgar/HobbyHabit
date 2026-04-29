package com.example.hobbyhabit.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: Int = 1, // single user, always id = 1
    val username: String,
    val email: String,
    val createdAt: Long = System.currentTimeMillis()
)