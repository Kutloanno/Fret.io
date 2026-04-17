package com.example.guitarkaizen.ui.screens.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.example.guitarkaizen.Metronome
import com.example.guitarkaizen.Tuner
import com.example.guitarkaizen.NoteFinder
import com.example.guitarkaizen.EarTrainerHub
import com.example.guitarkaizen.ChordLibrary
import com.example.guitarkaizen.FretboardExplorer




@Composable
fun PracticeScreen(
  onNavigate: (NavKey) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(24.dp)
  ) {
    // 1. Welcome Header Banner (Solid Black Box)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .border(2.dp, MaterialTheme.colorScheme.outline)
        .background(MaterialTheme.colorScheme.outline)
        .padding(16.dp)
    ) {
      Column {
        Text(
          text = "PRACTICE ROOM",
          color = MaterialTheme.colorScheme.background, // Vintage sand text
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "CHRONIC TIMING AND METRIC DRILLS.",
          color = MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold
        )
      }
    }

    // 2. Daily Practice Streak Box (RetroBox)
    RetroBox(shadowOffset = 4.dp) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "DAILY PRACTICE STREAK",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Practice today to lock your score!",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(10.dp))

          // Flat custom progress track
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .border(1.5.dp, MaterialTheme.colorScheme.outline)
              .height(10.dp)
              .background(MaterialTheme.colorScheme.surfaceVariant)
          ) {
            Box(
              modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.6f)
                .background(Color(0xFFCCCCCC)) // Medium grey accent
            )
          }
        }
        Spacer(modifier = Modifier.width(16.dp))

        // Dithered counters
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .border(1.5.dp, MaterialTheme.colorScheme.outline)
            .background(Color(0xFFE5E5E5))
            .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
          Text(
            text = "05",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "DAYS",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }

    Text(
      text = "PRACTICE UTILITIES",
      fontSize = 15.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )

    // 3. Practice Cards Directories (With dedicated metronome click trigger)
    PracticeCard(
      title = "Metronome & Rhythms",
      description = "Internalize steady timing with accents and custom signatures.",
      icon = Icons.Default.Settings,
      accentColor = Color(0xFFE5E5E5),
      onClick = { onNavigate(Metronome) }
    )

    PracticeCard(
      title = "Fretboard Note Finder",
      description = "Memorize notes across the neck using interactive visualization.",
      icon = Icons.Default.Info,
      accentColor = Color(0xFFCCCCCC),
      onClick = { onNavigate(NoteFinder) }
    )

    PracticeCard(
      title = "Chord Finder & Library",
      description = "Lookup open, bar, and jazz chord fingerings instantly.",
      icon = Icons.Default.PlayArrow,
      accentColor = Color(0xFFFAF6F0),
      onClick = { onNavigate(ChordLibrary) }
    )

    PracticeCard(
      title = "Fretboard Explorer",
      description = "Visualize scale intervals and arpeggios across the fretboard.",
      icon = Icons.Default.Info,
      accentColor = Color(0xFFE5E5E5),
      onClick = { onNavigate(FretboardExplorer) }
    )



    PracticeCard(
      title = "Ear Training Studio",
      description = "Develop perfect pitch recognizing intervals and patterns.",
      icon = Icons.Default.Star,
      accentColor = Color(0xFFCCCCCC),
      onClick = { onNavigate(EarTrainerHub) }
    )

    PracticeCard(
      title = "Guitar Tuner",
      description = "High-precision, zero-latency real-time pitch detection.",
      icon = Icons.Default.PlayArrow,
      accentColor = Color(0xFFCCCCCC),
      onClick = { onNavigate(Tuner) }
    )
  }
}

@Composable
fun PracticeCard(
  title: String,
  description: String,
  icon: ImageVector,
  accentColor: Color,
  onClick: () -> Unit
) {
  RetroBox(shadowOffset = 3.dp, backgroundColor = MaterialTheme.colorScheme.surface) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .border(1.5.dp, MaterialTheme.colorScheme.outline)
          .background(accentColor),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = MaterialTheme.colorScheme.outline,
          modifier = Modifier.size(20.dp)
        )
      }
      Spacer(modifier = Modifier.width(16.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title.uppercase(),
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = description,
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      Spacer(modifier = Modifier.width(8.dp))

      RetroButton(
        text = "Start",
        onClick = onClick,
        backgroundColor = Color(0xFFE5E5E5),
        modifier = Modifier.width(68.dp)
      )
    }
  }
}

// Global RetroBox Layout Helper
@Composable
fun RetroBox(
  modifier: Modifier = Modifier,
  borderColor: Color = MaterialTheme.colorScheme.outline,
  borderWidth: Dp = 2.dp,
  shadowOffset: Dp = 4.dp,
  backgroundColor: Color = MaterialTheme.colorScheme.surface,
  content: @Composable () -> Unit
) {
  Box(modifier = modifier) {
    Box(
      modifier = Modifier
        .matchParentSize()
        .offset(x = shadowOffset, y = shadowOffset)
        .background(MaterialTheme.colorScheme.outline)
    )
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(backgroundColor)
        .border(borderWidth, borderColor)
        .padding(14.dp)
    ) {
      content()
    }
  }
}

// Global RetroButton Helper
@Composable
fun RetroButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  backgroundColor: Color = Color(0xFFE5E5E5),
  shadowOffset: Dp = 3.dp
) {
  Box(
    modifier = modifier
      .clickable(onClick = onClick)
  ) {
    Box(
      modifier = Modifier
        .matchParentSize()
        .offset(x = shadowOffset, y = shadowOffset)
        .background(MaterialTheme.colorScheme.outline)
    )
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(backgroundColor)
        .border(1.5.dp, MaterialTheme.colorScheme.outline)
        .padding(vertical = 6.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = text.uppercase(),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
        color = MaterialTheme.colorScheme.onBackground
      )
    }
  }
}
