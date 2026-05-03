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

    // All sessions — used for stats and streak
    val allSessions: StateFlow<List<Session>> = repository.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Events ────────────────────────────────────────────────────────
    fun getEventsForHobby(hobbyId: Int): Flow<List<Event>> =
        repository.getEventsForHobby(hobbyId)

    fun getEventCountForHobby(hobbyId: Int): Flow<Int> =
        repository.getEventCountForHobby(hobbyId)

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
                    hobbyId          = hobbyId,
                    name             = name.trim(),
                    location         = location.trim(),
                    dateTime         = dateTime,
                    durationMinutes  = durationMinutes,
                    url              = url?.trim()?.takeIf { it.isNotBlank() },
                    source           = EventSource.USER,
                    imageUri         = imageUri,
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
    private val _calendarMonth = MutableStateFlow(YearMonth.now())
    val calendarMonth: StateFlow<YearMonth> = _calendarMonth

    fun nextMonth() { _calendarMonth.value = _calendarMonth.value.plusMonths(1) }
    fun prevMonth() { _calendarMonth.value = _calendarMonth.value.minusMonths(1) }

    private val _selectedDay = MutableStateFlow<LocalDate?>(null)
    val selectedDay: StateFlow<LocalDate?> = _selectedDay

    fun selectDay(date: LocalDate) {
        _selectedDay.value = if (_selectedDay.value == date) null else date
    }

    fun eventsForDay(date: LocalDate, events: List<Event>): List<Event> {
        val zoneId   = ZoneId.systemDefault()
        val dayStart = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val dayEnd   = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        return events.filter { it.dateTime in dayStart until dayEnd }
    }

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

    // ── Stats ─────────────────────────────────────────────────────────

    /** Total minutes logged for a hobby */
    fun totalMinutes(sessions: List<Session>): Int =
        sessions.sumOf { it.durationMinutes }

    /** Average session duration in minutes */
    fun avgMinutes(sessions: List<Session>): Int =
        if (sessions.isEmpty()) 0 else totalMinutes(sessions) / sessions.size

    /** Current consecutive-week streak for a hobby.
     *  A week counts if the user logged at least [weeklyGoal] sessions/events. */
    fun currentStreak(sessions: List<Session>, events: List<Event>, weeklyGoal: Int): Int {
        if (weeklyGoal == 0) return 0
        val zoneId = ZoneId.systemDefault()

        // Combine session and event timestamps into a set of (year, week) pairs
        fun epochToWeek(millis: Long): Pair<Int, Int> {
            val date = java.time.Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDate()
            val wf   = java.time.temporal.WeekFields.of(java.util.Locale.getDefault())
            return date.get(wf.weekOfWeekBasedYear()) to date.get(wf.weekBasedYear())
        }

        // Count activity per week
        val activityPerWeek = mutableMapOf<Pair<Int, Int>, Int>()
        sessions.forEach { s ->
            val key = epochToWeek(s.timestamp)
            activityPerWeek[key] = (activityPerWeek[key] ?: 0) + 1
        }
        events.forEach { e ->
            val key = epochToWeek(e.dateTime)
            activityPerWeek[key] = (activityPerWeek[key] ?: 0) + 1
        }

        // Walk backwards from current week counting consecutive goal-hit weeks
        val wf      = java.time.temporal.WeekFields.of(java.util.Locale.getDefault())
        var current = LocalDate.now()
        var streak  = 0

        while (true) {
            val key   = current.get(wf.weekOfWeekBasedYear()) to current.get(wf.weekBasedYear())
            val count = activityPerWeek[key] ?: 0
            if (count >= weeklyGoal) {
                streak++
                current = current.minusWeeks(1)
            } else {
                break
            }
        }
        return streak
    }

    /** Best (longest) streak ever for a hobby */
    fun bestStreak(sessions: List<Session>, events: List<Event>, weeklyGoal: Int): Int {
        if (weeklyGoal == 0) return 0
        val zoneId = ZoneId.systemDefault()
        fun epochToWeek(millis: Long): Pair<Int, Int> {
            val date = java.time.Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDate()
            val wf   = java.time.temporal.WeekFields.of(java.util.Locale.getDefault())
            return date.get(wf.weekOfWeekBasedYear()) to date.get(wf.weekBasedYear())
        }
        val activityPerWeek = mutableMapOf<Pair<Int, Int>, Int>()
        sessions.forEach { s ->
            val key = epochToWeek(s.timestamp)
            activityPerWeek[key] = (activityPerWeek[key] ?: 0) + 1
        }
        events.forEach { e ->
            val key = epochToWeek(e.dateTime)
            activityPerWeek[key] = (activityPerWeek[key] ?: 0) + 1
        }
        if (activityPerWeek.isEmpty()) return 0

        // Sort weeks chronologically and find longest consecutive run
        val sortedWeeks = activityPerWeek.entries
            .filter { it.value >= weeklyGoal }
            .map { it.key }

        if (sortedWeeks.isEmpty()) return 0

        var best    = 1
        var current = 1
        val sorted  = sortedWeeks.sortedWith(compareBy({ it.second }, { it.first }))

        for (i in 1 until sorted.size) {
            val prev = sorted[i - 1]
            val curr = sorted[i]
            // Check if consecutive (handles year boundary)
            val prevDate = java.time.LocalDate.now() // placeholder — we just check adjacency
            val isConsecutive = (curr.second == prev.second && curr.first == prev.first + 1) ||
                    (curr.second == prev.second + 1 && curr.first == 1 &&
                            prev.first >= 51)
            if (isConsecutive) {
                current++
                if (current > best) best = current
            } else {
                current = 1
            }
        }
        return best
    }

    // Home screen search + sort state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    fun setSearchQuery(q: String) { _searchQuery.value = q }

    enum class SortOrder { DEFAULT, NAME_ASC, MOST_ACTIVE }
    private val _sortOrder = MutableStateFlow(SortOrder.DEFAULT)
    val sortOrder: StateFlow<SortOrder> = _sortOrder
    fun setSortOrder(order: SortOrder) { _sortOrder.value = order }
}
