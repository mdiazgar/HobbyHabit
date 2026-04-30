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
import java.time.LocalDateTime
import java.time.ZoneId

class HobbyViewModel(application: Application) : AndroidViewModel(application) {

    private val db = HobbyDatabase.getDatabase(application)

    private val repository = HobbyRepository(
        db.hobbyDao(),
        db.sessionDao(),
        db.eventDao()
    )

    // ── Hobbies ───────────────────────────────────────────────────────
    val hobbies: StateFlow<List<Hobby>> = repository.getAllHobbies()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun addHobby(name: String, category: String, weeklyGoal: Int) {
        viewModelScope.launch {
            repository.addHobby(Hobby(name = name, category = category, weeklyGoal = weeklyGoal))
        }
    }

    fun deleteHobby(hobby: Hobby) {
        viewModelScope.launch { repository.deleteHobby(hobby) }
    }

    fun getHobbyById(id: Int): Flow<Hobby?> = repository.getHobbyById(id)

    // ── Sessions ──────────────────────────────────────────────────────
    fun getSessionsForHobby(hobbyId: Int): Flow<List<Session>> =
        repository.getSessionsForHobby(hobbyId)

    fun getSessionCountThisWeek(hobbyId: Int): Flow<Int> =
        repository.getSessionCountThisWeek(hobbyId)

    fun getTotalWeeklyActivity(hobbyId: Int): Flow<Int> =
        repository.getWeeklyActivityCount(hobbyId)

    fun getWeeklyActivityCount(hobbyId: Int): Flow<Int> =
        repository.getWeeklyActivityCount(hobbyId)

    // dateTime is optional — if null, uses current time
    fun logSession(
        hobbyId: Int,
        durationMinutes: Int,
        notes: String,
        dateTime: LocalDateTime? = null,
        timestamp: Long
    ) {
        viewModelScope.launch {
            val timestamp = dateTime
                ?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
                ?: System.currentTimeMillis()
            repository.logSession(
                Session(
                    hobbyId         = hobbyId,
                    durationMinutes = durationMinutes,
                    notes           = notes,
                    timestamp       = timestamp
                )
            )
        }
    }

    fun deleteSession(session: Session) {
        viewModelScope.launch { repository.deleteSession(session) }
    }

    fun updateSession(session: Session) {
        viewModelScope.launch { repository.updateSession(session) }
    }

    // Session editing state
    private val _editingSession = mutableStateOf<Session?>(null)
    val editingSession: State<Session?> = _editingSession

    fun startEditingSession(session: Session?) {
        _editingSession.value = session
    }

    // ── Events ────────────────────────────────────────────────────────
    fun getEventsForHobby(hobbyId: Int): Flow<List<Event>> =
        repository.getEventsForHobby(hobbyId)

    fun getEventCountForHobby(hobbyId: Int): Flow<Int> =
        repository.getEventCountForHobby(hobbyId)

    fun addManualEvent(
        hobbyId: Int,
        name: String,
        location: String,
        dateTime: Long,
        durationMinutes: Int?,
        url: String?
    ) {
        viewModelScope.launch {
            repository.addEvent(
                Event(
                    hobbyId         = hobbyId,
                    name            = name.trim(),
                    location        = location.trim(),
                    dateTime        = dateTime,
                    durationMinutes = durationMinutes,
                    url             = url?.trim()?.takeIf { it.isNotBlank() },
                    source          = EventSource.USER
                )
            )
        }
    }

    fun deleteEvent(event: Event) {
        viewModelScope.launch { repository.deleteEvent(event) }
    }

    fun updateEvent(event: Event) {
        viewModelScope.launch { repository.updateEvent(event) }
    }
}
