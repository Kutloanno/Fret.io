package com.example.guitarkaizen.ui.screens.eartraining

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.guitarkaizen.ui.screens.practice.RetroBox
import com.example.guitarkaizen.ui.screens.practice.RetroButton

@Composable
fun EarTrainerSettingsScreen(
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var cadenceVolume by remember { mutableFloatStateOf(0.8f) }
  var targetVolume by remember { mutableFloatStateOf(1.0f) }
  var selectedConvention by remember { mutableStateOf("Scale Degrees (1, 2, 3...)") }

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
          text = "< HUB",
          color = MaterialTheme.colorScheme.background,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
      }

      Spacer(modifier = Modifier.width(16.dp))

      Text(
        text = "STUDIO / SAMPLER SETTINGS",
        color = MaterialTheme.colorScheme.background,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 1.sp
      )
    }

    // 2. Sampler Volume Sliders
    RetroBox(shadowOffset = 4.dp) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Text(
          text = "SAMPLER VOLUME CONSOLE",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          color = MaterialTheme.colorScheme.onSurface
        )

        // Cadence Volume Control
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "CADENCE VOLUME: ${(cadenceVolume * 100).toInt()}%",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(4.dp))
          Slider(
            value = cadenceVolume,
            onValueChange = { cadenceVolume = it },
            colors = SliderDefaults.colors(
              thumbColor = Color(0xFFCCCCCC),
              activeTrackColor = MaterialTheme.colorScheme.outline,
              inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
          )
        }

        // Target Volume Control
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "TARGET DEGREE VOLUME: ${(targetVolume * 100).toInt()}%",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(4.dp))
          Slider(
            value = targetVolume,
            onValueChange = { targetVolume = it },
            colors = SliderDefaults.colors(
              thumbColor = Color(0xFFCCCCCC),
              activeTrackColor = MaterialTheme.colorScheme.outline,
              inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }

    // 3. Nomenclature / Naming Conventions Toggle
    RetroBox(shadowOffset = 4.dp) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = "NOMENCLATURE CONFIGURATIONS",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "Select your preferred notation style for scale degree guesses.",
          fontSize = 9.sp,
          fontFamily = FontFamily.Monospace,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val conventions = listOf(
          "Scale Degrees (1, 2, 3...)",
          "Pitch Intervals (Root, M2, M3...)",
          "Solfege (Do, Re, Mi...)"
        )
        conventions.forEach { convention ->
          val isSelected = selectedConvention == convention
          val bg = if (isSelected) Color(0xFFCCCCCC) else MaterialTheme.colorScheme.surface

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(40.dp)
              .background(bg)
              .border(1.5.dp, MaterialTheme.colorScheme.outline)
              .clickable { selectedConvention = convention }
              .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
          ) {
            Text(
              text = convention.uppercase(),
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }
    }

    RetroButton(
      text = "Apply Settings & Exit",
      onClick = onBackClick,
      backgroundColor = Color(0xFFCCCCCC),
      modifier = Modifier.fillMaxWidth()
    )
  }
}
