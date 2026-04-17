package com.example.guitarkaizen.ui.screens.tuner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.guitarkaizen.data.Tuning
import com.example.guitarkaizen.data.TuningDictionary
import com.example.guitarkaizen.ui.screens.practice.RetroBox
import com.example.guitarkaizen.ui.screens.practice.RetroButton

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TunerScreen(
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: TunerViewModel = viewModel()
) {
  val context = LocalContext.current
  val state by viewModel.state.collectAsState()

  // 1. Audio Record Permission request logic
  var hasAudioPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    )
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
    onResult = { granted ->
      hasAudioPermission = granted
    }
  )

  LaunchedEffect(hasAudioPermission) {
    if (hasAudioPermission) {
      viewModel.startListening()
    } else {
      permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
  }

  // Release microphone resources safely upon screen exit
  DisposableEffect(Unit) {
    onDispose {
      viewModel.stopListening()
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
          text = "< BACK",
          color = MaterialTheme.colorScheme.background,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
      }

      Spacer(modifier = Modifier.width(16.dp))

      Text(
        text = "PRACTICE / PITCH TUNER",
        color = MaterialTheme.colorScheme.background,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 1.sp
      )
    }

    // B. Permission Warning Card (if denied)
    if (!hasAudioPermission) {
      RetroBox(
        shadowOffset = 4.dp,
        backgroundColor = Color(0xFFE5E5E5) // Miami retro warning red
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(4.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "MICROPHONE DISCONNECTED",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = Color.Black
          )
          Text(
            text = "Real-time tuner pitch tracking requires audio capture recording access. Please click below to grant permission, or authorize microphone access for Fret.io in your device settings.",
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = Color.Black.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            lineHeight = 15.sp
          )
          Spacer(modifier = Modifier.height(6.dp))
          RetroButton(
            text = "Re-Authorize Mic",
            onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
            backgroundColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }

    // C. Settings & Controls Card
    RetroBox(shadowOffset = 4.dp) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Text(
          text = "TUNER TARGET DESIGNATION",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          color = MaterialTheme.colorScheme.onSurface
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.Bottom
        ) {
          // Dropdown for target tuning
          val tuningsList = TuningDictionary.tunings
          RetroDropdown(
            label = "Tuning Peg Template",
            options = tuningsList,
            selectedOption = state.selectedTuning,
            onOptionSelected = { viewModel.setSelectedTuning(it) },
            optionToString = { it.name },
            modifier = Modifier.weight(1f)
          )

          // Chunky toggle button for Auto vs Manual
          val autoBg = if (state.isAutoMode) Color(0xFFCCCCCC) else Color(0xFFE5E5E5)
          val autoText = if (state.isAutoMode) "AUTO MODE: ON" else "MANUAL: PEGS"

          Box(
            modifier = Modifier
              .weight(1f)
              .height(44.dp)
              .clickable { viewModel.setAutoMode(!state.isAutoMode) }
          ) {
            Box(
              modifier = Modifier
                .matchParentSize()
                .offset(x = 3.dp, y = 3.dp)
                .background(MaterialTheme.colorScheme.outline)
            )
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(autoBg)
                .border(1.5.dp, MaterialTheme.colorScheme.outline),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = autoText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onBackground
              )
            }
          }
        }
      }
    }

    // D. The 16-Bit Real-Time Visualizer Box
    RetroBox(shadowOffset = 4.dp) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "REAL-TIME OSCILLATOR GRAPH",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.fillMaxWidth()
        )

        // Monospace large frequencies readouts
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, MaterialTheme.colorScheme.outline)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "INCOMING PITCH",
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = if (state.currentPitchHz > 0) String.format("%.1f Hz", state.currentPitchHz) else "NO SIGNAL",
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = "TARGET REF (${state.closestNoteName})",
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = String.format("%.1f Hz", state.targetPitchHz),
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }

        // Cents Off digital reading
        val centsText = when {
          state.currentPitchHz <= 0 -> "WAITING FOR INPUT..."
          state.centsOff < -5f -> String.format("FLAT: %.0f CENTS", state.centsOff)
          state.centsOff > 5f -> String.format("SHARP: +%.0f CENTS", state.centsOff)
          else -> "IN TUNE!"
        }
        val centsColor = when {
          state.currentPitchHz <= 0 -> MaterialTheme.colorScheme.onSurfaceVariant
          state.centsOff < -5f || state.centsOff > 5f -> Color(0xFFE5E5E5)
          else -> Color(0xFFCCCCCC)
        }

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, MaterialTheme.colorScheme.outline)
            .background(centsColor)
            .padding(vertical = 8.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = centsText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = Color.Black
          )
        }

        // 16-Bit Segmented Block Cents Gauge (-50 to +50 cents)
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .height(32.dp)
              .border(1.5.dp, MaterialTheme.colorScheme.outline)
              .background(MaterialTheme.colorScheme.surface),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            val centsOff = state.centsOff
            
            for (i in -5..5) {
              val isCenter = i == 0
              val blockColor = when {
                state.currentPitchHz <= 0 -> Color.Transparent
                
                // perfectly in tune center block
                isCenter && centsOff in -5f..5f -> Color(0xFFCCCCCC) // Medium grey
                
                // flat left side blocks
                i < 0 && centsOff < -5f -> {
                  val bound = i * 10f
                  if (centsOff <= bound) Color(0xFFE5E5E5) else Color.Transparent // Light grey
                }
                
                // sharp right side blocks
                i > 0 && centsOff > 5f -> {
                  val bound = i * 10f
                  if (centsOff >= bound) Color(0xFFE5E5E5) else Color.Transparent // Light grey
                }
                
                else -> Color.Transparent
              }

              Box(
                modifier = Modifier
                  .weight(1f)
                  .fillMaxHeight()
                  .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                  .background(blockColor)
              )
            }
          }
          
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("FLAT (-50c)", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("0", fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("SHARP (+50c)", fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }
    }

    // E. The Headstock Interface (Tuning Pegboard)
    AnimatedVisibility(visible = state.selectedTuning.id != "chromatic") {
      RetroBox(shadowOffset = 4.dp) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          Column {
            Text(
              text = "GUITAR HEADSTOCK LAYOUT",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "Click any peg to lock into Manual tuning mode.",
              fontSize = 9.sp,
              fontFamily = FontFamily.Monospace,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          // Headstock styling: left 3 pegs vs right 3 pegs, or horizontal list
          // Real guitar 6-in-line or 3+3. Let's arrange them in a gorgeous 3x2 grid resembling a headstock!
          val strings = state.selectedTuning.strings
          
          if (strings.size >= 6) {
            // Arrange strings 6, 5, 4 (Low strings) on left, and 3, 2, 1 (High strings) on right
            val leftPegs = listOf(strings[5], strings[4], strings[3]) // E2 (6), A2 (5), D3 (4)
            val rightPegs = listOf(strings[2], strings[1], strings[0]) // G3 (3), B3 (2), E4 (1)

            Row(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
              horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
              // Left Pegs (Strings 6, 5, 4)
              Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                leftPegs.forEach { str ->
                  val isSelected = !state.isAutoMode && state.selectedStringIndex == str.index
                  val pegColor = if (isSelected) Color(0xFFE5E5E5) else MaterialTheme.colorScheme.surface
                  
                  RetroPegButton(
                    stringName = str.name,
                    stringDesc = "STR ${6 - str.index}",
                    isSelected = isSelected,
                    backgroundColor = pegColor,
                    onClick = {
                      viewModel.setSelectedStringIndex(str.index)
                      viewModel.setAutoMode(false) // Force manual mode
                    }
                  )
                }
              }

              // Visual Headstock neck spacer (gorgeous retro block in center)
              Box(
                modifier = Modifier
                  .width(28.dp)
                  .height(136.dp)
                  .border(1.5.dp, MaterialTheme.colorScheme.outline)
                  .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
              ) {
                // Stylized neck lines
                Column(
                  modifier = Modifier.fillMaxHeight().width(2.dp).background(MaterialTheme.colorScheme.outline)
                ) {}
              }

              // Right Pegs (Strings 3, 2, 1)
              Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                rightPegs.forEach { str ->
                  val isSelected = !state.isAutoMode && state.selectedStringIndex == str.index
                  val pegColor = if (isSelected) Color(0xFFE5E5E5) else MaterialTheme.colorScheme.surface
                  
                  RetroPegButton(
                    stringName = str.name,
                    stringDesc = "STR ${6 - str.index}",
                    isSelected = isSelected,
                    backgroundColor = pegColor,
                    onClick = {
                      viewModel.setSelectedStringIndex(str.index)
                      viewModel.setAutoMode(false) // Force manual mode
                    }
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

// Retro Peg Button helper
@Composable
fun RetroPegButton(
  stringName: String,
  stringDesc: String,
  isSelected: Boolean,
  backgroundColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(38.dp)
      .clickable(onClick = onClick)
  ) {
    Box(
      modifier = Modifier
        .matchParentSize()
        .offset(x = 3.dp, y = 3.dp)
        .background(MaterialTheme.colorScheme.outline)
    )
    Row(
      modifier = Modifier
        .fillMaxSize()
        .background(backgroundColor)
        .border(1.5.dp, MaterialTheme.colorScheme.outline)
        .padding(horizontal = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = stringName,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = stringDesc,
        fontSize = 8.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

// Generic custom RetroDropdown styled for Tuner classes
@Composable
fun <T> RetroDropdown(
  label: String,
  options: List<T>,
  selectedOption: T,
  onOptionSelected: (T) -> Unit,
  optionToString: (T) -> String,
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
          .padding(horizontal = 10.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = optionToString(selectedOption).uppercase(),
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
              text = optionToString(option).uppercase(),
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
