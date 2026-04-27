package com.example.hobbyhabit.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: Event)

    @Update
    suspend fun update(event: Event)

    @Query("SELECT * FROM events WHERE hobbyId = :hobbyId")
    fun getEventsForHobby(hobbyId: Int): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE status = 'UPCOMING'")
    fun getUpcomingEvents(): Flow<List<Event>>
}