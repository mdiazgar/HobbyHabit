package com.example.hobbyhabit.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HobbyDao {

    @Query("SELECT * FROM hobbies ORDER BY createdAt DESC")
    fun getAllHobbies(): Flow<List<Hobby>>

    @Query("SELECT * FROM hobbies WHERE id = :id")
    fun getHobbyById(id: Int): Flow<Hobby?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHobby(hobby: Hobby): Long

    @Delete
    suspend fun deleteHobby(hobby: Hobby)
}
