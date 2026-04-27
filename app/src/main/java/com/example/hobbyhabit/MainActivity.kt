package com.example.hobbyhabit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.hobbyhabit.navigation.NavGraph
import com.example.hobbyhabit.ui.components.BottomBar
import com.example.hobbyhabit.ui.theme.HobbyHabitTheme
import com.example.hobbyhabit.ui.viewmodel.EventViewModel
import com.example.hobbyhabit.ui.viewmodel.HobbyViewModel
import com.example.hobbyhabit.ui.viewmodel.UserViewModel

class MainActivity : ComponentActivity() {

    private val hobbyViewModel: HobbyViewModel by viewModels()
    private val eventViewModel: EventViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            HobbyHabitTheme {
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    hobbyViewModel = hobbyViewModel,
                    eventViewModel = eventViewModel,
                    userViewModel = userViewModel
                )
            }
        }
        }
    }
