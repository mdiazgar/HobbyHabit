package com.example.hobbyhabit.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HobbyDao {
    @Update
    suspend fun updateSession(session: Session)
    @Query("SELECT * FROM hobbies ORDER BY createdAt DESC")
    fun getAllHobbies(): Flow<List<Hobby>>

    @Update
    suspend fun updateHobby(hobby: Hobby)

    @Query("SELECT * FROM hobbies WHERE id = :id")
    fun getHobbyById(id: Int): Flow<Hobby?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHobby(hobby: Hobby): Long

    @Delete
    suspend fun deleteHobby(hobby: Hobby)
}
