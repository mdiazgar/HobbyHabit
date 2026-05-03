package com.example.hobbyhabit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.hobbyhabit.data.PreferencesManager
import com.example.hobbyhabit.navigation.NavGraph
import com.example.hobbyhabit.ui.components.BottomBar
import com.example.hobbyhabit.ui.screens.OnboardingScreen
import com.example.hobbyhabit.ui.theme.HobbyHabitTheme
import com.example.hobbyhabit.ui.viewmodel.EventViewModel
import com.example.hobbyhabit.ui.viewmodel.HobbyViewModel
import com.example.hobbyhabit.ui.viewmodel.UserViewModel
import com.example.hobbyhabit.worker.SessionReminderWorker

class MainActivity : ComponentActivity() {

    private val hobbyViewModel: HobbyViewModel by viewModels()
    private val eventViewModel: EventViewModel by viewModels()
    private val userViewModel: UserViewModel   by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Schedule reminder notifications
        try {
            SessionReminderWorker.schedule(this)
        } catch (e: Exception) {
            // WorkManager not available yet
        }
        val prefs = PreferencesManager(this)

        setContent {
            HobbyHabitTheme {
                var onboardingDone by remember { mutableStateOf(prefs.onboardingComplete) }

                if (!onboardingDone) {
                    OnboardingScreen(
                        onFinish = {
                            prefs.onboardingComplete = true
                            onboardingDone = true
                        }
                    )
                } else {
                    val navController = rememberNavController()
                    Scaffold(
                        bottomBar = { BottomBar(navController) }
                    ) { innerPadding ->
                        NavGraph(
                            navController  = navController,
                            hobbyViewModel = hobbyViewModel,
                            eventViewModel = eventViewModel,
                            userViewModel  = userViewModel,
                            modifier       = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}