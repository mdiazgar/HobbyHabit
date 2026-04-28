package com.example.hobbyhabit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hobbyhabit.data.local.Event
import com.example.hobbyhabit.data.local.EventStatus
import com.example.hobbyhabit.data.local.Hobby
import com.example.hobbyhabit.data.remote.TicketmasterEvent
import com.example.hobbyhabit.data.repository.EventRepository
import com.example.hobbyhabit.data.repository.HobbyRepository
import com.example.hobbyhabit.data.repository.TicketmasterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


sealed class EventUiState {
    object Idle : EventUiState()
    object Loading : EventUiState()
    data class Success(val events: List<TicketmasterEvent>) : EventUiState()
    data class Error(val message: String) : EventUiState()
}

class EventViewModel(
    private val hobbyRepository: HobbyRepository,
    private val eventRepository: EventRepository,
    private val ticketmasterRepository: TicketmasterRepository
) : ViewModel() {

    // UI STATE (Ticketmaster search)
    private val _uiState = MutableStateFlow<EventUiState>(EventUiState.Idle)
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()

    // Selected Event
    private val _selectedEvent = MutableStateFlow<TicketmasterEvent?>(null)
    val selectedEvent: StateFlow<TicketmasterEvent?> = _selectedEvent.asStateFlow()

    // Dialog States
    private val _showRegisterDialog = MutableStateFlow(false)
    val showRegisterDialog: StateFlow<Boolean> = _showRegisterDialog.asStateFlow()

    private val _showHobbyPicker = MutableStateFlow(false)
    val showHobbyPicker: StateFlow<Boolean> = _showHobbyPicker.asStateFlow()

    private val _navigateToCreateHobby = MutableStateFlow(false)
    val navigateToCreateHobby: StateFlow<Boolean> = _navigateToCreateHobby.asStateFlow()

    // HOBBIES
    private val _hobbies = MutableStateFlow<List<Hobby>>(emptyList())
    val hobbies: StateFlow<List<Hobby>> = _hobbies.asStateFlow()

    init {
        // Load hobbies from Room
        viewModelScope.launch {
            hobbyRepository.getAllHobbies().collect { list ->
                _hobbies.value = list
            }
        }
    }

    // SEARCH EVENTS
    fun searchEvents(
        apiKey: String,
        query: String,
        lat: Double? = null,
        lng: Double? = null
    ) {
        _uiState.value = EventUiState.Loading

        viewModelScope.launch {
            val result = ticketmasterRepository.searchEvents(
                apiKey, query, lat, lng
            )

            _uiState.value = result.fold(
                onSuccess = { EventUiState.Success(it) },
                onFailure = { EventUiState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    // EVENT CLICKED
    fun onEventClicked(event: TicketmasterEvent) {
        _selectedEvent.value = event
        _showRegisterDialog.value = true
    }

    fun dismissDialog() {
        _showRegisterDialog.value = false
        _selectedEvent.value = null
    }

    // REGISTER FLOW
    fun confirmRegisterEvent() {
        _showRegisterDialog.value = false

        if (_hobbies.value.isEmpty()) {
            _navigateToCreateHobby.value = true
        } else {
            _showHobbyPicker.value = true
        }
    }

    fun dismissHobbyPicker() {
        _showHobbyPicker.value = false
    }

    fun navigateHandled() {
        _navigateToCreateHobby.value = false
    }

    // SAVE EVENT to HOBBY
    fun saveEventToHobby(hobby: Hobby) {

        android.util.Log.d("EVENT_DEBUG", "saveEventToHobby CALLED")
        android.util.Log.d("EVENT_DEBUG", "Selected event BEFORE = ${_selectedEvent.value}")
        android.util.Log.d("EVENT_DEBUG", "Clicked hobby = $hobby")

        val event = _selectedEvent.value
        if (event == null) {
            android.util.Log.e("EVENT_DEBUG", "Selected event is NULL ❌")
            return
        }

        viewModelScope.launch {
            val eventId = event.id ?: run {
                android.util.Log.e("EVENT_DEBUG", "Event ID is NULL ❌")
                return@launch
            }

            eventRepository.insert(
                Event(
                    hobbyId = hobby.id,
                    name = event.name ?: "",
                    location = "",
                    dateTime = parseDate(event),
                    durationMinutes = null,
                    url = event.url,
                    status = EventStatus.UPCOMING
                )
            )

            android.util.Log.d("EVENT_DEBUG", "EVENT SAVED SUCCESSFULLY ✅")

            _selectedEvent.value = null
            _showHobbyPicker.value = false
        }
    }

    // DATE PARSING
    private fun parseDate(event: TicketmasterEvent): Long {
        val date = event.dates?.start?.localDate ?: return System.currentTimeMillis()
        val time = event.dates?.start?.localTime ?: "00:00:00"

        return try {
            val dateTimeString = "${date}T$time"
            java.time.LocalDateTime.parse(dateTimeString)
                .atZone(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}