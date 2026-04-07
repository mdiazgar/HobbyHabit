package com.example.hobbyhabit.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Query("SELECT * FROM sessions WHERE hobbyId = :hobbyId ORDER BY timestamp DESC")
    fun getSessionsForHobby(hobbyId: Int): Flow<List<Session>>

    @Query("SELECT COUNT(*) FROM sessions WHERE hobbyId = :hobbyId AND timestamp >= :weekStart")
    fun getSessionCountThisWeek(hobbyId: Int, weekStart: Long): Flow<Int>

    @Insert
    suspend fun insertSession(session: Session)

    @Update
    suspend fun updateSession(session: Session)
    @Delete
    suspend fun deleteSession(session: Session)
}
