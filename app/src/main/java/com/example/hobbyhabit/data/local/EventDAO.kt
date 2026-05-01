package com.example.hobbyhabit.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("""
    SELECT COUNT(*) FROM events 
    WHERE hobbyId = :hobbyId 
    AND dateTime >= :weekStart
    AND dateTime <= :weekEnd
""")
    fun getEventCountThisWeek(
        hobbyId: Int,
        weekStart: Long,
        weekEnd: Long = System.currentTimeMillis()
    ): Flow<Int>
    @Query("SELECT * FROM events ORDER BY dateTime DESC")
    fun getAllEvents(): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE hobbyId = :hobbyId")
    fun getEventsForHobby(hobbyId: Int): Flow<List<Event>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)
    @Insert
    suspend fun insert(event: Event)
    @Query("SELECT COUNT(*) FROM events WHERE hobbyId = :hobbyId")
    fun getEventCountForHobby(hobbyId: Int): Flow<Int>

    @Query("SELECT * FROM events WHERE hobbyId = :hobbyId AND name = :name LIMIT 1")
    suspend fun findEvent(hobbyId: Int, name: String): Event?
}