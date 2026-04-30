package com.example.hobbyhabit.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hobbyhabit.data.local.Event
import com.example.hobbyhabit.data.local.EventSource
import com.example.hobbyhabit.data.local.Hobby
import com.example.hobbyhabit.data.local.HobbyDatabase
import com.example.hobbyhabit.data.local.Session
import com.example.hobbyhabit.data.repository.HobbyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId

class HobbyViewModel(application: Application) : AndroidViewModel(application) {

    private val db = HobbyDatabase.getDatabase(application)
    private val repository = HobbyRepository(db.hobbyDao(), db.sessionDao(), db.eventDao())

    // ── Hobbies ───────────────────────────────────────────────────────
    val hobbies: StateFlow<List<Hobby>> = repository.getAllHobbies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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

    fun logSession(
        hobbyId: Int,
        durationMinutes: Int,
        notes: String,
        dateTime: LocalDateTime? = null
    ) {
        viewModelScope.launch {
            val timestamp = dateTime
                ?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
                ?: System.currentTimeMillis()
            repository.logSession(
                Session(hobbyId = hobbyId, durationMinutes = durationMinutes,
                    notes = notes, timestamp = timestamp)
            )
        }
    }

    fun deleteSession(session: Session) {
        viewModelScope.launch { repository.deleteSession(session) }
    }

    fun updateSession(session: Session) {
        viewModelScope.launch { repository.updateSession(session) }
    }

    private val _editingSession = mutableStateOf<Session?>(null)
    val editingSession: State<Session?> = _editingSession

    fun startEditingSession(session: Session?) { _editingSession.value = session }

    // ── Events ────────────────────────────────────────────────────────
    fun getEventsForHobby(hobbyId: Int): Flow<List<Event>> =
        repository.getEventsForHobby(hobbyId)

    fun getEventCountForHobby(hobbyId: Int): Flow<Int> =
        repository.getEventCountForHobby(hobbyId)

    // All events — used by calendar
    val allEvents: StateFlow<List<Event>> = repository.getAllEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addManualEvent(
        hobbyId: Int,
        name: String,
        location: String,
        dateTime: Long,
        durationMinutes: Int?,
        url: String?,
        imageUri: String? = null
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
                    source          = EventSource.USER,
                    imageUri        = imageUri,
                    isCustomCategory = true
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

    // ── Calendar ──────────────────────────────────────────────────────

    // Currently displayed month on the calendar
    private val _calendarMonth = MutableStateFlow(YearMonth.now())
    val calendarMonth: StateFlow<YearMonth> = _calendarMonth

    fun nextMonth()  { _calendarMonth.value = _calendarMonth.value.plusMonths(1) }
    fun prevMonth()  { _calendarMonth.value = _calendarMonth.value.minusMonths(1) }

    // Selected day in the calendar
    private val _selectedDay = MutableStateFlow<LocalDate?>(null)
    val selectedDay: StateFlow<LocalDate?> = _selectedDay

    fun selectDay(date: LocalDate) {
        _selectedDay.value = if (_selectedDay.value == date) null else date
    }

    // Get events for a specific day from allEvents
    fun eventsForDay(date: LocalDate, events: List<Event>): List<Event> {
        val zoneId  = ZoneId.systemDefault()
        val dayStart = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val dayEnd   = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        return events.filter { it.dateTime in dayStart until dayEnd }
    }

    // Set of days that have events — used to mark calendar dots
    fun daysWithEvents(month: YearMonth, events: List<Event>): Set<LocalDate> {
        val zoneId = ZoneId.systemDefault()
        return events.mapNotNull { event ->
            try {
                java.time.Instant.ofEpochMilli(event.dateTime)
                    .atZone(zoneId).toLocalDate()
                    .takeIf { it.year == month.year && it.monthValue == month.monthValue }
            } catch (e: Exception) { null }
        }.toSet()
    }
}
