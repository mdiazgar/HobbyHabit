package com.example.hobbyhabit.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hobbyhabit.data.local.Hobby
import com.example.hobbyhabit.data.local.HobbyDatabase
import com.example.hobbyhabit.data.local.Session
import com.example.hobbyhabit.data.repository.HobbyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
class HobbyViewModel(application: Application) : AndroidViewModel(application) {

    private val db = HobbyDatabase.getDatabase(application)
    private val repository = HobbyRepository(db.hobbyDao(), db.sessionDao())

    val hobbies: StateFlow<List<Hobby>> = repository.getAllHobbies()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun addHobby(name: String, weeklyGoal: Int) {
        viewModelScope.launch {
            repository.addHobby(Hobby(name = name, weeklyGoal = weeklyGoal))
        }
    }

    fun deleteHobby(hobby: Hobby) {
        viewModelScope.launch { repository.deleteHobby(hobby) }
    }

    fun getHobbyById(id: Int): Flow<Hobby?> = repository.getHobbyById(id)

    fun getSessionsForHobby(hobbyId: Int): Flow<List<Session>> =
        repository.getSessionsForHobby(hobbyId)

    fun getSessionCountThisWeek(hobbyId: Int): Flow<Int> =
        repository.getSessionCountThisWeek(hobbyId)

    fun logSession(hobbyId: Int, durationMinutes: Int, notes: String) {
        viewModelScope.launch {
            repository.logSession(
                Session(hobbyId = hobbyId, durationMinutes = durationMinutes, notes = notes)
            )
        }
    }

    fun deleteSession(session: Session) {
        viewModelScope.launch {
            repository.deleteSession(session)
        }
    }

    private val _editingSession = mutableStateOf<Session?>(null)
    val editingSession: State<Session?> = _editingSession
    fun startEditingSession(session: Session?) {
        _editingSession.value = session
    }

    fun updateSession(session: Session) {
        viewModelScope.launch {
            repository.updateSession(session) // implement this in your repository
        }
    }

}
