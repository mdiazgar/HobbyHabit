package com.example.hobbyhabit.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class OnboardingPage(
    val emoji: String,
    val title: String,
    val description: String,
    val backgroundColor: androidx.compose.ui.graphics.Color
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val primary   = MaterialTheme.colorScheme.primaryContainer
    val secondary = MaterialTheme.colorScheme.secondaryContainer
    val tertiary  = MaterialTheme.colorScheme.tertiaryContainer

    val pages = listOf(
        OnboardingPage(
            emoji           = "🎯",
            title           = "Track Your Hobbies",
            description     = "Add hobbies you love and set a weekly goal. Log sessions to build your streak and stay consistent.",
            backgroundColor = primary
        ),
        OnboardingPage(
            emoji           = "🎟️",
            title           = "Discover Local Events",
            description     = "Find real Ticketmaster events near you based on your hobbies — concerts, theatre, sports and more.",
            backgroundColor = secondary
        ),
        OnboardingPage(
            emoji           = "🔥",
            title           = "Build Your Streak",
            description     = "Hit your weekly goal every week to grow your streak. Check your stats and see how far you've come.",
            backgroundColor = tertiary
        )
    )

    var currentPage by remember { mutableIntStateOf(0) }
    val page = pages[currentPage]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Illustration area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.55f)
                .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                .background(page.backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(page.emoji, fontSize = 96.sp)
            }
        }

        // Text content
        Column(
            modifier = Modifier
                .weight(0.45f)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(24.dp))
                Text(
                    text       = page.title,
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text      = page.description,
                    style     = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Dot indicators
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pages.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .size(if (index == currentPage) 20.dp else 8.dp, 8.dp)
                                .clip(if (index == currentPage) RoundedCornerShape(4.dp) else CircleShape)
                                .background(
                                    if (index == currentPage) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant
                                )
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Buttons
                if (currentPage < pages.size - 1) {
                    Button(
                        onClick   = { currentPage++ },
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = RoundedCornerShape(12.dp)
                    ) {
                        Text("Next", style = MaterialTheme.typography.labelLarge)
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick  = onFinish,
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp)
                    ) {
                        Text("Skip")
                    }
                } else {
                    Button(
                        onClick  = onFinish,
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp)
                    ) {
                        Text("Get Started 🚀", style = MaterialTheme.typography.labelLarge)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}