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

    fun getAllHobbies(): Flow<List<Hobby>> {
        return hobbyDao.getAllHobbies()
    }

    fun getHobbyById(id: Int): Flow<Hobby?> {
        return hobbyDao.getHobbyById(id)
    }

    suspend fun addHobby(hobby: Hobby) {
        hobbyDao.insertHobby(hobby)
    }

    suspend fun deleteHobby(hobby: Hobby) {
        hobbyDao.deleteHobby(hobby)
    }

    // -------------------------
    // SESSIONS
    // -------------------------

    fun getSessionsForHobby(hobbyId: Int): Flow<List<Session>> {
        return sessionDao.getSessionsForHobby(hobbyId)
    }

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
    suspend fun updateSession(session: Session) {
        sessionDao.updateSession(session)
    }
    suspend fun logSession(session: Session) {
        sessionDao.insertSession(session)
    }

    suspend fun deleteSession(session: Session) {
        sessionDao.deleteSession(session)
    }


}