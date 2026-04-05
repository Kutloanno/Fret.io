package com.example.guitarkaizen.ui.screens.eartraining

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.guitarkaizen.ui.screens.practice.RetroBox
import com.example.guitarkaizen.ui.screens.practice.RetroButton
import com.example.guitarkaizen.ui.screens.tuner.RetroDropdown

@Composable
fun EarTrainerArenaScreen(
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: FunctionalEarViewModel = viewModel()
) {
  val context = LocalContext.current
  val state by viewModel.state.collectAsState()

  // Initialize samplers safely at screen startup
  LaunchedEffect(Unit) {
    viewModel.initEngine(context)
    viewModel.playCadence() // Auto play first cadence
  }

  var lastCorrectResult by remember { mutableStateOf<Boolean?>(null) }
  var lastGuessedLabel by remember { mutableStateOf("") }

  val keysList = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
  val chromaticDegrees = listOf(
    Pair("1", 1), Pair("b2", -1), Pair("2", 2),
    Pair("b3", -2), Pair("3", 3), Pair("4", 4),
    Pair("b5", -5), Pair("5", 5), Pair("b6", -6),
    Pair("6", 6), Pair("b7", -7), Pair("7", 7)
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp)
  ) {
    // A. Navigation Header
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
        text = "STUDIO / TONAL ARENA",
        color = MaterialTheme.colorScheme.background,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 1.sp
      )
    }

    // B. Stats & Key Configurations
    RetroBox(shadowOffset = 3.dp) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
      ) {
        Column {
          Text(
            text = "ARENA SCORE: ${state.score}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Diatonic Degree Identification",
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        RetroDropdown(
          label = "Key Lock",
          options = keysList,
          selectedOption = state.currentKey,
          onOptionSelected = { viewModel.setKey(it) },
          optionToString = { it },
          modifier = Modifier.width(96.dp)
        )
      }
    }

    // C. The Sampler Sequencer Card (Play Controls)
    RetroBox(shadowOffset = 4.dp) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "CADENCE SEQUENCER CONTROL",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.fillMaxWidth()
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Play Cadence massive button
          val cadenceText = if (state.isPlayingCadence) "PLAYING..." else "PLAY CADENCE"
          val cadenceColor = if (state.isPlayingCadence) Color(0xFFE5E5E5) else Color(0xFFCCCCCC)

          RetroButton(
            text = cadenceText,
            onClick = { if (!state.isPlayingCadence) viewModel.playCadence() },
            backgroundColor = cadenceColor,
            modifier = Modifier.weight(1.5f)
          )

          // Play Target Note button
          RetroButton(
            text = "REPLAY TARGET",
            onClick = { if (!state.isPlayingCadence) viewModel.replayTargetNote() },
            backgroundColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.weight(1f)
          )
        }

        // Status banner
        val statusText = when {
          state.isPlayingCadence -> "LISTENING TO I-IV-V-I CADENCE..."
          lastCorrectResult == true -> "CORRECT ANSWER! (+1 SCORE)"
          lastCorrectResult == false -> "WRONG DEGREE GUESSED ($lastGuessedLabel). TRY AGAIN!"
          else -> "CADENCE LOADED. READY TO GUESS!"
        }
        val statusBg = when {
          state.isPlayingCadence -> Color(0xFFFAF6F0)
          lastCorrectResult == true -> Color(0xFFCCCCCC)
          lastCorrectResult == false -> Color(0xFFE5E5E5)
          else -> MaterialTheme.colorScheme.surfaceVariant
        }

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, MaterialTheme.colorScheme.outline)
            .background(statusBg)
            .padding(vertical = 10.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = statusText,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = Color.Black
          )
        }
      }
    }

    // D. 12-Button Chromatic Guessing Grid (Disabled while playing)
    RetroBox(shadowOffset = 4.dp) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = "IDENTIFY SCALE DEGREE:",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          color = MaterialTheme.colorScheme.onSurface
        )

        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          val rows = chromaticDegrees.chunked(3)
          rows.forEach { rowPairs ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              rowPairs.forEach { (label, degreeVal) ->
                val isButtonEnabled = !state.isPlayingCadence
                val buttonBg = if (isButtonEnabled) MaterialTheme.colorScheme.surface else Color(0xFFE0E0E0).copy(alpha = 0.5f)

                RetroButton(
                  text = label,
                  onClick = {
                    if (isButtonEnabled) {
                      val isCorrect = viewModel.submitGuess(degreeVal)
                      lastCorrectResult = isCorrect
                      lastGuessedLabel = label
                    }
                  },
                  backgroundColor = buttonBg,
                  modifier = Modifier.weight(1f)
                )
              }
            }
          }
        }
      }
    }
  }
}
