package com.example.hobbyhabit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hobbyhabit.ui.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: UserViewModel,
    onBack: () -> Unit
) {
    val user by viewModel.user.collectAsState()

    // editing mode: true when no user exists yet or user taps Edit
    var isEditing by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var usernameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }

    // when user data loads from DB, pre-fill the fields
    val currentUser = user
    if (currentUser != null && !isEditing) {
        // show profile view
    } else if (currentUser == null && !isEditing) {
        isEditing = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    text = "My Profile",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                ) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                actions = {
                    if (currentUser != null && !isEditing) {
                        IconButton(onClick = {
                            username = currentUser.username
                            email = currentUser.email
                            isEditing = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(24.dp))

            // Avatar circle — shows initials if user exists, placeholder if not
            AvatarCircle(
                initials = if (currentUser != null)
                    currentUser.username.take(2).uppercase()
                else "?"
            )

            Spacer(Modifier.height(24.dp))

            if (isEditing) {
                // ── Edit / Create mode ─────────────────────────────────

                Text(
                    text = if (currentUser == null) "Create your profile" else "Edit profile",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(20.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it; usernameError = false },
                    label = { Text("Username") },
                    placeholder = { Text("e.g. pottery_lover") },
                    isError = usernameError,
                    supportingText = if (usernameError) {{ Text("Please enter a username") }} else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; emailError = false },
                    label = { Text("Email") },
                    placeholder = { Text("you@example.com") },
                    isError = emailError,
                    supportingText = if (emailError) {{ Text("Please enter a valid email") }} else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        usernameError = username.isBlank()
                        emailError = email.isBlank() || !email.contains("@")
                        if (!usernameError && !emailError) {
                            viewModel.saveUser(username.trim(), email.trim())
                            isEditing = false
                        }
                    }
                ) {
                    Text("Save Profile")
                }

                if (currentUser != null) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { isEditing = false }
                    ) {
                        Text("Cancel")
                    }
                }

            } else if (currentUser != null) {
                // ── View mode ──────────────────────────────────────────

                Text(
                    text = currentUser.username,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = currentUser.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(32.dp))

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.deleteUser()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete Profile")
                }
            }
        }
    }
}

@Composable
fun AvatarCircle(initials: String) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        )
    }
}