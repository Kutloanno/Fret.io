package com.example.guitarkaizen.ui.screens.minigames

import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.guitarkaizen.R
import com.example.guitarkaizen.ui.screens.practice.RetroBox
import com.example.guitarkaizen.ui.screens.practice.RetroButton
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

enum class GameState {
  IDLE, PLAYING, GAME_OVER
}

data class GuessOption(val name: String, val semitones: Int)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScaleDegreeGameScreen(
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  // 1. Game Engine Saved States
  var gameState by rememberSaveable { mutableStateOf(GameState.IDLE) }
  var score by rememberSaveable { mutableStateOf(0) }
  var timeLeft by rememberSaveable { mutableStateOf(60) }
  
  // Active Root / Target note states
  var rootString by rememberSaveable { mutableStateOf(4) } // Default String A (index 4)
  var rootFret by rememberSaveable { mutableStateOf(3) }   // Default Fret 3 (C)
  var targetString by rememberSaveable { mutableStateOf(3) } // Default String D (index 3)
  var targetFret by rememberSaveable { mutableStateOf(5) }   // Default Fret 5 (G)
  var correctInterval by rememberSaveable { mutableStateOf(7) } // Perfect 5th

  // Reference note states (for "Show Another Scale Degree" toggle)
  var refNoteString by rememberSaveable { mutableStateOf(4) }
  var refNoteFret by rememberSaveable { mutableStateOf(3) }
  var refNoteIntervalName by rememberSaveable { mutableStateOf("R") }
  var refNoteSemitones by rememberSaveable { mutableStateOf(0) }

  // Custom incorrect guess tracker for current note
  val wrongGuesses = remember { mutableStateListOf<Int>() }
  var hasGuessedCorrectly by remember { mutableStateOf(false) }

  // 2. Collapsible Configurations Saved States
  var isConfigExpanded by rememberSaveable { mutableStateOf(false) }
  var keyLock by rememberSaveable { mutableStateOf<Int?>(null) } // null = "ALL"
  val activeStrings = remember { mutableStateListOf(0, 1, 2, 3, 4, 5) }
  val activeIntervals = remember { mutableStateListOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11) }

  // UPGRADED ADVANCED FILTERS
  var currentTuning by rememberSaveable { mutableStateOf("Standard (EADGBe)") }
  var cagedShape by rememberSaveable { mutableStateOf("Any Position") }
  var isOctaveLimited by rememberSaveable { mutableStateOf(false) } // Within an Octave vs. Greater Range
  var isNarrowFretRange by rememberSaveable { mutableStateOf(false) } // Narrow (+/- 3 frets) vs. Wide Range
  var showOtherDegree by rememberSaveable { mutableStateOf(false) } // Show Scale Degree vs. Show Root Note

  // 3. Tuning configurations mapping
  val stringPitches = remember(currentTuning) {
    when (currentTuning) {
      "Drop D (DADGBe)" -> intArrayOf(64, 59, 55, 50, 45, 38)
      "DADGAD" -> intArrayOf(62, 57, 55, 50, 45, 38)
      "Open G (DGDGBd)" -> intArrayOf(62, 59, 55, 50, 43, 38)
      else -> intArrayOf(64, 59, 55, 50, 45, 40) // Standard EADGBe
    }
  }

  val labels = remember(currentTuning) {
    when (currentTuning) {
      "Drop D (DADGBe)" -> listOf("e", "B", "G", "D", "A", "D")
      "DADGAD" -> listOf("D", "A", "G", "D", "A", "D")
      "Open G (DGDGBd)" -> listOf("d", "B", "G", "D", "G", "D")
      else -> listOf("e", "B", "G", "D", "A", "E")
    }
  }

  // 4. SoundPool Audio Player
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

  val noteNames = remember { arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B") }

  // 12 Semitone Offsets mapped to Guess Buttons
  val guessOptions = remember {
    listOf(
      GuessOption("1", 0), GuessOption("b2", 1), GuessOption("2", 2),
      GuessOption("b3", 3), GuessOption("3", 4), GuessOption("4", 5),
      GuessOption("b5", 6), GuessOption("5", 7), GuessOption("b6", 8),
      GuessOption("6", 9), GuessOption("b7", 10), GuessOption("7", 11)
    )
  }

  // Helper function to calculate CAGED shape boundaries
  fun isFretInCagedShape(fret: Int, shape: String, rootPitchClass: Int): Boolean {
    val refString = when (shape) {
      "C Shape", "A Shape" -> 4 // String 5 (A)
      "G Shape", "E Shape" -> 5 // String 6 (E)
      "D Shape" -> 3           // String 4 (D)
      else -> return true
    }
    val openPitch = stringPitches[refString]
    var refFret = (rootPitchClass - openPitch + 120) % 12
    
    // Shift reference fret upwards if backwards-spanning shapes require it
    if (refFret < 3 && (shape == "C Shape" || shape == "G Shape")) {
      refFret += 12
    }
    
    val (start, end) = when (shape) {
      "C Shape" -> Pair(refFret - 3, refFret)
      "A Shape" -> Pair(refFret, refFret + 3)
      "G Shape" -> Pair(refFret - 3, refFret)
      "E Shape" -> Pair(refFret, refFret + 3)
      "D Shape" -> Pair(refFret - 1, refFret + 3)
      else -> Pair(0, 12)
    }
    
    val fNorm = if (fret < start && start > 12) fret + 12 else fret
    return fNorm in start..end || (fNorm + 12) in start..end || (fNorm - 12) in start..end
  }

  // 5. Question Generator Engine
  val generateNewQuestion = {
    wrongGuesses.clear()
    hasGuessedCorrectly = false

    val stringsToUse = if (activeStrings.isEmpty()) listOf(0, 1, 2, 3, 4, 5) else activeStrings
    val intervalsToUse = if (activeIntervals.isEmpty()) listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11) else activeIntervals

    var attempts = 0
    var found = false

    while (!found && attempts < 4000) {
      attempts++
      val rStr = stringsToUse.random()
      val rFret = (0..12).random()
      val rPitch = stringPitches[rStr] + rFret
      val rPitchClass = rPitch % 12

      // Enforce Key Lock if active
      if (keyLock != null && rPitchClass != keyLock) {
        continue
      }

      val tStr = stringsToUse.random()
      val tFret = (0..12).random()
      
      // Prevent placing target exactly on root coordinates
      if (tStr == rStr && tFret == rFret) {
        continue
      }

      val tPitch = stringPitches[tStr] + tFret
      val tPitchClass = tPitch % 12
      val interval = (tPitchClass - rPitchClass + 12) % 12

      // Enforce Interval filter
      if (!intervalsToUse.contains(interval)) {
        continue
      }

      // Enforce Range Constraint: Octave Limit
      if (isOctaveLimited && kotlin.math.abs(tPitch - rPitch) > 12) {
        continue
      }

      // Enforce Range Constraint: Fret Range (+/- 3 frets)
      if (isNarrowFretRange && kotlin.math.abs(tFret - rFret) > 3) {
        continue
      }

      // Enforce CAGED Shape boundaries on both Root and Target notes
      if (cagedShape != "Any Position") {
        if (!isFretInCagedShape(rFret, cagedShape, rPitchClass) || 
            !isFretInCagedShape(tFret, cagedShape, rPitchClass)) {
          continue
        }
      }

      // Found a valid pair! Commit states.
      rootString = rStr
      rootFret = rFret
      targetString = tStr
      targetFret = tFret
      correctInterval = interval

      // Reference Note Configuration
      if (showOtherDegree) {
        // Randomly pick between Major 3rd (4 semitones) and Perfect 5th (7 semitones) for Reference Note
        val refChoice = if ((0..1).random() == 0) Pair("3", 4) else Pair("5", 7)
        refNoteIntervalName = refChoice.first
        refNoteSemitones = refChoice.second

        val refPitchClass = (rPitchClass + refNoteSemitones) % 12
        var foundRef = false
        var refAttempts = 0
        
        while (!foundRef && refAttempts < 1000) {
          refAttempts++
          val s = stringsToUse.random()
          val f = (0..12).random()
          if ((stringPitches[s] + f) % 12 == refPitchClass) {
            // Respect CAGED shape if active
            if (cagedShape != "Any Position" && !isFretInCagedShape(f, cagedShape, rPitchClass)) {
              continue
            }
            refNoteString = s
            refNoteFret = f
            foundRef = true
          }
        }
        
        if (!foundRef) {
          refNoteString = rStr
          refNoteFret = (rFret + refNoteSemitones).coerceIn(0, 12)
        }
      } else {
        refNoteString = rStr
        refNoteFret = rFret
        refNoteIntervalName = "R"
        refNoteSemitones = 0
      }

      found = true
    }

    // Safety fallback
    if (!found) {
      rootString = 4
      rootFret = 3
      targetString = 3
      targetFret = 5
      correctInterval = 7
      refNoteString = 4
      refNoteFret = 3
      refNoteIntervalName = "R"
      refNoteSemitones = 0
    }
  }

  // 6. Timer Coroutine
  LaunchedEffect(gameState) {
    if (gameState == GameState.PLAYING) {
      timeLeft = 60
      while (timeLeft > 0) {
        delay(1000L)
        timeLeft--
      }
      gameState = GameState.GAME_OVER
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp)
  ) {
    // A. Retro Screen Header
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
        text = "THEORY / DEGREE SPEEDRUN",
        color = MaterialTheme.colorScheme.background,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 1.sp
      )
    }

    // B. State Screen Panels
    when (gameState) {
      GameState.IDLE -> {
        RetroBox(shadowOffset = 4.dp) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "SCALE DEGREE SPEEDRUN",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              color = MaterialTheme.colorScheme.onSurface,
              textAlign = TextAlign.Center
            )

            Text(
              text = "Drill scale degree recognition at high speed. You will see a REFERENCE note (Light grey circle with 'R' or a known degree '3/5') and a TARGET note (Medium grey '?') on the wooden neck.\n\nQuickly identify the scale degree interval offset of the Target note relative to the actual hidden Root note. Guess as many as you can in 60 seconds!",
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium,
              fontFamily = FontFamily.Monospace,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            RetroButton(
              text = "Start Speedrun",
              onClick = {
                score = 0
                generateNewQuestion()
                gameState = GameState.PLAYING
              },
              backgroundColor = Color(0xFFCCCCCC),
              modifier = Modifier.fillMaxWidth().height(48.dp)
            )
          }
        }
      }

      GameState.PLAYING -> {
        // Stats bar
        RetroBox(shadowOffset = 3.dp) {
          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "TIME LEFT: ${timeLeft}s",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "SESSION SCORE: $score",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.outline
              )
            }

            Box(
              modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, MaterialTheme.colorScheme.outline)
                .height(8.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
              Box(
                modifier = Modifier
                  .fillMaxHeight()
                  .fillMaxWidth(timeLeft / 60f)
                  .background(if (timeLeft > 15) Color(0xFFCCCCCC) else Color(0xFFE5E5E5))
              )
            }
          }
        }

        // Beautiful Maple Fretboard Canvas
        BoxWithConstraints(
          modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, MaterialTheme.colorScheme.outline)
            .background(Color(0xFFD79F67)) // Maple wood base
        ) {
          val width = maxWidth
          val height = 180.dp

          Canvas(
            modifier = Modifier
              .fillMaxWidth()
              .height(height)
          ) {
            val grainColor = Color(0xFFB57E47)
            val w = size.width
            val h = size.height

            // 1. Draw wood grain vectors
            val waveCount = 5
            for (i in 0 until waveCount) {
              val path = Path()
              val startY = h * (0.15f + i * 0.18f)
              path.moveTo(0f, startY)
              val steps = 24
              for (step in 1..steps) {
                val x = w * (step.toFloat() / steps)
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

            val leftMargin = 45.dp.toPx()
            val rightMargin = 15.dp.toPx()
            val playableWidth = w - leftMargin - rightMargin
            val fretSpacing = playableWidth / 12f

            // 2. Draw Nut (Thick line)
            drawLine(
              color = Color.Black,
              start = Offset(x = leftMargin, y = 0f),
              end = Offset(x = leftMargin, y = h),
              strokeWidth = 6.dp.toPx()
            )

            // 3. Draw Frets
            for (fret in 1..12) {
              val x = leftMargin + fret * fretSpacing
              drawLine(
                color = Color.Black,
                start = Offset(x = x, y = 0f),
                end = Offset(x = x, y = h),
                strokeWidth = 2.5.dp.toPx()
              )
            }

            // 4. Fret Markers (unrounded 8.dp square blocks)
            val markerFrets = intArrayOf(3, 5, 7, 9)
            val markerSize = 8.dp.toPx()
            val centerY = h / 2f

            for (fret in markerFrets) {
              val x = leftMargin + (fret - 0.5f) * fretSpacing
              drawRect(
                color = Color.Black,
                topLeft = Offset(x = x - markerSize / 2f, y = centerY - markerSize / 2f),
                size = Size(markerSize, markerSize)
              )
            }

            // Double dots at fret 12
            val x12 = leftMargin + (12 - 0.5f) * fretSpacing
            val stringSpacing = (h - 30.dp.toPx()) / 5f
            val stringStartY = 15.dp.toPx()

            val dotY1 = stringStartY + 1.5f * stringSpacing
            val dotY2 = stringStartY + 3.5f * stringSpacing

            drawRect(
              color = Color.Black,
              topLeft = Offset(x = x12 - markerSize / 2f, y = dotY1 - markerSize / 2f),
              size = Size(markerSize, markerSize)
            )
            drawRect(
              color = Color.Black,
              topLeft = Offset(x = x12 - markerSize / 2f, y = dotY2 - markerSize / 2f),
              size = Size(markerSize, markerSize)
            )

            // 5. Draw Strings
            for (s in 0..5) {
              val y = stringStartY + s * stringSpacing
              drawLine(
                color = Color.Black,
                start = Offset(x = leftMargin, y = y),
                end = Offset(x = w, y = y),
                strokeWidth = (1f + s * 0.4f).dp.toPx()
              )
            }
          }

          // Compose Layout Overlays
          val widthDp = maxWidth
          val leftMarginDp = 45.dp
          val rightMarginDp = 15.dp
          val playableWidthDp = widthDp - leftMarginDp - rightMarginDp
          val fretSpacingDp = playableWidthDp / 12f
          val stringSpacingDp = (height - 30.dp) / 5f
          val stringStartYDp = 15.dp

          // 1. Dynamic String Tuning Labels
          labels.forEachIndexed { sIndex, label ->
            val yOffset = stringStartYDp + stringSpacingDp * sIndex - 9.dp
            Text(
              text = label,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              color = Color.Black,
              modifier = Modifier
                .offset(x = 18.dp, y = yOffset)
                .width(16.dp),
              textAlign = TextAlign.Center
            )
          }

          fun getXOffset(fret: Int): Dp {
            return if (fret == 0) {
              leftMarginDp - 18.dp
            } else {
              leftMarginDp + fretSpacingDp * (fret - 0.5f) - 12.dp
            }
          }

          fun getYOffset(strIndex: Int): Dp {
            return stringStartYDp + stringSpacingDp * strIndex - 12.dp
          }

          // 2. Reference Note bubble (Light grey circle with "R" or interval offset "3/5")
          val refX = getXOffset(refNoteFret)
          val refY = getYOffset(refNoteString)
          Box(
            modifier = Modifier
              .offset(x = refX, y = refY)
              .size(24.dp)
              .background(color = Color(0xFFE5E5E5), shape = CircleShape)
              .border(2.dp, Color.Black, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = refNoteIntervalName,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              color = Color.Black
            )
          }

          // 3. Target Note bubble (Medium grey circle with "?")
          val targetX = getXOffset(targetFret)
          val targetY = getYOffset(targetString)
          Box(
            modifier = Modifier
              .offset(x = targetX, y = targetY)
              .size(24.dp)
              .background(color = Color(0xFFCCCCCC), shape = CircleShape)
              .border(2.dp, Color.Black, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "?",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              color = Color.Black
            )
          }
        }

        // C. Arcade Control Grid
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          guessOptions.chunked(3).forEach { rowOptions ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              rowOptions.forEach { option ->
                val semitones = option.semitones
                val isWrong = wrongGuesses.contains(semitones)
                
                val buttonColor = when {
                  isWrong -> Color(0xFFE5E5E5).copy(alpha = 0.5f)
                  else -> MaterialTheme.colorScheme.surface
                }

                RetroButtonWithAction(
                  text = option.name,
                  onClick = {
                    if (gameState == GameState.PLAYING && !hasGuessedCorrectly && !isWrong) {
                      if (semitones == correctInterval) {
                        hasGuessedCorrectly = true
                        soundPool.play(correctSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
                        score += 1
                        generateNewQuestion()
                      } else {
                        wrongGuesses.add(semitones)
                        soundPool.play(incorrectSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
                      }
                    }
                  },
                  backgroundColor = buttonColor,
                  modifier = Modifier.weight(1f),
                  isEnabled = !isWrong && !hasGuessedCorrectly
                )
              }
            }
          }
        }
      }

      GameState.GAME_OVER -> {
        RetroBox(shadowOffset = 4.dp) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "SPEEDRUN FINISHED!",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              color = MaterialTheme.colorScheme.onSurface
            )

            Box(
              modifier = Modifier
                .border(2.dp, MaterialTheme.colorScheme.outline)
                .background(Color(0xFFE5E5E5))
                .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
              Text(
                text = "SCORE: $score",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color.Black
              )
            }

            Text(
              text = "Congratulations! Your score has been synced to your Kaizen level progress. Let's see if you can break your record!",
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center
            )

            RetroButton(
              text = "Play Again",
              onClick = {
                score = 0
                generateNewQuestion()
                gameState = GameState.PLAYING
              },
              backgroundColor = Color(0xFFCCCCCC),
              modifier = Modifier.fillMaxWidth().height(48.dp)
            )
          }
        }
      }
    }

    // C. COLLAPSIBLE CONFIGURATION MATRIX
    RetroBox(shadowOffset = 4.dp) {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { isConfigExpanded = !isConfigExpanded }
            .padding(vertical = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "GAME CONFIGURATION",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = if (isConfigExpanded) "[- COLLAPSE]" else "[+ EXPAND]",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFE5E5E5)
          )
        }

        AnimatedVisibility(visible = isConfigExpanded) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(MaterialTheme.colorScheme.outline)
            )

            // Dynamic Tuning & CAGED shape selectors
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              val tunings = listOf("Standard (EADGBe)", "Drop D (DADGBe)", "DADGAD", "Open G (DGDGBd)")
              RetroDropdown(
                label = "Tuning Selector",
                options = tunings,
                selectedOption = currentTuning,
                onOptionSelected = {
                  currentTuning = it
                  if (gameState == GameState.PLAYING) generateNewQuestion()
                },
                modifier = Modifier.weight(1f)
              )

              val cagedShapes = listOf("Any Position", "C Shape", "A Shape", "G Shape", "E Shape", "D Shape")
              RetroDropdown(
                label = "CAGED Shape",
                options = cagedShapes,
                selectedOption = cagedShape,
                onOptionSelected = {
                  cagedShape = it
                  if (gameState == GameState.PLAYING) generateNewQuestion()
                },
                modifier = Modifier.weight(1f)
              )
            }

            // Radio Button Constraints Group
            Column(
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Text(
                text = "RANGE & TRAINING CONSTRAINTS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )

              // 1. Octave Limits
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
              ) {
                Text(
                  text = "OCTAVE LIMIT:",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  color = MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.width(96.dp).padding(vertical = 4.dp)
                )
                RetroRadioButton(
                  selected = isOctaveLimited,
                  onClick = {
                    isOctaveLimited = true
                    if (gameState == GameState.PLAYING) generateNewQuestion()
                  },
                  label = "Within Octave",
                  modifier = Modifier.weight(1f)
                )
                RetroRadioButton(
                  selected = !isOctaveLimited,
                  onClick = {
                    isOctaveLimited = false
                    if (gameState == GameState.PLAYING) generateNewQuestion()
                  },
                  label = "Greater Range",
                  modifier = Modifier.weight(1f)
                )
              }

              // 2. Fret Range Toggles
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
              ) {
                Text(
                  text = "FRET RANGE:",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  color = MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.width(96.dp).padding(vertical = 4.dp)
                )
                RetroRadioButton(
                  selected = isNarrowFretRange,
                  onClick = {
                    isNarrowFretRange = true
                    if (gameState == GameState.PLAYING) generateNewQuestion()
                  },
                  label = "Narrow (+/-3)",
                  modifier = Modifier.weight(1f)
                )
                RetroRadioButton(
                  selected = !isNarrowFretRange,
                  onClick = {
                    isNarrowFretRange = false
                    if (gameState == GameState.PLAYING) generateNewQuestion()
                  },
                  label = "Wide Range",
                  modifier = Modifier.weight(1f)
                )
              }

              // 3. Reference Note Toggles
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
              ) {
                Text(
                  text = "REFERENCE:",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  color = MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.width(96.dp).padding(vertical = 4.dp)
                )
                RetroRadioButton(
                  selected = !showOtherDegree,
                  onClick = {
                    showOtherDegree = false
                    if (gameState == GameState.PLAYING) generateNewQuestion()
                  },
                  label = "Root Note (R)",
                  modifier = Modifier.weight(1f)
                )
                RetroRadioButton(
                  selected = showOtherDegree,
                  onClick = {
                    showOtherDegree = true
                    if (gameState == GameState.PLAYING) generateNewQuestion()
                  },
                  label = "Scale Degree",
                  modifier = Modifier.weight(1f)
                )
              }
            }

            // Key Lock selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text(
                text = "KEY LOCK (ROOT NOTE)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              
              val keyList = listOf("ALL", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
              FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                keyList.forEachIndexed { index, name ->
                  val lockValue = if (name == "ALL") null else if (index <= 1) 0 else index - 1
                  val isSelected = (name == "ALL" && keyLock == null) || (name != "ALL" && keyLock == lockValue)

                  RetroMiniButton(
                    text = name,
                    onClick = {
                      keyLock = if (name == "ALL") null else (index - 1)
                      if (gameState == GameState.PLAYING) generateNewQuestion()
                    },
                    isSelected = isSelected
                  )
                }
              }
            }

            // String limits
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text(
                text = "STRING LIMITS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )

              val strings = listOf(
                Pair("e (1st)", 0), Pair("B (2nd)", 1), Pair("G (3rd)", 2),
                Pair("D (4th)", 3), Pair("A (5th)", 4), Pair("E (6th)", 5)
              )

              Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                RetroMiniButton(
                  text = "ALL STRINGS",
                  onClick = {
                    activeStrings.clear()
                    activeStrings.addAll(0..5)
                    if (gameState == GameState.PLAYING) generateNewQuestion()
                  },
                  isSelected = activeStrings.size == 6
                )
                RetroMiniButton(
                  text = "TREBLE (1-4)",
                  onClick = {
                    activeStrings.clear()
                    activeStrings.addAll(listOf(0, 1, 2, 3))
                    if (gameState == GameState.PLAYING) generateNewQuestion()
                  },
                  isSelected = activeStrings.size == 4 && activeStrings.contains(0) && !activeStrings.contains(4)
                )
                RetroMiniButton(
                  text = "BASS (5-6)",
                  onClick = {
                    activeStrings.clear()
                    activeStrings.addAll(listOf(4, 5))
                    if (gameState == GameState.PLAYING) generateNewQuestion()
                  },
                  isSelected = activeStrings.size == 2 && activeStrings.contains(4) && !activeStrings.contains(0)
                )
              }

              Spacer(modifier = Modifier.height(4.dp))

              FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                strings.forEach { (name, sIdx) ->
                  val isChecked = activeStrings.contains(sIdx)
                  RetroCheckbox(
                    checked = isChecked,
                    onCheckedChange = { check ->
                      if (check) {
                        if (!activeStrings.contains(sIdx)) activeStrings.add(sIdx)
                      } else {
                        if (activeStrings.size > 1) activeStrings.remove(sIdx)
                      }
                      if (gameState == GameState.PLAYING) generateNewQuestion()
                    },
                    label = name
                  )
                }
              }
            }

            // Interval degree filters
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text(
                text = "INTERVAL DEGREES FILTERS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )

              Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                RetroMiniButton(
                  text = "ALL DEGREES",
                  onClick = {
                    activeIntervals.clear()
                    activeIntervals.addAll(0..11)
                    if (gameState == GameState.PLAYING) generateNewQuestion()
                  },
                  isSelected = activeIntervals.size == 12
                )
                RetroMiniButton(
                  text = "TRIADS (1,3,5)",
                  onClick = {
                    activeIntervals.clear()
                    activeIntervals.addAll(listOf(0, 4, 7))
                    if (gameState == GameState.PLAYING) generateNewQuestion()
                  },
                  isSelected = activeIntervals.size == 3 && activeIntervals.contains(4) && activeIntervals.contains(7)
                )
                RetroMiniButton(
                  text = "DIATONIC (1-7)",
                  onClick = {
                    activeIntervals.clear()
                    activeIntervals.addAll(listOf(0, 2, 4, 5, 7, 9, 11))
                    if (gameState == GameState.PLAYING) generateNewQuestion()
                  },
                  isSelected = activeIntervals.size == 7
                )
                RetroMiniButton(
                  text = "PENTATONIC",
                  onClick = {
                    activeIntervals.clear()
                    activeIntervals.addAll(listOf(0, 2, 4, 7, 9))
                    if (gameState == GameState.PLAYING) generateNewQuestion()
                  },
                  isSelected = activeIntervals.size == 5
                )
              }

              Spacer(modifier = Modifier.height(4.dp))

              FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                guessOptions.forEach { opt ->
                  val isChecked = activeIntervals.contains(opt.semitones)
                  RetroCheckbox(
                    checked = isChecked,
                    onCheckedChange = { check ->
                      if (check) {
                        if (!activeIntervals.contains(opt.semitones)) activeIntervals.add(opt.semitones)
                      } else {
                        if (activeIntervals.size > 1) activeIntervals.remove(opt.semitones)
                      }
                      if (gameState == GameState.PLAYING) generateNewQuestion()
                    },
                    label = opt.name
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

// Retro selection and drop custom utilities
@Composable
fun RetroDropdown(
  label: String,
  options: List<String>,
  selectedOption: String,
  onOptionSelected: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var expanded by remember { mutableStateOf(false) }

  Column(modifier = modifier) {
    Text(
      text = label.uppercase(),
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(4.dp))

    Box(modifier = Modifier.fillMaxWidth()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.5.dp, MaterialTheme.colorScheme.outline)
          .background(MaterialTheme.colorScheme.surface)
          .clickable { expanded = !expanded }
          .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = selectedOption.uppercase(),
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = if (expanded) "▲" else "▼",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.outline
        )
      }

      androidx.compose.material3.DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier
          .background(MaterialTheme.colorScheme.surface)
          .border(1.5.dp, MaterialTheme.colorScheme.outline)
          .width(IntrinsicSize.Max)
      ) {
        options.forEach { option ->
          val isSelected = option == selectedOption
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(if (isSelected) Color(0xFFE5E5E5) else MaterialTheme.colorScheme.surface)
              .clickable {
                onOptionSelected(option)
                expanded = false
              }
              .padding(horizontal = 16.dp, vertical = 10.dp)
          ) {
            Text(
              text = option.uppercase(),
              fontSize = 11.sp,
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

@Composable
fun RetroRadioButton(
  selected: Boolean,
  onClick: () -> Unit,
  label: String,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .clickable(onClick = onClick)
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(16.dp)
        .border(1.5.dp, MaterialTheme.colorScheme.outline)
        .background(MaterialTheme.colorScheme.surface),
      contentAlignment = Alignment.Center
    ) {
      if (selected) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .background(Color.Black)
        )
      }
    }
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = label.uppercase(),
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}

@Composable
fun RetroMiniButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isSelected: Boolean = false,
  backgroundColor: Color = if (isSelected) Color(0xFFE5E5E5) else MaterialTheme.colorScheme.surface
) {
  Box(
    modifier = modifier
      .border(1.5.dp, MaterialTheme.colorScheme.outline)
      .background(backgroundColor)
      .clickable(onClick = onClick)
      .padding(horizontal = 8.dp, vertical = 6.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text.uppercase(),
      fontSize = 9.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace,
      color = MaterialTheme.colorScheme.onBackground
    )
  }
}

@Composable
fun RetroCheckbox(
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  label: String,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .clickable { onCheckedChange(!checked) }
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(18.dp)
        .border(1.5.dp, MaterialTheme.colorScheme.outline)
        .background(if (checked) Color(0xFFCCCCCC) else MaterialTheme.colorScheme.surface),
      contentAlignment = Alignment.Center
    ) {
      if (checked) {
        Text(
          text = "X",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          color = Color.Black
        )
      }
    }
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = label.uppercase(),
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}

@Composable
fun RetroButtonWithAction(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  backgroundColor: Color = Color(0xFFE5E5E5),
  shadowOffset: Dp = 3.dp,
  isEnabled: Boolean = true
) {
  val outlineColor = MaterialTheme.colorScheme.outline

  Box(
    modifier = modifier
      .clickable(enabled = isEnabled, onClick = onClick)
  ) {
    Box(
      modifier = Modifier
        .matchParentSize()
        .offset(x = shadowOffset, y = shadowOffset)
        .background(outlineColor)
    )
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(backgroundColor)
        .border(1.5.dp, outlineColor)
        .padding(vertical = 12.dp),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = text.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        color = outlineColor
      )
    }
  }
}
