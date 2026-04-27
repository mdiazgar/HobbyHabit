package com.example.hobbyhabit.data.repository

import com.example.hobbyhabit.data.local.Event
import com.example.hobbyhabit.data.local.EventDao
import kotlinx.coroutines.flow.Flow

class EventRepository(
    private val eventDao: EventDao
) {
    fun getUpcomingEvents(): Flow<List<Event>> =
        eventDao.getUpcomingEvents()

    fun getEventsForHobby(hobbyId: Int): Flow<List<Event>> =
        eventDao.getEventsForHobby(hobbyId)

    suspend fun insert(event: Event) =
        eventDao.insert(event)

    suspend fun update(event: Event) =
        eventDao.update(event)
}