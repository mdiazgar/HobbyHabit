package com.example.hobbyhabit.data.repository

import com.example.hobbyhabit.data.local.Hobby
import com.example.hobbyhabit.data.local.HobbyDao
import com.example.hobbyhabit.data.local.Session
import com.example.hobbyhabit.data.local.SessionDao
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class HobbyRepository(
    private val hobbyDao: HobbyDao,
    private val sessionDao: SessionDao
) {

    fun getAllHobbies(): Flow<List<Hobby>> = hobbyDao.getAllHobbies()

    fun getHobbyById(id: Int): Flow<Hobby?> = hobbyDao.getHobbyById(id)

    fun getSessionsForHobby(hobbyId: Int): Flow<List<Session>> =
        sessionDao.getSessionsForHobby(hobbyId)

    fun getSessionCountThisWeek(hobbyId: Int): Flow<Int> {
        val weekStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return sessionDao.getSessionCountThisWeek(hobbyId, weekStart)
    }

    suspend fun addHobby(hobby: Hobby) = hobbyDao.insertHobby(hobby)

    suspend fun deleteHobby(hobby: Hobby) = hobbyDao.deleteHobby(hobby)

    suspend fun logSession(session: Session) = sessionDao.insertSession(session)
}
