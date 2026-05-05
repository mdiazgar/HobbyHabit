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

    // ── Hobbies ───────────────────────────────────────────────────────
    fun getAllHobbies(): Flow<List<Hobby>> = hobbyDao.getAllHobbies()
    fun getHobbyById(id: Int): Flow<Hobby?> = hobbyDao.getHobbyById(id)
    suspend fun addHobby(hobby: Hobby) = hobbyDao.insertHobby(hobby)
    suspend fun deleteHobby(hobby: Hobby) = hobbyDao.deleteHobby(hobby)

    // ── Sessions ──────────────────────────────────────────────────────
    fun getSessionsForHobby(hobbyId: Int): Flow<List<Session>> =
        sessionDao.getSessionsForHobby(hobbyId)

    fun getAllSessions(): Flow<List<Session>> = sessionDao.getAllSessions()

    fun getSessionCountThisWeek(hobbyId: Int): Flow<Int> {
        return sessionDao.getSessionCountThisWeek(hobbyId, weekStartMillis())
    }

    suspend fun updateSession(session: Session) = sessionDao.updateSession(session)
    suspend fun deleteSession(session: Session) = sessionDao.deleteSession(session)

    // ── Events ────────────────────────────────────────────────────────
    fun getEventsForHobby(hobbyId: Int): Flow<List<Event>> =
        eventDao.getEventsForHobby(hobbyId)

    fun getAllEvents(): Flow<List<Event>> = eventDao.getAllEvents()

    fun getEventCountForHobby(hobbyId: Int): Flow<Int> =
        eventDao.getEventCountForHobby(hobbyId)


    suspend fun addEvent(event: Event) = eventDao.insertEvent(event)
    suspend fun updateEvent(event: Event) = eventDao.updateEvent(event)
    suspend fun deleteEvent(event: Event) = eventDao.deleteEvent(event)
    suspend fun findEvent(hobbyId: Int, name: String): Event? =
        eventDao.findEvent(hobbyId, name)

    // ── Weekly activity (sessions + events this week) ─────────────────
    fun getWeeklyActivityCount(hobbyId: Int): Flow<Int> {
        val weekStart = weekStartMillis()
        val now = System.currentTimeMillis()

        return combine(
            sessionDao.getSessionCountThisWeek(hobbyId, weekStart),
            eventDao.getWeeklyEventCount(hobbyId, weekStart, now)
        ) { sessions, events ->
            sessions + events
        }
    }
    suspend fun updateHobby(hobby: Hobby) {
        hobbyDao.updateHobby(hobby)
    }

    private fun weekStartMillis(): Long =
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
}