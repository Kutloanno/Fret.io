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
fun EarTrainerBuilderScreen(
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedQuadrachord by remember { mutableStateOf("Full Chromatic (1-7)") }
  var cadenceFreq by remember { mutableStateOf("Play Every Round") }
  var pitchRangeSliderValue by remember { mutableFloatStateOf(1f) }

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
        text = "STUDIO / MATRIX BUILDER",
        color = MaterialTheme.colorScheme.background,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 1.sp
      )
    }

    // 2. Scale Quadrachord Selection
    RetroBox(shadowOffset = 4.dp) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = "SCALE QUADRACHORD MATRIX",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "Isolate specific sectors of the major scale for targeted training.",
          fontSize = 9.sp,
          fontFamily = FontFamily.Monospace,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val options = listOf("Lower (1-4)", "Upper (5-7)", "Full Chromatic (1-7)")
        options.forEach { option ->
          val isSelected = selectedQuadrachord == option
          val bg = if (isSelected) Color(0xFFCCCCCC) else MaterialTheme.colorScheme.surface

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(40.dp)
              .background(bg)
              .border(1.5.dp, MaterialTheme.colorScheme.outline)
              .clickable { selectedQuadrachord = option }
              .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
          ) {
            Text(
              text = option.uppercase(),
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }
    }

    // 3. Cadence Frequency Selector
    RetroBox(shadowOffset = 4.dp) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = "CADENCE FREQUENCY TIMING",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "Configure how often the functional I-IV-V-I sequence plays.",
          fontSize = 9.sp,
          fontFamily = FontFamily.Monospace,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val frequencies = listOf("Play Every Round", "Play Key Change Only", "Never Play Cadence")
        frequencies.forEach { freq ->
          val isSelected = cadenceFreq == freq
          val bg = if (isSelected) Color(0xFFCCCCCC) else MaterialTheme.colorScheme.surface

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(40.dp)
              .background(bg)
              .border(1.5.dp, MaterialTheme.colorScheme.outline)
              .clickable { cadenceFreq = freq }
              .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
          ) {
            Text(
              text = freq.uppercase(),
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }
    }

    // 4. Testing Octave Pitch Range (Placeholder Slider)
    RetroBox(shadowOffset = 4.dp) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = "TESTING OCTAVE PITCH RANGE",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          color = MaterialTheme.colorScheme.onSurface
        )

        val rangeLabel = when (pitchRangeSliderValue.toInt()) {
          1 -> "OCTAVE 3 ONLY (LOW)"
          2 -> "OCTAVE 4 ONLY (HIGH)"
          else -> "DYNAMIC MULTI-OCTAVE"
        }
        Text(
          text = "ACTIVE SPAN: $rangeLabel",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Slider(
          value = pitchRangeSliderValue,
          onValueChange = { pitchRangeSliderValue = it },
          valueRange = 1f..3f,
          steps = 1,
          colors = SliderDefaults.colors(
            thumbColor = Color(0xFFE5E5E5),
            activeTrackColor = MaterialTheme.colorScheme.outline,
            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
          ),
          modifier = Modifier.fillMaxWidth()
        )
      }
    }

    RetroButton(
      text = "Apply Matrix Filters",
      onClick = onBackClick,
      backgroundColor = Color(0xFFCCCCCC),
      modifier = Modifier.fillMaxWidth()
    )
  }
}
