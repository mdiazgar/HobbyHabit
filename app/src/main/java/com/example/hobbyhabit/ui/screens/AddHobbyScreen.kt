package com.example.hobbyhabit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.hobbyhabit.ui.viewmodel.HobbyViewModel

//Adding a new hobby page
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHobbyScreen(viewModel: HobbyViewModel, onBack: () -> Unit) {
    var hobbyName by remember { mutableStateOf("") }
    var weeklyGoal by remember { mutableStateOf("3") }
    var nameError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Hobby") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "What hobby would you like to track?",
                style = MaterialTheme.typography.bodyLarge
            )

            OutlinedTextField(
                value = hobbyName,
                onValueChange = { hobbyName = it; nameError = false },
                label = { Text("Hobby Name") },
                placeholder = { Text("e.g. Pottery, Photography, Dance") },
                isError = nameError,
                supportingText = if (nameError) {{ Text("Please enter a hobby name") }} else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = weeklyGoal,
                onValueChange = { if (it.all(Char::isDigit)) weeklyGoal = it },
                label = { Text("Weekly Goal (sessions)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.weight(1f))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (hobbyName.isBlank()) {
                        nameError = true
                    } else {
                        val goal = weeklyGoal.toIntOrNull()?.coerceAtLeast(1) ?: 3
                        viewModel.addHobby(hobbyName.trim(), goal)
                        onBack()
                    }
                }
            ) {
                Text("Add Hobby")
            }
        }
    }
}
