package com.example.hobbyhabit.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick  = { navController.navigate("home") {
                popUpTo("home") { inclusive = true }
            }},
            icon  = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Hobbies") }
        )
        NavigationBarItem(
            selected = currentRoute == "event_browser",
            onClick  = { navController.navigate("event_browser") {
                popUpTo("home")
            }},
            icon  = { Icon(Icons.Default.Search, contentDescription = "Events") },
            label = { Text("Events") }
        )
        NavigationBarItem(
            selected = currentRoute == "calendar",
            onClick  = { navController.navigate("calendar") {
                popUpTo("home")
            }},
            icon  = { Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar") },
            label = { Text("Calendar") }
        )
    }
}