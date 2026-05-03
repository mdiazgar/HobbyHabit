package com.example.hobbyhabit.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val hobbies      by viewModel.hobbies.collectAsState()
    val allSessions  by viewModel.allSessions.collectAsState()
    val allEvents    by viewModel.allEvents.collectAsState()
    val searchQuery  by viewModel.searchQuery.collectAsState()
    val sortOrder    by viewModel.sortOrder.collectAsState()

    var showSortMenu       by remember { mutableStateOf(false) }
    var showSearchBar      by remember { mutableStateOf(false) }
    var hobbyToDelete      by remember { mutableStateOf<Hobby?>(null) }

    // Filter + sort hobbies
    val weeklyCountMap = remember(allSessions, allEvents) {
        val weekStart = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
        val sessionCounts = allSessions.groupBy { it.hobbyId }
            .mapValues { (_, s) -> s.count { it.timestamp >= weekStart } }
        val eventCounts = allEvents.groupBy { it.hobbyId }
            .mapValues { (_, e) -> e.count { it.dateTime >= weekStart } }
        hobbies.associate { h ->
            h.id to ((sessionCounts[h.id] ?: 0) + (eventCounts[h.id] ?: 0))
        }
    }

    val filteredHobbies = hobbies
        .filter { it.name.contains(searchQuery, ignoreCase = true) }
        .let { list ->
            when (sortOrder) {
                HobbyViewModel.SortOrder.NAME_ASC    -> list.sortedBy { it.name }
                HobbyViewModel.SortOrder.MOST_ACTIVE -> list.sortedByDescending { weeklyCountMap[it.id] ?: 0 }
                HobbyViewModel.SortOrder.DEFAULT     -> list
            }
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HobbyHabit") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    // Search toggle
                    IconButton(onClick = {
                        showSearchBar = !showSearchBar
                        if (!showSearchBar) viewModel.setSearchQuery("")
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    // Sort menu
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Default") },
                                onClick = {
                                    viewModel.setSortOrder(HobbyViewModel.SortOrder.DEFAULT)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("A → Z") },
                                onClick = {
                                    viewModel.setSortOrder(HobbyViewModel.SortOrder.NAME_ASC)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Most Active") },
                                onClick = {
                                    viewModel.setSortOrder(HobbyViewModel.SortOrder.MOST_ACTIVE)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                    // Profile
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddHobby) {
                Icon(Icons.Default.Add, contentDescription = "Add Hobby")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search bar
            item {
                AnimatedVisibility(visible = showSearchBar) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        label = { Text("Search hobbies") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null,
                                modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            if (filteredHobbies.isEmpty()) {
                item {
                    if (searchQuery.isNotBlank()) {
                        EmptyState(
                            emoji    = "🔍",
                            title    = "No results for \"$searchQuery\"",
                            subtitle = "Try a different search term"
                        )
                    } else {
                        EmptyState(
                            emoji    = "🌱",
                            title    = "No hobbies yet",
                            subtitle = "Tap + to add your first hobby and start building great habits!"
                        )
                    }
                }
            } else {
                items(filteredHobbies, key = { it.id }) { hobby ->
                    val weeklyCount = weeklyCountMap[hobby.id] ?: 0
                    val sessions    = allSessions.filter { it.hobbyId == hobby.id }
                    val events      = allEvents.filter { it.hobbyId == hobby.id }
                    val streak      = viewModel.currentStreak(sessions, events, hobby.weeklyGoal)

                    HobbyCard(
                        hobby        = hobby,
                        weeklyCount  = weeklyCount,
                        streak       = streak,
                        onClick      = { onHobbyClick(hobby) },
                        onDelete     = { hobbyToDelete = hobby }
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    hobbyToDelete?.let { hobby ->
        val sessionCount = allSessions.count { it.hobbyId == hobby.id }
        val eventCount   = allEvents.count { it.hobbyId == hobby.id }
        AlertDialog(
            onDismissRequest = { hobbyToDelete = null },
            title = { Text("Delete \"${hobby.name}\"?") },
            text  = {
                Text(
                    "This will permanently delete $sessionCount session(s) and " +
                            "$eventCount event(s) linked to this hobby. This cannot be undone."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteHobby(hobby)
                    hobbyToDelete = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { hobbyToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun HobbyCard(
    hobby: Hobby,
    weeklyCount: Int,
    streak: Int,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val progress = (weeklyCount.toFloat() / hobby.weeklyGoal.coerceAtLeast(1)).coerceIn(0f, 1f)
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(hobby.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    Text(hobby.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Streak badge
                if (streak > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text("🔥", fontSize = 16.sp)
                        Spacer(Modifier.width(2.dp))
                        Text(
                            "$streak",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                // ⋮ menu
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp))
                            },
                            onClick = { menuExpanded = false; onDelete() }
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color    = if (progress >= 1f) MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(4.dp))

            Text(
                "$weeklyCount / ${hobby.weeklyGoal} this week",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}