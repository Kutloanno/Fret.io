package com.example.guitarkaizen.ui.screens.notefinder

import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.guitarkaizen.R
import com.example.guitarkaizen.ui.screens.practice.RetroBox
import com.example.guitarkaizen.ui.screens.practice.RetroButton
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoteFinderScreen(
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: NoteFinderViewModel = viewModel()
) {
  val context = LocalContext.current
  val state by viewModel.state.collectAsState()

  // 1. SoundPool Audio Player
  val soundPool = remember {
    val audioAttributes = AudioAttributes.Builder()
      .setUsage(AudioAttributes.USAGE_GAME)
      .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
      .build()
    SoundPool.Builder()
      .setMaxStreams(2)
      .setAudioAttributes(audioAttributes)
      .build()
  }

  val correctSoundId = remember {
    try {
      soundPool.load(context, R.raw.correct, 1)
    } catch (e: Exception) {
      0
    }
  }

  val incorrectSoundId = remember {
    try {
      soundPool.load(context, R.raw.incorrect, 1)
    } catch (e: Exception) {
      0
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      soundPool.release()
    }
  }

  // Play audio chimes reactively based on guess results
  LaunchedEffect(state.lastGuessResult) {
    when (state.lastGuessResult) {
      true -> {
        try {
          soundPool.play(correctSoundId, 1f, 1f, 1, 0, 1f)
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
      false -> {
        try {
          soundPool.play(incorrectSoundId, 1f, 1f, 1, 0, 1f)
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
      null -> {}
    }
  }

  var isConfigExpanded by rememberSaveable { mutableStateOf(false) }
  val chromaticNotes = listOf("C", "C#/Db", "D", "D#/Eb", "E", "F", "F#/Gb", "G", "G#/Ab", "A", "A#/Bb", "B")

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp)
  ) {
    // A. Header Bar
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
        text = "PRACTICE / NOTE FINDER",
        color = MaterialTheme.colorScheme.background,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 1.sp
      )
    }

    // B. Session Stats Bar
    RetroBox(shadowOffset = 3.dp) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "SESSION SCORE: ${state.sessionScore}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(2.dp))
          val accuracy = if (state.totalGuesses > 0) {
            (state.sessionScore.toFloat() / state.totalGuesses * 100).toInt()
          } else {
            0
          }
          Text(
            text = "ACCURACY: $accuracy% (${state.sessionScore}/${state.totalGuesses})",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        RetroButton(
          text = "Reset Stats",
          onClick = { viewModel.resetStats() },
          backgroundColor = Color(0xFFE5E5E5),
          modifier = Modifier.width(96.dp)
        )
      }
    }

    // C. 24-Fret Scrollable maple wood Fretboard Canvas
    RetroBox(
      shadowOffset = 4.dp,
      backgroundColor = Color(0xFFD79F67) // Maple wood color
    ) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = "FIND THE HIGHLIGHTED TARGET NOTE:",
          fontSize = 9.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          color = Color.Black
        )

        // Horizontal Scroll Container
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
        ) {
          BoxWithConstraints(
            modifier = Modifier
              .width(1320.dp) // Fixed width to ensure 50.dp frets, avoiding squishing
              .height(180.dp)
          ) {
            val w = 1320.dp
            val h = 180.dp
            val leftMargin = 60.dp
            val rightMargin = 20.dp
            val playableWidth = w - leftMargin - rightMargin
            val fretSpacing = playableWidth / 24f
            val stringSpacing = (h - 30.dp) / 5f
            val stringStartY = 15.dp

            Canvas(
              modifier = Modifier.fillMaxSize()
            ) {
              val grainColor = Color(0xFFB57E47)
              val wPx = size.width
              val hPx = size.height
              val leftMarginPx = leftMargin.toPx()
              val rightMarginPx = rightMargin.toPx()
              val playableWidthPx = wPx - leftMarginPx - rightMarginPx
              val fretSpacingPx = playableWidthPx / 24f
              val stringSpacingPx = (hPx - 30.dp.toPx()) / 5f
              val stringStartYPx = 15.dp.toPx()

              // 1. Draw Programmatic Wood Grains (deterministic waves)
              val waveCount = 5
              for (i in 0 until waveCount) {
                val path = Path()
                val startY = hPx * (0.15f + i * 0.18f)
                path.moveTo(0f, startY)
                val steps = 48
                for (step in 1..steps) {
                  val x = wPx * (step.toFloat() / steps)
                  val waveY = startY + sin(step * 0.7f + i * 1.6f) * 6f + cos(step * 0.3f) * 3f
                  path.lineTo(x, waveY)
                }
                drawPath(
                  path = path,
                  color = grainColor,
                  style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round),
                  alpha = 0.35f
                )
              }

              // 2. Draw Nut (Thick line)
              drawLine(
                color = Color.Black,
                start = Offset(x = leftMarginPx, y = 0f),
                end = Offset(x = leftMarginPx, y = hPx),
                strokeWidth = 6.dp.toPx()
              )

              // 3. Draw Frets
              for (fret in 1..24) {
                val x = leftMarginPx + fret * fretSpacingPx
                drawLine(
                  color = Color.Black,
                  start = Offset(x = x, y = 0f),
                  end = Offset(x = x, y = hPx),
                  strokeWidth = 2.5.dp.toPx()
                )
              }

              // 4. Single unrounded square fret markers at frets 3, 5, 7, 9, 15, 17, 19, 21
              val singleMarkerFrets = intArrayOf(3, 5, 7, 9, 15, 17, 19, 21)
              val markerSize = 8.dp.toPx()
              val centerY = hPx / 2f
              for (fret in singleMarkerFrets) {
                val x = leftMarginPx + (fret - 0.5f) * fretSpacingPx
                drawRect(
                  color = Color.Black,
                  topLeft = Offset(x = x - markerSize / 2f, y = centerY - markerSize / 2f),
                  size = Size(markerSize, markerSize)
                )
              }

              // Double unrounded square markers at frets 12 and 24
              val doubleMarkerFrets = intArrayOf(12, 24)
              for (fret in doubleMarkerFrets) {
                val x = leftMarginPx + (fret - 0.5f) * fretSpacingPx
                val dotY1 = stringStartYPx + 1.5f * stringSpacingPx
                val dotY2 = stringStartYPx + 3.5f * stringSpacingPx

                drawRect(
                  color = Color.Black,
                  topLeft = Offset(x = x - markerSize / 2f, y = dotY1 - markerSize / 2f),
                  size = Size(markerSize, markerSize)
                )
                drawRect(
                  color = Color.Black,
                  topLeft = Offset(x = x - markerSize / 2f, y = dotY2 - markerSize / 2f),
                  size = Size(markerSize, markerSize)
                )
              }

              // 5. Draw Strings (standard EADGBe strings - Low E to High E)
              for (s in 0..5) {
                val y = stringStartYPx + s * stringSpacingPx
                drawLine(
                  color = Color.Black,
                  start = Offset(x = leftMarginPx, y = y),
                  end = Offset(x = wPx, y = y),
                  strokeWidth = (1f + s * 0.4f).dp.toPx()
                )
              }

              // 6. Smart Fretboard Shading: Shade inactive start/end boundaries
              val minFret = state.selectedMinFret
              val maxFret = state.selectedMaxFret

              // Left side shading (from nut to minFret)
              if (minFret > 0) {
                val endX = leftMarginPx + minFret * fretSpacingPx
                drawRect(
                  color = Color.Black.copy(alpha = 0.45f),
                  topLeft = Offset(x = leftMarginPx, y = 0f),
                  size = Size(endX - leftMarginPx, hPx)
                )
              }

              // Right side shading (from maxFret to 24th fret)
              if (maxFret < 24) {
                val startX = leftMarginPx + maxFret * fretSpacingPx
                val endX = leftMarginPx + 24 * fretSpacingPx
                drawRect(
                  color = Color.Black.copy(alpha = 0.45f),
                  topLeft = Offset(x = startX, y = 0f),
                  size = Size(endX - startX, hPx)
                )
              }
            }

            // Compose Overlays: Tuning Labels and Target Note Bubble
            val labels = listOf("e", "B", "G", "D", "A", "E")

            // 1. Dynamic Open String Labels
            labels.forEachIndexed { sIndex, label ->
              val yOffset = stringStartY + stringSpacing * sIndex - 9.dp
              Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color.Black,
                modifier = Modifier
                  .offset(x = 24.dp, y = yOffset)
                  .width(20.dp),
                textAlign = TextAlign.Center
              )
            }

            // 2. Note Position Calculation Helpers
            fun getXOffset(fret: Int): Dp {
              return if (fret == 0) {
                leftMargin - 22.dp
              } else {
                leftMargin + fretSpacing * (fret - 0.5f) - 12.dp
              }
            }

            fun getYOffset(strIndex: Int): Dp {
              return stringStartY + stringSpacing * strIndex - 12.dp
            }

            // 3. Highlighted Target Note Bubble
            val noteX = getXOffset(state.currentFret)
            val noteY = getYOffset(state.currentString)

            val bubbleBg = when (state.lastGuessResult) {
              true -> Color(0xFFCCCCCC)  // Medium grey for correct
              false -> Color(0xFFE5E5E5) // Light grey for incorrect
              else -> Color(0xFFCCCCCC)  // Standard medium grey
            }

            val bubbleText = when (state.lastGuessResult) {
              true -> state.currentNoteName
              false -> state.lastGuessedNote
              else -> "?"
            }

            Box(
              modifier = Modifier
                .offset(x = noteX, y = noteY)
                .size(24.dp)
                .background(bubbleBg, CircleShape)
                .border(2.dp, Color.Black, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = bubbleText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color.Black
              )
            }
          }
        }
      }
    }

    // D. Tactile 12-Button Chromatic Guessing Grid
    RetroBox(shadowOffset = 4.dp) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = "SELECT NOTE GUESS SUBMISSION:",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          color = MaterialTheme.colorScheme.onSurface
        )

        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Render buttons in a gorgeous 4x3 Grid (using standard Rows)
          val rows = chromaticNotes.chunked(3)
          rows.forEach { rowNotes ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              rowNotes.forEach { note ->
                val isCorrectHighlight = state.lastGuessResult == true && note == state.currentNoteName
                val isWrongHighlight = state.lastGuessResult == false && note == state.lastGuessedNote

                val buttonBg = when {
                  isCorrectHighlight -> Color(0xFFCCCCCC)  // Medium grey
                  isWrongHighlight -> Color(0xFFE5E5E5)    // Light grey
                  else -> MaterialTheme.colorScheme.surface
                }

                RetroButton(
                  text = note,
                  onClick = { viewModel.guessNote(note) },
                  backgroundColor = buttonBg,
                  modifier = Modifier.weight(1f)
                )
              }
            }
          }
        }
      }
    }

    // E. Collapsible Options Configuration Menu
    RetroBox(shadowOffset = 4.dp) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { isConfigExpanded = !isConfigExpanded }
            .padding(vertical = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "TRAINER CONFIGURATION MATRIX",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = if (isConfigExpanded) "COLLAPSE ▲" else "EXPAND ▼",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.outline
          )
        }

        AnimatedVisibility(visible = isConfigExpanded) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
          ) {
            // 1. Fret Testing Range selector (Plus/Minus Increments)
            Column(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = "FRET RANGE BOUNDARIES",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
              ) {
                // Min Fret Controls
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "START FRET: ${state.selectedMinFret}",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    Box(
                      modifier = Modifier
                        .size(30.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.5.dp, MaterialTheme.colorScheme.outline)
                        .clickable { viewModel.setMinFret(state.selectedMinFret - 1) },
                      contentAlignment = Alignment.Center
                    ) {
                      Text("-", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Box(
                      modifier = Modifier
                        .size(30.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.5.dp, MaterialTheme.colorScheme.outline)
                        .clickable { viewModel.setMinFret(state.selectedMinFret + 1) },
                      contentAlignment = Alignment.Center
                    ) {
                      Text("+", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                  }
                }

                // Max Fret Controls
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "END FRET: ${state.selectedMaxFret}",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    Box(
                      modifier = Modifier
                        .size(30.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.5.dp, MaterialTheme.colorScheme.outline)
                        .clickable { viewModel.setMaxFret(state.selectedMaxFret - 1) },
                      contentAlignment = Alignment.Center
                    ) {
                      Text("-", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Box(
                      modifier = Modifier
                        .size(30.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.5.dp, MaterialTheme.colorScheme.outline)
                        .clickable { viewModel.setMaxFret(state.selectedMaxFret + 1) },
                      contentAlignment = Alignment.Center
                    ) {
                      Text("+", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                  }
                }
              }
            }

            // 2. String Isolation Checkbox Toggles
            Column(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = "STRING ISOLATION PEGS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                val stringPegs = listOf("1st (e)", "2nd (B)", "3rd (G)", "4th (D)", "5th (A)", "6th (E)")
                // Note standard string peg index is 0..5, where 0 = High E (1st), 5 = Low E (6th)
                stringPegs.forEachIndexed { sIndex, label ->
                  val targetIndex = 5 - sIndex // Reverse to align index 5 (Low E) on right and index 0 (High E) on left
                  val isActive = state.activeStrings[targetIndex]
                  val bg = if (isActive) Color(0xFFCCCCCC) else MaterialTheme.colorScheme.surface

                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .height(34.dp)
                      .background(bg)
                      .border(1.5.dp, MaterialTheme.colorScheme.outline)
                      .clickable { viewModel.toggleString(targetIndex) },
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = label.substringBefore(" "),
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold,
                      fontFamily = FontFamily.Monospace,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                  }
                }
              }
            }

            // 3. Diatonic Key Lock Selection Row
            Column(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = "KEY CENTER DIATONIC LOCK",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )

              Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                val keys = listOf("ALL", "C", "G", "D", "A", "E", "F")
                keys.forEach { key ->
                  val isSelected = state.selectedKeyLock == key
                  val bg = if (isSelected) Color(0xFFCCCCCC) else MaterialTheme.colorScheme.surface

                  Box(
                    modifier = Modifier
                      .width(60.dp)
                      .height(34.dp)
                      .background(bg)
                      .border(1.5.dp, MaterialTheme.colorScheme.outline)
                      .clickable { viewModel.setKeyLock(key) },
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = key,
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      fontFamily = FontFamily.Monospace,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
