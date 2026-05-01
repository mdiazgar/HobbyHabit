package com.example.hobbyhabit.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hobbyhabit.data.local.Hobby
import com.example.hobbyhabit.data.local.HobbyDatabase
import com.example.hobbyhabit.data.local.Session
import com.example.hobbyhabit.data.repository.HobbyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import com.example.hobbyhabit.data.local.Event
import com.example.hobbyhabit.data.local.EventSource

class HobbyViewModel(application: Application) : AndroidViewModel(application) {

    private val db = HobbyDatabase.getDatabase(application)

    private val repository = HobbyRepository(
        db.hobbyDao(),
        db.sessionDao(),
        db.eventDao()
    )

    // HOBBIES
    val hobbies: StateFlow<List<Hobby>> = repository.getAllHobbies()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // name = display name, category = Ticketmaster classification
    fun addHobby(name: String, category: String, weeklyGoal: Int) {
        viewModelScope.launch {
            repository.addHobby(Hobby(name = name, category = category, weeklyGoal = weeklyGoal))
        }
    }
    fun getWeeklyActivityCount(hobbyId: Int): Flow<Int> =
        repository.getWeeklyActivityCount(hobbyId)

    fun deleteHobby(hobby: Hobby) {
        viewModelScope.launch {
            repository.deleteHobby(hobby)
        }
    }

    fun addManualEvent(
        hobbyId: Int,
        name: String,
        location: String?,
        dateTime: Long,
        durationMinutes: Int?,
        url: String?
    ) {
        viewModelScope.launch {
            repository.insertEvent(
                Event(
                    hobbyId = hobbyId,
                    name = name,
                    location = location,
                    dateTime = dateTime,
                    durationMinutes = durationMinutes,
                    url = url,
                    source = EventSource.USER
                )
            )
        }
    }

    fun getHobbyById(id: Int): Flow<Hobby?> =
        repository.getHobbyById(id)

    // SESSIONS
    fun getSessionsForHobby(hobbyId: Int): Flow<List<Session>> =
        repository.getSessionsForHobby(hobbyId)

    fun getSessionCountThisWeek(hobbyId: Int): Flow<Int> =
        repository.getSessionCountThisWeek(hobbyId)

    fun logSession(
        hobbyId: Int,
        durationMinutes: Int,
        notes: String,
        location: String? = null,
        dateTime: Long = System.currentTimeMillis(),
        url: String? = null)
    {
        viewModelScope.launch {
            repository.logSession(
                Session(
                    hobbyId = hobbyId,
                    durationMinutes = durationMinutes,
                    notes = notes,
                    location = location,
                    dateTime = dateTime,
                    url = url,
                    source = EventSource.USER
                )
            )
        }
    }
    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            repository.deleteEvent(event)
        }
    }
    fun deleteSession(session: Session) {
        viewModelScope.launch {
            repository.deleteSession(session)
        }
    }

    // EVENTS
    fun getEventsForHobby(hobbyId: Int): Flow<List<com.example.hobbyhabit.data.local.Event>> =
        repository.getEventsForHobby(hobbyId)

    fun getEventCountForHobby(hobbyId: Int): Flow<Int> =
        repository.getEventCountForHobby(hobbyId)

    // WEEKLY TOTAL (SESSIONS + EVENTS)

    fun getTotalWeeklyActivity(hobbyId: Int): Flow<Int> =
        repository.getWeeklyActivityCount(hobbyId)

    // SESSION EDITING
    private val _editingSession = mutableStateOf<Session?>(null)
    val editingSession: State<Session?> = _editingSession

    fun startEditingSession(session: Session?) {
        _editingSession.value = session
    }

    fun updateSession(session: Session) {
        viewModelScope.launch {
            repository.updateSession(session)
        }
    }
}
