package com.example.hobbyhabit.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.hobbyhabit.ui.viewmodel.HobbyViewModel

// Map of display name → Ticketmaster classificationName
val HOBBY_CATEGORIES = linkedMapOf(
    "Theatre & Arts" to "Arts & Theatre",
    "Music" to "Music",
    "Comedy" to "Comedy",
    "Dance" to "Dance & Performance Arts",
    "Film & Cinema" to "Film",
    "Sports" to "Sports",
    "Family & Kids" to "Family",
    "Education" to "Education"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddHobbyScreen(viewModel: HobbyViewModel, onBack: () -> Unit) {
    var hobbyName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }  // display name
    var weeklyGoal by remember { mutableStateOf("3") }
    var nameError by remember { mutableStateOf(false) }
    var categoryError by remember { mutableStateOf(false) }

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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hobby name
            Text("What would you like to call this hobby?",
                style = MaterialTheme.typography.bodyLarge)

            OutlinedTextField(
                value = hobbyName,
                onValueChange = { hobbyName = it; nameError = false },
                label = { Text("Hobby Name") },
                placeholder = { Text("e.g. My Theatre Hobby") },
                isError = nameError,
                supportingText = if (nameError) {{ Text("Please enter a name") }} else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Category picker
            Text("Choose a category", style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold)
            Text("This helps us find relevant events near you",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            AnimatedVisibility(visible = categoryError) {
                Text("Please select a category",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error)
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HOBBY_CATEGORIES.keys.forEach { displayName ->
                    FilterChip(
                        selected = selectedCategory == displayName,
                        onClick = {
                            selectedCategory = displayName
                            categoryError = false
                        },
                        label = { Text(displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            // Weekly goal
            Text("Weekly goal", style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold)

            OutlinedTextField(
                value = weeklyGoal,
                onValueChange = { if (it.all(Char::isDigit)) weeklyGoal = it },
                label = { Text("Sessions per week") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    nameError = hobbyName.isBlank()
                    categoryError = selectedCategory == null
                    if (!nameError && !categoryError) {
                        val ticketmasterCategory = HOBBY_CATEGORIES[selectedCategory] ?: selectedCategory!!
                        val goal = weeklyGoal.toIntOrNull()?.coerceAtLeast(1) ?: 3
                        viewModel.addHobby(
                            name = hobbyName.trim(),
                            category = ticketmasterCategory,
                            weeklyGoal = goal
                        )
                        onBack()
                    }
                }
            ) {
                Text("Add Hobby")
            }
        }
    }
}
