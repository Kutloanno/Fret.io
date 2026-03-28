package com.example.guitarkaizen.ui.screens.fretboardexplorer

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.guitarkaizen.data.ScaleDictionary
import com.example.guitarkaizen.ui.screens.eartraining.EarTrainerAudioEngine
import com.example.guitarkaizen.ui.screens.practice.RetroBox
import com.example.guitarkaizen.ui.screens.practice.RetroButton
import com.example.guitarkaizen.ui.screens.tuner.RetroDropdown

@Composable
fun FretboardExplorerScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FretboardExplorerViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface


    // Instantiate and manage the audio engine
    val audioEngine = remember { EarTrainerAudioEngine() }

    LaunchedEffect(Unit) {
        audioEngine.load(context)
    }

    DisposableEffect(Unit) {
        onDispose {
            audioEngine.release()
        }
    }

    val keys = ScaleDictionary.keysList
    val scales = ScaleDictionary.scalePatterns.map { it.name }

    // Scroll states for lists & fretboard
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // A. Retro Navigation Header Bar
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
                text = "STUDIO / FRETBOARD EXPLORER",
                color = MaterialTheme.colorScheme.background,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }

        // B. Configuration Card (Key and Scale Selection)
        RetroBox(shadowOffset = 3.dp) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "EXPLORATION TARGET",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RetroDropdown(
                            label = "Key / Root",
                            options = keys,
                            selectedOption = state.selectedKey,
                            onOptionSelected = { viewModel.setKey(it) },
                            optionToString = { it },
                            modifier = Modifier.weight(1f)
                        )

                        RetroDropdown(
                            label = "Scale / Arpeggio",
                            options = scales,
                            selectedOption = state.selectedScale,
                            onOptionSelected = { viewModel.setScale(it) },
                            optionToString = { it },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    RetroDropdown(
                        label = "CAGED Position Filter",
                        options = listOf("Any Position", "C Shape", "A Shape", "G Shape", "E Shape", "D Shape"),
                        selectedOption = state.selectedCagedPosition,
                        onOptionSelected = { viewModel.setCagedPosition(it) },
                        optionToString = { it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // C. The Interactive Maple Fretboard Canvas
        RetroBox(shadowOffset = 4.dp, backgroundColor = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "MAPLE FRETBOARD EXPLORER (TAP TO PLUCK NOTES)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .horizontalScroll(scrollState)
                        .border(2.dp, MaterialTheme.colorScheme.outline)
                ) {
                    var canvasWidth by remember { mutableStateOf(0) }
                    var canvasHeight by remember { mutableStateOf(0) }

                    Canvas(
                        modifier = Modifier
                            .width(1350.dp)
                            .fillMaxHeight()
                            .onSizeChanged {
                                canvasWidth = it.width
                                canvasHeight = it.height
                            }
                            .pointerInput(state.notes, canvasWidth, canvasHeight) {
                                if (canvasWidth <= 0 || canvasHeight <= 0) return@pointerInput

                                val leftMarginPx = with(density) { 50.dp.toPx() }
                                val rightMarginPx = with(density) { 30.dp.toPx() }
                                val stringStartYPx = with(density) { 20.dp.toPx() }
                                val playableWidthPx = canvasWidth - leftMarginPx - rightMarginPx
                                val fretSpacingPx = playableWidthPx / 22f
                                val stringSpacingPx = (canvasHeight - with(density) { 48.dp.toPx() }) / 5f

                                detectTapGestures { offset ->
                                    var closestString: Int? = null
                                    var minStringDist = Float.MAX_VALUE
                                    val yThreshold = with(density) { 32.dp.toPx() }

                                    for (s in 0..5) {
                                        val stringY = stringStartYPx + (5 - s) * stringSpacingPx
                                        val dist = kotlin.math.abs(offset.y - stringY)
                                        if (dist < yThreshold && dist < minStringDist) {
                                            minStringDist = dist
                                            closestString = s
                                        }
                                    }

                                    if (closestString != null) {
                                        val relativeX = offset.x - leftMarginPx
                                        val tappedFret: Int
                                        if (relativeX < 0) {
                                            tappedFret = 0
                                        } else {
                                            val fretFloat = relativeX / fretSpacingPx
                                            tappedFret = (fretFloat + 0.5f).toInt().coerceIn(1, 22)
                                        }

                                        val tappedNote = state.notes.firstOrNull {
                                            it.stringIndex == closestString && it.fret == tappedFret
                                        }
                                        if (tappedNote != null) {
                                            Log.d("Gesture", "Fretboard pluck: ${tappedNote.noteName} (MIDI ${tappedNote.absoluteMidi}) at string $closestString, fret $tappedFret")
                                            audioEngine.playNote(tappedNote.absoluteMidi)
                                        }
                                    }
                                }
                            }
                    ) {
                        if (canvasWidth <= 0 || canvasHeight <= 0) return@Canvas

                        val leftMarginPx = 50.dp.toPx()
                        val rightMarginPx = 30.dp.toPx()
                        val stringStartYPx = 20.dp.toPx()
                        val playableWidthPx = canvasWidth - leftMarginPx - rightMarginPx
                        val fretSpacingPx = playableWidthPx / 22f
                        val stringSpacingPx = (canvasHeight - 48.dp.toPx()) / 5f

                        // 1. Draw wood background neck
                        val neckHeight = canvasHeight - 24.dp.toPx()
                        drawRect(
                            color = Color(0xFFD79F67),
                            topLeft = Offset(leftMarginPx, 0f),
                            size = Size(canvasWidth.toFloat() - leftMarginPx, neckHeight)
                        )

                        // 2. Wood grain waves
                        val grainColor = Color(0xFFB57E47)
                        val waveCount = 5
                        for (i in 0 until waveCount) {
                            val path = Path()
                            val startY = neckHeight * (0.15f + i * 0.18f)
                            path.moveTo(leftMarginPx, startY)
                            val steps = 50
                            for (step in 1..steps) {
                                val x = leftMarginPx + (canvasWidth.toFloat() - leftMarginPx) * (step.toFloat() / steps)
                                val waveY = startY + kotlin.math.sin(step * 0.5f + i * 1.6f) * 6f + kotlin.math.cos(step * 0.2f) * 3f
                                path.lineTo(x, waveY)
                            }
                            drawPath(
                                path = path,
                                color = grainColor,
                                style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round),
                                alpha = 0.35f
                            )
                        }

                        // 3. Draw Nut (Thick bar)
                        drawLine(
                            color = Color.Black,
                            start = Offset(leftMarginPx, 0f),
                            end = Offset(leftMarginPx, neckHeight),
                            strokeWidth = 6.dp.toPx()
                        )

                        // 4. Draw 22 Frets (wires)
                        for (f in 1..22) {
                            val x = leftMarginPx + f * fretSpacingPx
                            drawLine(
                                color = Color.Black,
                                start = Offset(x, 0f),
                                end = Offset(x, neckHeight),
                                strokeWidth = 2.5.dp.toPx()
                            )
                        }

                        // 5. Draw Fret Inlays (centered squares)
                        val markerFrets = listOf(3, 5, 7, 9, 15, 17, 19, 21)
                        val markerSize = 8.dp.toPx()
                        val centerY = neckHeight / 2f

                        for (fret in markerFrets) {
                            val x = leftMarginPx + (fret - 0.5f) * fretSpacingPx
                            drawRect(
                                color = Color.Black.copy(alpha = 0.8f),
                                topLeft = Offset(x - markerSize / 2f, centerY - markerSize / 2f),
                                size = Size(markerSize, markerSize)
                            )
                        }

                        // Double inlay squares at fret 12
                        val x12 = leftMarginPx + (12 - 0.5f) * fretSpacingPx
                        val dotY1 = stringStartYPx + 1.5f * stringSpacingPx
                        val dotY2 = stringStartYPx + 3.5f * stringSpacingPx
                        drawRect(
                            color = Color.Black.copy(alpha = 0.8f),
                            topLeft = Offset(x12 - markerSize / 2f, dotY1 - markerSize / 2f),
                            size = Size(markerSize, markerSize)
                        )
                        drawRect(
                            color = Color.Black.copy(alpha = 0.8f),
                            topLeft = Offset(x12 - markerSize / 2f, dotY2 - markerSize / 2f),
                            size = Size(markerSize, markerSize)
                        )

                        // 6. Draw 6 Strings (horizontal lines)
                        for (s in 0..5) {
                            val y = stringStartYPx + s * stringSpacingPx
                            drawLine(
                                color = Color.Black,
                                start = Offset(leftMarginPx, y),
                                end = Offset(canvasWidth.toFloat(), y),
                                strokeWidth = (1f + (5 - s) * 0.4f).dp.toPx()
                            )
                        }

                        // 7. Draw Tuning Labels to the left of the Nut
                        val tuningNames = listOf("E", "B", "G", "D", "A", "E")

                        tuningNames.forEachIndexed { idx, name ->
                            val y = stringStartYPx + idx * stringSpacingPx
                            val labelLayout = textMeasurer.measure(
                                text = name,
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = onSurfaceColor
                                )
                            )
                            val tx = leftMarginPx - 24.dp.toPx() - labelLayout.size.width / 2f
                            val ty = y - labelLayout.size.height / 2f
                            drawText(
                                textLayoutResult = labelLayout,
                                topLeft = Offset(tx, ty)
                            )
                        }

                        // 8. Draw Fret Numbers at the bottom of the canvas
                        for (f in 1..22) {
                            val x = leftMarginPx + (f - 0.5f) * fretSpacingPx
                            val numLayout = textMeasurer.measure(
                                text = f.toString(),
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = onSurfaceColor.copy(alpha = 0.6f)
                                )
                            )
                            drawText(
                                textLayoutResult = numLayout,
                                topLeft = Offset(x - numLayout.size.width / 2f, canvasHeight - 18.dp.toPx())
                            )
                        }

                        // 9. Draw Active Notes on Fretboard Grid
                        state.notes.forEach { note ->
                            val y = stringStartYPx + (5 - note.stringIndex) * stringSpacingPx
                            val x = if (note.fret == 0) {
                                leftMarginPx - 12.dp.toPx()
                            } else {
                                leftMarginPx + (note.fret - 0.5f) * fretSpacingPx
                            }

                            val nodeSize = 24.dp.toPx()
                            val halfNode = nodeSize / 2f
                            val shadowOffset = 2.5.dp.toPx()

                            // Drop Shadow Box
                            drawRect(
                                color = Color.Black,
                                topLeft = Offset(x - halfNode + shadowOffset, y - halfNode + shadowOffset),
                                size = Size(nodeSize, nodeSize)
                            )

                            // Active Note Box (Light grey for Key Root, Medium grey for extensions)
                            val markerColor = if (note.isRoot) Color(0xFFE5E5E5) else Color(0xFFCCCCCC)
                            drawRect(
                                color = markerColor,
                                topLeft = Offset(x - halfNode, y - halfNode),
                                size = Size(nodeSize, nodeSize)
                            )

                            // Border
                            drawRect(
                                color = Color.Black,
                                topLeft = Offset(x - halfNode, y - halfNode),
                                size = Size(nodeSize, nodeSize),
                                style = Stroke(width = 1.5.dp.toPx())
                            )

                            // Monospace scale degree text centered perfectly
                            val degreeLayout = textMeasurer.measure(
                                text = note.degreeName,
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Color.Black
                                )
                            )
                            drawText(
                                textLayoutResult = degreeLayout,
                                topLeft = Offset(x - degreeLayout.size.width / 2f, y - degreeLayout.size.height / 2f)
                            )
                        }
                    }
                }
            }
        }
    }
}
