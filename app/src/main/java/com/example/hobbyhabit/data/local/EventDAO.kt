package com.example.hobbyhabit.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event)

    @Update
    suspend fun updateEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)

    @Query("SELECT * FROM events WHERE hobbyId = :hobbyId ORDER BY dateTime DESC")
    fun getEventsForHobby(hobbyId: Int): Flow<List<Event>>

    @Query("SELECT * FROM events ORDER BY dateTime DESC")
    fun getAllEvents(): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE hobbyId = :hobbyId AND name = :name LIMIT 1")
    suspend fun findEvent(hobbyId: Int, name: String): Event?

    @Query("SELECT COUNT(*) FROM events WHERE hobbyId = :hobbyId")
    fun getEventCountForHobby(hobbyId: Int): Flow<Int>
}

