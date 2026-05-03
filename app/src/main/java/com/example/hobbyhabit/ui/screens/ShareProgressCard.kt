package com.example.hobbyhabit.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.hobbyhabit.data.local.Hobby
import com.example.hobbyhabit.data.local.Session
import java.io.File
import java.io.FileOutputStream

// ── Shareable card composable ───────────────────────────────────────────────

@Composable
fun ShareProgressCard(
    hobby: Hobby,
    sessions: List<Session>,
    weeklyCount: Int,
    streak: Int,
    onShare: () -> Unit
) {
    val totalMins = sessions.sumOf { it.durationMinutes }
    val progress  = (weeklyCount.toFloat() / hobby.weeklyGoal.coerceAtLeast(1)).coerceIn(0f, 1f)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // The card that will be shared
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape     = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors    = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("HobbyHabit", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        Text(hobby.name, style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(hobby.category, style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    }
                    Text(categoryEmoji(hobby.category), fontSize = 40.sp)
                }

                Spacer(Modifier.height(20.dp))

                // Streak
                if (streak > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔥", fontSize = 24.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("$streak week streak",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // Weekly progress bar
                Text("This week: $weeklyCount / ${hobby.weeklyGoal} sessions",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color    = if (progress >= 1f) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(16.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MiniStat("📋", "${sessions.size}", "Sessions")
                    MiniStat("⏱️", formatMins(totalMins), "Total time")
                }

                Spacer(Modifier.height(16.dp))

                // Footer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tracked with HobbyHabit 🎯",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick   = onShare,
            modifier  = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
        ) {
            Text("Share Progress 🚀")
        }
    }
}

@Composable
private fun MiniStat(emoji: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 20.sp)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
    }
}

private fun formatMins(mins: Int): String = when {
    mins < 60 -> "${mins}m"
    else      -> "${mins / 60}h ${mins % 60}m"
}

// ── Share intent helper ─────────────────────────────────────────────────────

fun shareTextProgress(
    context: Context,
    hobbyName: String,
    streak: Int,
    weeklyCount: Int,
    weeklyGoal: Int,
    totalSessions: Int
) {
    val streakText = if (streak > 0) "🔥 $streak week streak!\n" else ""
    val text = """
🎯 HobbyHabit Progress

$hobbyName
${streakText}This week: $weeklyCount / $weeklyGoal sessions
Total sessions: $totalSessions

Tracked with HobbyHabit 📱
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type    = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share your progress"))
}