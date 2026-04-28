package com.example.hobbyhabit.data.repository

import com.example.hobbyhabit.data.local.Event
import com.example.hobbyhabit.data.local.EventDao
import kotlinx.coroutines.flow.Flow

class EventRepository(
    private val eventDao: EventDao
) {

    fun getAllEvents(): Flow<List<Event>> {
        return eventDao.getAllEvents()
    }

    fun getEventsForHobby(hobbyId: Int): Flow<List<Event>> {
        return eventDao.getEventsForHobby(hobbyId)
    }

    suspend fun insert(event: Event) {
        eventDao.insertEvent(event)
    }

    suspend fun delete(event: Event) {
        eventDao.deleteEvent(event)
    }
}