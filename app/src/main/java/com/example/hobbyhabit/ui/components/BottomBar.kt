package com.example.hobbyhabit.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialogDefaults.containerColor
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text


@Composable
fun BottomBar(navController: NavHostController) {
    NavigationBar (
        containerColor = MaterialTheme.colorScheme.primaryContainer
    )
    {
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("home") },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Hobbies") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("event_browser") },
            icon = { Icon(Icons.Default.Search, contentDescription = "Events") },
            label = { Text("Events") }
        )
    }
}