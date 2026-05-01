package com.example.hobbyhabit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import com.example.hobbyhabit.data.local.Hobby
import com.example.hobbyhabit.ui.viewmodel.HobbyViewModel
import com.example.hobbyhabit.ui.theme.BlushPink
import com.example.hobbyhabit.ui.theme.CreamPeach
import com.example.hobbyhabit.ui.theme.SageGreen
import com.example.hobbyhabit.ui.theme.WarmGray

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
                title = {
                    Text(
                        "HobbyHabit",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,  // DarkSlate
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer, // CreamPeach
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer // CreamPeach
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddHobby,
                containerColor = MaterialTheme.colorScheme.primary, // DeepSage
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Hobby")
            }
        },
        containerColor = MaterialTheme.colorScheme.background // SoftCream screen bg
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
                    Text(
                        "No hobbies yet!",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground, // DarkSlate
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Tap + to get started",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant // SlateBlue
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
                        onClick = { onHobbyClick(hobby) },
                        onDeleteClick = { viewModel.deleteHobby(hobby) }
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
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val count by viewModel.getWeeklyActivityCount(hobby.id).collectAsState(initial = 0)
    val progress = (count.toFloat() / hobby.weeklyGoal).coerceIn(0f, 1f)
    val progressPercent = (progress * 100).toInt()
    val isGoalReached = progress >= 1f

    // Card color shifts based on progress state

    val cardColor = when {
        isGoalReached -> BlushPink
        progress <= 0f -> WarmGray
        else -> CreamPeach
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = hobby.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface // DarkSlate
                )
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete Hobby",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant // SlateBlue
                    )
                }
            }

            Spacer(Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "$count / ${hobby.weeklyGoal} sessions this week",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant // SlateBlue
                )
                Text(
                    if (isGoalReached) "100%" else "$progressPercent%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = if (isGoalReached)
                        MaterialTheme.colorScheme.tertiary  // BlushPink accent
                    else
                        MaterialTheme.colorScheme.primary   // DeepSage
                )
            }

            Spacer(Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,       // DeepSage always
                    trackColor = SageGreen.copy(alpha = 0.25f)
            )

            if (isGoalReached) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Goal reached!",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}