package com.example.hobbyhabit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.example.hobbyhabit.data.local.HobbyDatabase
import com.example.hobbyhabit.data.remote.RetrofitInstance
import com.example.hobbyhabit.data.repository.EventRepository
import com.example.hobbyhabit.data.repository.HobbyRepository
import com.example.hobbyhabit.data.repository.TicketmasterRepository
import com.example.hobbyhabit.navigation.NavGraph
import com.example.hobbyhabit.ui.components.BottomBar
import com.example.hobbyhabit.ui.theme.HobbyHabitTheme
import com.example.hobbyhabit.ui.viewmodel.EventViewModel
import com.example.hobbyhabit.ui.viewmodel.EventViewModelFactory
import com.example.hobbyhabit.ui.viewmodel.HobbyViewModel
import com.example.hobbyhabit.ui.viewmodel.UserViewModel

class MainActivity : ComponentActivity() {

    private val hobbyViewModel: HobbyViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

    private lateinit var eventViewModel: EventViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // ✅ CORRECT DATABASE
        val database = HobbyDatabase.getDatabase(this)

        val hobbyRepository = HobbyRepository(
            database.hobbyDao(),
            database.sessionDao(),
                    database.eventDao()
        )
        val eventRepository = EventRepository(database.eventDao())
        val ticketmasterRepository = TicketmasterRepository(RetrofitInstance.api)

        val factory = EventViewModelFactory(
            hobbyRepository,
            eventRepository,
            ticketmasterRepository
        )

        eventViewModel = ViewModelProvider(this, factory)[EventViewModel::class.java]

        setContent {
            HobbyHabitTheme {

                val navController = rememberNavController()

                Scaffold(
                    bottomBar = {
                        BottomBar(navController)
                    }
                ) { innerPadding ->

                    NavGraph(
                        navController = navController,
                        hobbyViewModel = hobbyViewModel,
                        eventViewModel = eventViewModel,
                        userViewModel = userViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}