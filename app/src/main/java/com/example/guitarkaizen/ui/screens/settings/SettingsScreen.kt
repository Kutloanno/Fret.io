package com.example.guitarkaizen.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.guitarkaizen.theme.ThemeConfig
import com.example.guitarkaizen.ui.screens.practice.RetroBox
import com.example.guitarkaizen.ui.screens.practice.RetroButton

@Composable
fun SettingsScreen(
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var isStereoEnabled by remember { mutableStateOf(true) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(24.dp)
  ) {
    // 1. Navigation Header Box with Retro "BACK" Action
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
        .border(2.dp, MaterialTheme.colorScheme.outline)
        .background(MaterialTheme.colorScheme.outline)
        .padding(horizontal = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Retro Back Button (0.dp shapes and offsets)
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
          fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
      }

      Spacer(modifier = Modifier.width(16.dp))

      Text(
        text = "PRACTICE / SETTINGS",
        color = MaterialTheme.colorScheme.background,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
        letterSpacing = 1.sp
      )
    }

    // 2. Active Mode Display & Dynamic Switcher (RetroBox)
    RetroBox(shadowOffset = 4.dp) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Text(
          text = "VISUAL ENVIRONMENT",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
          color = MaterialTheme.colorScheme.onSurface
        )

        // Large Readout Panel
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, MaterialTheme.colorScheme.outline)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 14.dp),
          contentAlignment = Alignment.Center
        ) {
          val modeName = if (ThemeConfig.isDarkTheme) "MONOCHROME DARK" else "VINTAGE BEIGE LIGHT"
          Text(
            text = modeName,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        // Chunky Manual Theme Toggle Switch
        val activeModeColor = if (ThemeConfig.isDarkTheme) Color(0xFFCCCCCC) else Color(0xFFE5E5E5)
        RetroButton(
          text = if (ThemeConfig.isDarkTheme) "Set Light Mode" else "Set Dark Mode",
          onClick = { ThemeConfig.isDarkTheme = !ThemeConfig.isDarkTheme },
          backgroundColor = activeModeColor,
          modifier = Modifier.fillMaxWidth().height(48.dp)
        )
      }
    }

    // 3. Audio Diagnostic Toggles (RetroBox)
    RetroBox(shadowOffset = 4.dp) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Text(
          text = "AUDIO CHANNELS",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
          color = MaterialTheme.colorScheme.onSurface
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "STEREO BASS ENHANCEMENT",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Dithered sub-frequency mechanical balance",
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          // Mechanical flat switch box
          val toggleColor = if (isStereoEnabled) Color(0xFFCCCCCC) else MaterialTheme.colorScheme.surfaceVariant
          Box(
            modifier = Modifier
              .width(60.dp)
              .height(30.dp)
              .border(1.5.dp, MaterialTheme.colorScheme.outline)
              .background(toggleColor)
              .clickable { isStereoEnabled = !isStereoEnabled },
            contentAlignment = if (isStereoEnabled) Alignment.CenterEnd else Alignment.CenterStart
          ) {
            Box(
              modifier = Modifier
                .padding(horizontal = 4.dp)
                .size(20.dp)
                .border(1.5.dp, MaterialTheme.colorScheme.outline)
                .background(Color.White)
            )
          }
        }
      }
    }

    // 4. Device Engine Diagnostics (RetroBox)
    RetroBox(shadowOffset = 4.dp) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = "KAIZEN ENGINE DIAGNOSTICS",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
          color = MaterialTheme.colorScheme.onSurface
        )

        val textStyle = MaterialTheme.typography.bodyMedium.copy(
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Column(
          modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, MaterialTheme.colorScheme.outline)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text("PLATFORM VERSION: ANDROID KAIZEN 1.0", style = textStyle)
          Text("COMPILER TARGET : JVM TOOLCHAIN 21", style = textStyle)
          Text("AUDIO LATENCY   : LOW LATENCY OK", style = textStyle)
          Text("SYSTEM STATUS   : INTEGRITY VERIFIED", style = textStyle)
        }
      }
    }

    // Footer Copyright Label
    Text(
      text = "KAIZEN SYSTEM 2.0.0 © 1986 POOLSUITE",
      fontSize = 9.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
      color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
      modifier = Modifier.align(Alignment.CenterHorizontally)
    )
  }
}
