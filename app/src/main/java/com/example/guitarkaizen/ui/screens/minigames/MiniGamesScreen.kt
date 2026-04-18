package com.example.guitarkaizen.ui.screens.minigames

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.example.guitarkaizen.ScaleDegreeGame
import com.example.guitarkaizen.ui.screens.practice.RetroBox
import com.example.guitarkaizen.ui.screens.practice.RetroButton

@Composable
fun MiniGamesScreen(
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
    // 1. Retro Header Box (Solid Black)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .border(2.dp, MaterialTheme.colorScheme.outline)
        .background(MaterialTheme.colorScheme.outline)
        .padding(16.dp)
    ) {
      Column {
        Text(
          text = "THEORY ARCADE",
          color = MaterialTheme.colorScheme.background, // sand text
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "GAMIFIED MUSIC THEORY CHALLENGES.",
          color = MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold
        )
      }
    }

    // 2. Gamification XP progress box (RetroBox)
    RetroBox(shadowOffset = 4.dp) {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            // Flat retro star container
            Box(
              modifier = Modifier
                .size(36.dp)
                .border(1.5.dp, MaterialTheme.colorScheme.outline)
                .background(Color(0xFFE5E5E5)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(18.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "KAIZEN LEVEL 05",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Title: Pentatonic Master",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
          Text(
            text = "4,250 XP",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.outline
          )
        }
        Spacer(modifier = Modifier.height(14.dp))
        
        // Custom progress bar
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
              .fillMaxWidth(0.75f) // 75% progress
              .background(Color(0xFFCCCCCC)) // Medium grey accent
          )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("750 XP to next level", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text("LVL 06", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    }

    Text(
      text = "AVAILABLE MINI-GAMES",
      fontSize = 15.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )

    // 3. Mini-Games list (Wiring play button to target ScaleDegreeGameScreen)
    GameCard(
      title = "Fretboard Speedrun",
      description = "Locate randomized target notes on the neck before time runs out.",
      highScore = "Score: 24 (Hard)",
      icon = Icons.Default.PlayArrow,
      accentColor = Color(0xFFCCCCCC),
      onClick = { onNavigate(ScaleDegreeGame) }
    )

  }
}

@Composable
fun GameCard(
  title: String,
  description: String,
  highScore: String,
  icon: ImageVector,
  accentColor: Color,
  onClick: () -> Unit
) {
  RetroBox(shadowOffset = 3.dp) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Solid flat square icon container
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
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = highScore,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.outline
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
      
      // Retro play button
      RetroButton(
        text = "Play",
        onClick = onClick,
        backgroundColor = Color(0xFFE5E5E5),
        modifier = Modifier.width(68.dp)
      )
    }
  }
}
