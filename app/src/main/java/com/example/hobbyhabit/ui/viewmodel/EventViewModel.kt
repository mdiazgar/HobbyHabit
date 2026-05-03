package com.example.hobbyhabit.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hobbyhabit.data.local.Hobby
import com.example.hobbyhabit.data.local.HobbyDatabase
import com.example.hobbyhabit.data.mapper.toEvent
import com.example.hobbyhabit.data.remote.RetrofitInstance
import com.example.hobbyhabit.data.remote.TicketmasterEvent
import com.example.hobbyhabit.data.repository.EventRepository
import com.example.hobbyhabit.data.repository.HobbyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class EventUiState {
    object Idle    : EventUiState()
    object Loading : EventUiState()
    data class Success(val events: List<TicketmasterEvent>) : EventUiState()
    data class Error(val message: String) : EventUiState()
}

class EventViewModel(application: Application) : AndroidViewModel(application) {

    private val db = HobbyDatabase.getDatabase(application)

    // Ticketmaster API repository
    private val apiRepository = EventRepository(db.eventDao(), RetrofitInstance.api)

    // Hobbies — needed for hobby picker dialog
    private val hobbyRepository = HobbyRepository(db.hobbyDao(), db.sessionDao(), db.eventDao())

    val hobbies: StateFlow<List<Hobby>> = hobbyRepository.getAllHobbies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Ticketmaster search state ──────────────────────────────────────
    private val _uiState = MutableStateFlow<EventUiState>(EventUiState.Idle)
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()

    // ── Register dialog state ──────────────────────────────────────────
    private val _selectedEvent = MutableStateFlow<TicketmasterEvent?>(null)
    val selectedEvent: StateFlow<TicketmasterEvent?> = _selectedEvent.asStateFlow()

    private val _showRegisterDialog = MutableStateFlow(false)
    val showRegisterDialog: StateFlow<Boolean> = _showRegisterDialog.asStateFlow()

    private val _showHobbyPicker = MutableStateFlow(false)
    val showHobbyPicker: StateFlow<Boolean> = _showHobbyPicker.asStateFlow()

    private val _navigateToCreateHobby = MutableStateFlow(false)
    val navigateToCreateHobby: StateFlow<Boolean> = _navigateToCreateHobby.asStateFlow()

    // ── Search Ticketmaster ────────────────────────────────────────────
    fun searchEvents(apiKey: String, query: String, lat: Double? = null, lng: Double? = null) {
        _uiState.value = EventUiState.Loading
        viewModelScope.launch {
            val result = apiRepository.searchEvents(apiKey, query, lat, lng)
            _uiState.value = result.fold(
                onSuccess = { EventUiState.Success(it) },
                onFailure = { EventUiState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    // ── Event click + register flow ────────────────────────────────────
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
        if (hobbies.value.isEmpty()) _navigateToCreateHobby.value = true
        else _showHobbyPicker.value = true
    }

    fun dismissHobbyPicker() {
        _showHobbyPicker.value = false
        _selectedEvent.value = null
    }

    fun navigateHandled() {
        _navigateToCreateHobby.value = false
    }

    fun registerTicketmasterEvent(hobby: Hobby) {
        val tmEvent = _selectedEvent.value ?: return
        viewModelScope.launch {
            val event    = tmEvent.toEvent(hobby.id)
            val existing = apiRepository.findEvent(hobby.id, event.name)
            if (existing == null) apiRepository.insert(event)
        }
        _selectedEvent.value = null
        _showHobbyPicker.value = false
    }
}