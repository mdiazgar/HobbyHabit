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
    private val ticketmasterRepository: TicketmasterRepository   // ✅ ADD THIS
) : ViewModel() {

    private val _uiState = MutableStateFlow<EventUiState>(EventUiState.Idle)
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()

    private val _selectedEvent = MutableStateFlow<TicketmasterEvent?>(null)
    val selectedEvent = _selectedEvent

    private val _showRegisterDialog = MutableStateFlow(false)
    val showRegisterDialog = _showRegisterDialog

    private val _showHobbyPicker = MutableStateFlow(false)
    val showHobbyPicker = _showHobbyPicker

    private val _navigateToCreateHobby = MutableStateFlow(false)
    val navigateToCreateHobby = _navigateToCreateHobby

    private var cachedHobbies: List<Hobby> = emptyList()

    init {
        viewModelScope.launch {
            hobbyRepository.getAllHobbies().collect {
                cachedHobbies = it
            }
        }
    }

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

    fun onEventClicked(event: TicketmasterEvent) {
        _selectedEvent.value = event
        _showRegisterDialog.value = true
    }

    fun dismissDialog() {
        _showRegisterDialog.value = false
        _selectedEvent.value = null
    }

    fun confirmRegisterEvent() {
        _showRegisterDialog.value = false

        if (cachedHobbies.isEmpty()) {
            _navigateToCreateHobby.value = true
        } else {
            _showHobbyPicker.value = true
        }
    }

    fun saveEventToHobby(hobby: Hobby) {
        val event = _selectedEvent.value ?: return

        viewModelScope.launch {
            eventRepository.insert(
                Event(
                    id = event.id ?: return@launch,
                    hobbyId = hobby.id,   // ✅ REQUIRED
                    name = event.name ?: "",
                    location = "",
                    dateTime = parseDate(event),
                    durationMinutes = null,
                    url = event.url,
                    status = EventStatus.UPCOMING
                )
            )
        }

        _selectedEvent.value = null
        _showHobbyPicker.value = false
    }

    fun dismissHobbyPicker() {
        _showHobbyPicker.value = false
    }

    fun navigateHandled() {
        _navigateToCreateHobby.value = false
    }

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