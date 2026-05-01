package com.example.hobbyhabit.data.repository

import com.example.hobbyhabit.data.local.Event
import com.example.hobbyhabit.data.local.EventDao
import com.example.hobbyhabit.data.local.Hobby
import com.example.hobbyhabit.data.local.HobbyDao
import com.example.hobbyhabit.data.local.Session
import com.example.hobbyhabit.data.local.SessionDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar


class HobbyRepository(
    private val hobbyDao: HobbyDao,
    private val sessionDao: SessionDao,
    private val eventDao: EventDao
) {

    // HOBBIES

    fun getAllHobbies(): Flow<List<Hobby>> = hobbyDao.getAllHobbies()
    // ── Sessions ──────────────────────────────────────────────────────
    fun getHobbyById(id: Int): Flow<Hobby?> = hobbyDao.getHobbyById(id)

    suspend fun addHobby(hobby: Hobby) = hobbyDao.insertHobby(hobby)
    // ── Events ────────────────────────────────────────────────────────
    fun getEventsForHobby(hobbyId: Int): Flow<List<Event>> =
        eventDao.getEventsForHobby(hobbyId)

    fun getEventCountForHobby(hobbyId: Int): Flow<Int> =
        eventDao.getEventCountForHobby(hobbyId)

    suspend fun addEvent(event: Event) = eventDao.insertEvent(event)
    suspend fun updateEvent(event: Event) = eventDao.updateEvent(event)
    suspend fun findEvent(hobbyId: Int, name: String): Event? =
        eventDao.findEvent(hobbyId, name)

    suspend fun updateSession(session: Session) =
        sessionDao.updateSession(session)
    suspend fun deleteHobby(hobby: Hobby) =
        hobbyDao.deleteHobby(hobby)

    suspend fun insertEvent(event: Event) {
        eventDao.insert(event)
    }
    fun getSessionsForHobby(hobbyId: Int): Flow<List<Session>> =
        sessionDao.getSessionsForHobby(hobbyId)

    suspend fun logSession(session: Session) =
        sessionDao.insertSession(session)
    suspend fun deleteSession(session: Session) =
        sessionDao.deleteSession(session)
    suspend fun deleteEvent(event: Event) {
        eventDao.deleteEvent(event)
    }

    //WEEKLY TOTAL (SESSIONS + EVENTS)
    fun getWeeklyActivityCount(hobbyId: Int): Flow<Int> {
        val weekStart = weekStartMillis()
        val now = System.currentTimeMillis()

        return combine(
            sessionDao.getSessionCountThisWeek(hobbyId, weekStart, now),
            eventDao.getWeeklyEventCount(hobbyId, weekStart, now)
        ) { sessions, events ->
            sessions + events
        }
    }

    private fun weekStartMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val diff = (dayOfWeek - Calendar.MONDAY + 7) % 7
        calendar.add(Calendar.DAY_OF_YEAR, -diff)
        return calendar.timeInMillis
    }
}