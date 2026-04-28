package com.example.hobbyhabit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hobbyhabit.data.local.Hobby
import com.example.hobbyhabit.ui.viewmodel.HobbyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HobbyViewModel,
    onAddHobby: () -> Unit,
    onHobbyClick: (Hobby) -> Unit,
    onProfileClick: () -> Unit
) {
    val hobbies by viewModel.hobbies.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HobbyHabit", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },

        // ✅ THIS IS CORRECT AND MUST STAY LIKE THIS
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddHobby,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Hobby")
            }
        }
    ) { innerPadding ->

        if (hobbies.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("No hobbies yet!", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Tap + to get started",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(hobbies, key = { it.id }) { hobby ->
                    HobbyCard(
                        hobby = hobby,
                        viewModel = viewModel,
                        onClick = { onHobbyClick(hobby) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HobbyCard(
    hobby: Hobby,
    viewModel: HobbyViewModel,
    onClick: () -> Unit
) {
    val count by viewModel.getWeeklyActivityCount(hobby.id)
        .collectAsState(initial = 0)

    val progress = (count.toFloat() / hobby.weeklyGoal).coerceIn(0f, 1f)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                hobby.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(4.dp))

            Text(
                "$count / ${hobby.weeklyGoal} sessions this week",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = if (progress >= 1f)
                    MaterialTheme.colorScheme.tertiary
                else
                    MaterialTheme.colorScheme.primary
            )

            if (progress >= 1f) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Goal reached!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}