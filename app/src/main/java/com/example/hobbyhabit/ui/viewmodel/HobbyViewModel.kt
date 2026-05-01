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
import java.util.Calendar


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

    fun deleteHobby(hobby: Hobby) {
        viewModelScope.launch { repository.deleteHobby(hobby) }
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
    fun updateSession(session: Session) {
        viewModelScope.launch {
            repository.updateSession(session)
        }
    }
    fun getHobbyById(id: Int): Flow<Hobby?> =
        repository.getHobbyById(id)

    // SESSIONS
    fun getSessionsForHobby(hobbyId: Int): Flow<List<Session>> =
        repository.getSessionsForHobby(hobbyId)

    // dateTime is optional — if null, uses current time

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

    fun deleteSession(session: Session) {
        viewModelScope.launch { repository.deleteSession(session) }
    }

    // EVENTS
    fun getEventsForHobby(hobbyId: Int): Flow<List<com.example.hobbyhabit.data.local.Event>> =
        repository.getEventsForHobby(hobbyId)

    // WEEKLY TOTAL (SESSIONS + EVENTS)

    fun getTotalWeeklyActivity(hobbyId: Int): Flow<Int> =
        repository.getWeeklyActivityCount(hobbyId)

    // SESSION EDITING
    private val _editingSession = mutableStateOf<Session?>(null)
    val editingSession: State<Session?> = _editingSession

    fun startEditingSession(session: Session?) {
        _editingSession.value = session
    }

    // ── Events ────────────────────────────────────────────────────────

    fun getEventCountForHobby(hobbyId: Int): Flow<Int> =
        repository.getEventCountForHobby(hobbyId)
    fun deleteEvent(event: Event) {
        viewModelScope.launch { repository.deleteEvent(event) }
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
    private val _weeklyCounts = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val weeklyCounts: StateFlow<Map<Int, Int>> = _weeklyCounts.asStateFlow()

    fun loadWeeklyCount(hobbyId: Int) {
        viewModelScope.launch {
            repository.getWeeklyActivityCount(hobbyId)
                .collect { count ->
                    _weeklyCounts.update { it + (hobbyId to count) }
                }
        }
    }

    fun updateEvent(event: Event) {
        viewModelScope.launch { repository.updateEvent(event) }
    }
}
