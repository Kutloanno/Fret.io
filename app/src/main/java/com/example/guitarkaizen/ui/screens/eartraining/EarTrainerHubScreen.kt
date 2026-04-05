package com.example.guitarkaizen.ui.screens.eartraining

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.example.guitarkaizen.EarTrainerArena
import com.example.guitarkaizen.EarTrainerBuilder
import com.example.guitarkaizen.EarTrainerSettings
import com.example.guitarkaizen.ui.screens.practice.PracticeCard
import com.example.guitarkaizen.ui.screens.practice.RetroBox

@Composable
fun EarTrainerHubScreen(
  onBackClick: () -> Unit,
  onNavigate: (NavKey) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp)
  ) {
    // 1. Navigation Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
        .border(2.dp, MaterialTheme.colorScheme.outline)
        .background(MaterialTheme.colorScheme.outline)
        .padding(horizontal = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .border(1.5.dp, MaterialTheme.colorScheme.background)
          .background(MaterialTheme.colorScheme.outline)
          .clickable(onClick = onBackClick)
          .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "< BACK",
          color = MaterialTheme.colorScheme.background,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
      }

      Spacer(modifier = Modifier.width(16.dp))

      Text(
        text = "STUDIO / EAR TRAINING",
        color = MaterialTheme.colorScheme.background,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 1.sp
      )
    }

    // 2. Welcome Banner Console
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .border(2.dp, MaterialTheme.colorScheme.outline)
        .background(MaterialTheme.colorScheme.outline)
        .padding(16.dp)
    ) {
      Column {
        Text(
          text = "FUNCTIONAL EAR TRAINING",
          color = MaterialTheme.colorScheme.background,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "DEVELOP PERFECT TONAL GRAVITY ASSOCIATION.",
          color = MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
          fontSize = 9.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
      }
    }

    Text(
      text = "STUDIO LAB UTILITIES",
      fontSize = 13.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace,
      color = MaterialTheme.colorScheme.onSurface
    )

    // 3. Chunky Practice navigation cards
    PracticeCard(
      title = "Basic Arena",
      description = "Diad scale degree recognition with cadence reinforcement.",
      icon = Icons.Default.PlayArrow,
      accentColor = Color(0xFFCCCCCC),
      onClick = { onNavigate(EarTrainerArena) }
    )

    PracticeCard(
      title = "Advanced Arena",
      description = "Chromatic vocal interval jumps and complex pitch locking.",
      icon = Icons.Default.Star,
      accentColor = Color(0xFFE5E5E5),
      onClick = { onNavigate(EarTrainerArena) }
    )

    PracticeCard(
      title = "Melodic Dictations",
      description = "Identify and chart complete melodic paths on a virtual staff.",
      icon = Icons.Default.Info,
      accentColor = Color(0xFFFAF6F0),
      onClick = {}
    )

    PracticeCard(
      title = "Chord Sampler",
      description = "Recognize root-position triads, intervals, and jazz extensions.",
      icon = Icons.Default.PlayArrow,
      accentColor = Color(0xFFCCCCCC),
      onClick = {}
    )

    PracticeCard(
      title = "Custom Matrix Builder",
      description = "Design custom scale, cadence, and boundary training filters.",
      icon = Icons.Default.Settings,
      accentColor = Color(0xFFFAF6F0),
      onClick = { onNavigate(EarTrainerBuilder) }
    )

    PracticeCard(
      title = "Sampler Settings",
      description = "Configure custom volume, naming, and sampling templates.",
      icon = Icons.Default.Settings,
      accentColor = Color(0xFFE5E5E5),
      onClick = { onNavigate(EarTrainerSettings) }
    )
  }
}
