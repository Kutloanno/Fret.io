package com.example.guitarkaizen.ui.screens.metronome

import android.media.AudioAttributes
import android.media.SoundPool
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.guitarkaizen.R
import com.example.guitarkaizen.ui.screens.practice.RetroBox
import com.example.guitarkaizen.ui.screens.practice.RetroButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

@Composable
fun MetronomeScreen(
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  // 1. Saveable States
  var isPlaying by rememberSaveable { mutableStateOf(false) }
  var bpm by rememberSaveable { mutableStateOf(100) } // Default 100 BPM
  var timeSignature by rememberSaveable { mutableStateOf("4/4") } // Default 4/4 measure
  var currentBeat by remember { mutableStateOf(1) } // Visual active beat tracker

  // Dynamically parse beats numerator (top number) from signature string
  val beatsPerMeasure = remember(timeSignature) {
    try {
      timeSignature.substringBefore("/").toInt()
    } catch (e: Exception) {
      4
    }
  }

  // 2. SoundPool Audio Player Initialization
  val soundPool = remember {
    val audioAttributes = AudioAttributes.Builder()
      .setUsage(AudioAttributes.USAGE_GAME)
      .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
      .build()
    SoundPool.Builder()
      .setMaxStreams(2) // Overlap sounds smoothly
      .setAudioAttributes(audioAttributes)
      .build()
  }

  // Load custom mechanical WAV files dropped by user in res/raw/
  val accentSoundId = remember {
    soundPool.load(context, R.raw.accent, 1)
  }

  val subbeatSoundId = remember {
    soundPool.load(context, R.raw.subbeat, 1)
  }

  // Release streams safely on screen disposal
  DisposableEffect(Unit) {
    onDispose {
      soundPool.release()
    }
  }

  // 3. Timing Loop with Accent Beats and Drift Compensation
  LaunchedEffect(isPlaying, bpm, timeSignature) {
    if (isPlaying) {
      val beats = beatsPerMeasure
      val intervalMs = (60000.0 / bpm).toLong()
      currentBeat = 1 // Start beat 1

      withContext(Dispatchers.Default) {
        while (isActive) {
          val startTime = System.currentTimeMillis()

          // Accent beat 1 differently from sub-beats
          val activeSoundId = if (currentBeat == 1) accentSoundId else subbeatSoundId
          soundPool.play(activeSoundId, 1.0f, 1.0f, 1, 0, 1.0f)

          val elapsed = System.currentTimeMillis() - startTime
          val sleep = intervalMs - elapsed
          if (sleep > 0) {
            delay(sleep)
          } else {
            delay(1)
          }

          // Advance beat pointer
          val nextBeat = (currentBeat % beats) + 1
          currentBeat = nextBeat
        }
      }
    } else {
      currentBeat = 1 // Reset visualizer
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(24.dp)
  ) {
    // A. Navigation Header Box with Retro "BACK" Action
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
          fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
      }

      Spacer(modifier = Modifier.width(16.dp))

      Text(
        text = "PRACTICE / METRONOME",
        color = MaterialTheme.colorScheme.background,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
        letterSpacing = 1.sp
      )
    }

    // B. Visual Flashing Beat Indicator Grid (RetroBox - Responsive layout)
    RetroBox(shadowOffset = 4.dp) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = "MEASURE VISUALIZER",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
          color = MaterialTheme.colorScheme.onSurface
        )

        // Visual flash grid - Dynamic wrapping for high beat counts
        val maxPerRow = 6
        val rowsCount = if (beatsPerMeasure <= maxPerRow) 1 else 2
        val colsCount = if (beatsPerMeasure <= maxPerRow) beatsPerMeasure else (beatsPerMeasure + 1) / 2

        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          for (r in 0 until rowsCount) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              val start = r * colsCount + 1
              val end = minOf(beatsPerMeasure, (r + 1) * colsCount)
              for (i in start..end) {
                val isCurrent = i == currentBeat && isPlaying
                val flashColor = if (isCurrent) Color(0xFFCCCCCC) else MaterialTheme.colorScheme.surfaceVariant

                Box(
                  modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .border(2.dp, MaterialTheme.colorScheme.outline)
                    .background(flashColor),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = i.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                }
              }

              // Pad empty slots in row 2 to preserve equal grid alignments
              val activeColsInRow = end - start + 1
              if (activeColsInRow < colsCount) {
                for (pad in activeColsInRow until colsCount) {
                  Spacer(modifier = Modifier.weight(1f))
                }
              }
            }
          }
        }
      }
    }

    // C. Core Metronome Controller (RetroBox)
    RetroBox(shadowOffset = 4.dp) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Text(
          text = "TEMPO CONFIGURATION",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
          color = MaterialTheme.colorScheme.onSurface
        )

        // Chunky BPM Display Box
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, MaterialTheme.colorScheme.outline)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 14.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "$bpm BPM",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        // Flat Slider (max limit upgraded to 240 BPM!)
        Slider(
          value = bpm.toFloat(),
          onValueChange = { bpm = it.toInt() },
          valueRange = 40f..240f,
          colors = SliderDefaults.colors(
            thumbColor = Color(0xFFE5E5E5), // Light grey accent
            activeTrackColor = Color(0xFFCCCCCC), // Medium grey accent
            inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
          ),
          modifier = Modifier.fillMaxWidth()
        )

        // Precise Incremental Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          RetroButton(
            text = "-5",
            onClick = { bpm = (bpm - 5).coerceIn(40, 240) },
            modifier = Modifier.weight(1f)
          )
          RetroButton(
            text = "-1",
            onClick = { bpm = (bpm - 1).coerceIn(40, 240) },
            modifier = Modifier.weight(1f)
          )
          RetroButton(
            text = "+1",
            onClick = { bpm = (bpm + 1).coerceIn(40, 240) },
            modifier = Modifier.weight(1f)
          )
          RetroButton(
            text = "+5",
            onClick = { bpm = (bpm + 5).coerceIn(40, 240) },
            modifier = Modifier.weight(1f)
          )
        }
      }
    }

    // D. Time Signatures Selector (RetroBox - Expanded list!)
    RetroBox(shadowOffset = 4.dp) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = "TIME SIGNATURE",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
          color = MaterialTheme.colorScheme.onSurface
        )

        val signaturesList = listOf(
          listOf("1/4", "2/4", "3/4", "4/4"),
          listOf("5/4", "7/4", "5/8", "6/8"),
          listOf("7/8", "9/8", "12/8")
        )

        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          signaturesList.forEach { rowSignatures ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              rowSignatures.forEach { option ->
                val isSelected = timeSignature == option
                val chipBg = if (isSelected) Color(0xFFE5E5E5) else MaterialTheme.colorScheme.surface

                Box(
                  modifier = Modifier
                    .weight(1f)
                    .border(1.5.dp, MaterialTheme.colorScheme.outline)
                    .background(chipBg)
                    .clickable {
                      timeSignature = option
                      isPlaying = false // Stop briefly to reset parameters smoothly
                    }
                    .padding(vertical = 8.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = option,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                  )
                }
              }

              // Pad empty spacing in the last row for perfect column alignment
              if (rowSignatures.size < 4) {
                for (pad in rowSignatures.size until 4) {
                  Spacer(modifier = Modifier.weight(1f))
                }
              }
            }
          }
        }
      }
    }

    // E. Massive Play/Stop Trigger
    val playStopColor = if (isPlaying) Color(0xFFCCCCCC) else Color(0xFFE5E5E5)
    RetroButton(
      text = if (isPlaying) "Stop Metronome" else "Start Metronome",
      onClick = { isPlaying = !isPlaying },
      backgroundColor = playStopColor,
      modifier = Modifier.fillMaxWidth().height(52.dp)
    )
  }
}
