package com.example.guitarkaizen.ui.screens.chordlibrary

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.guitarkaizen.data.ChordVoicing

/**
 * A Jetpack Compose Canvas component that renders a standard vertical 6-string, 5-fret chord box.
 *
 * Supports decoupled touch gestures (concurrent pluck taps and strum swipes), wide hitboxes (60f),
 * neck-shifting offsets, muted/open indicators, and left-handed guitar mirroring.
 */
@Composable
fun ChordGridCanvas(
    voicing: ChordVoicing,
    isLeftHanded: Boolean,
    onStringTriggered: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val outlineColor = MaterialTheme.colorScheme.outline
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceColor = MaterialTheme.colorScheme.surface

    // Fretboard layout margins in DP
    val leftMarginDp = 36.dp
    val rightMarginDp = 36.dp
    val topMarginDp = 40.dp
    val bottomMarginDp = 20.dp

    var canvasWidth by remember { mutableStateOf(0) }

    Canvas(
        modifier = modifier
            .background(surfaceColor)
            .onSizeChanged { canvasWidth = it.width }
            // 1. Pluck Gesture: Tap detection running in its own concurrent pointerInput scope
            .pointerInput(voicing, isLeftHanded, canvasWidth) {
                if (canvasWidth <= 0) return@pointerInput

                val leftMarginPx = with(density) { leftMarginDp.toPx() }
                val rightMarginPx = with(density) { rightMarginDp.toPx() }
                val gridWidthPx = canvasWidth - leftMarginPx - rightMarginPx
                val stringGapPx = gridWidthPx / 5f

                detectTapGestures { offset ->
                    val stringIdx = getClosestStringWithThreshold(
                        x = offset.x,
                        leftMarginPx = leftMarginPx,
                        stringGapPx = stringGapPx,
                        isLeftHanded = isLeftHanded,
                        threshold = 60f
                    )
                    if (stringIdx != null) {
                        Log.d("Gesture", "Tap (pluck) registered on string index: $stringIdx")
                        onStringTriggered(stringIdx)
                    }
                }
            }
            // 2. Strum Gesture: Horizontal drag detection in a separate concurrent pointerInput scope
            .pointerInput(voicing, isLeftHanded, canvasWidth) {
                if (canvasWidth <= 0) return@pointerInput

                val leftMarginPx = with(density) { leftMarginDp.toPx() }
                val rightMarginPx = with(density) { rightMarginDp.toPx() }
                val gridWidthPx = canvasWidth - leftMarginPx - rightMarginPx
                val stringGapPx = gridWidthPx / 5f

                // Maintain a local swipe de-duplication state
                val swipedStrings = mutableSetOf<Int>()

                detectDragGestures(
                    onDragStart = { offset ->
                        swipedStrings.clear()
                        val stringIdx = getClosestStringWithThreshold(
                            x = offset.x,
                            leftMarginPx = leftMarginPx,
                            stringGapPx = stringGapPx,
                            isLeftHanded = isLeftHanded,
                            threshold = 60f
                        )
                        if (stringIdx != null) {
                            swipedStrings.add(stringIdx)
                            Log.d("Gesture", "Drag start (strum) registered on string index: $stringIdx")
                            onStringTriggered(stringIdx)
                        }
                    },
                    onDrag = { change, _ ->
                        val stringIdx = getClosestStringWithThreshold(
                            x = change.position.x,
                            leftMarginPx = leftMarginPx,
                            stringGapPx = stringGapPx,
                            isLeftHanded = isLeftHanded,
                            threshold = 60f
                        )
                        if (stringIdx != null && stringIdx !in swipedStrings) {
                            swipedStrings.add(stringIdx)
                            Log.d("Gesture", "Drag sweep (strum) registered on string index: $stringIdx")
                            onStringTriggered(stringIdx)
                        }
                    },
                    onDragEnd = {
                        swipedStrings.clear()
                        Log.d("Gesture", "Drag sweep completed cleanly")
                    },
                    onDragCancel = {
                        swipedStrings.clear()
                        Log.d("Gesture", "Drag sweep cancelled")
                    }
                )
            }
    ) {
        val width = size.width
        val height = size.height

        val leftMargin = leftMarginDp.toPx()
        val rightMargin = rightMarginDp.toPx()
        val topMargin = topMarginDp.toPx()
        val bottomMargin = bottomMarginDp.toPx()

        val gridWidth = width - leftMargin - rightMargin
        val gridHeight = height - topMargin - bottomMargin

        val stringGap = gridWidth / 5f
        val fretGap = gridHeight / 5f

        // Calculate starting fret. Shift fret range if target uses higher neck positions.
        val playedFrets = voicing.frets.filter { it > 0 }
        val startFret = if (playedFrets.isEmpty() || playedFrets.maxOrNull()!! <= 5) {
            1
        } else {
            playedFrets.minOrNull()!!
        }

        // 1. Draw 6 strings (vertical lines)
        for (i in 0..5) {
            val stringX = leftMargin + i * stringGap
            drawLine(
                color = outlineColor,
                start = Offset(stringX, topMargin),
                end = Offset(stringX, topMargin + gridHeight),
                strokeWidth = 2.dp.toPx()
            )
        }

        // 2. Draw 6 fret bars (horizontal lines)
        for (j in 0..5) {
            val fretY = topMargin + j * fretGap
            val isNut = (startFret == 1 && j == 0)
            val strokeWidth = if (isNut) 6.dp.toPx() else 2.dp.toPx()

            drawLine(
                color = outlineColor,
                start = Offset(leftMargin, fretY),
                end = Offset(leftMargin + gridWidth, fretY),
                strokeWidth = strokeWidth
            )
        }

        // 3. Draw high-register fret indicator on the left (e.g. "8 fr.")
        if (startFret > 1) {
            val labelText = "$startFret fr."
            val labelLayout = textMeasurer.measure(
                text = labelText,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            )
            val textWidth = labelLayout.size.width
            val textHeight = labelLayout.size.height

            val labelX = leftMargin - textWidth - 8.dp.toPx()
            val labelY = topMargin + (0.5f * fretGap) - textHeight / 2f
            drawText(
                textLayoutResult = labelLayout,
                topLeft = Offset(labelX, labelY),
                color = onSurfaceColor
            )
        }

        // 4. Draw muted/open indicators above nut, and solid circles inside fret blocks
        voicing.frets.forEachIndexed { s, fret ->
            // Lefty mirroring logic: invert string indices horizontally
            val drawStringIdx = if (isLeftHanded) (5 - s) else s
            val stringX = leftMargin + drawStringIdx * stringGap

            if (fret == -1) {
                // Muted string 'X'
                val labelLayout = textMeasurer.measure(
                    text = "X",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                )
                val tx = stringX - labelLayout.size.width / 2f
                val ty = topMargin - 20.dp.toPx() - labelLayout.size.height / 2f
                drawText(labelLayout, topLeft = Offset(tx, ty), color = outlineColor)
            } else if (fret == 0) {
                // Open string 'O'
                val labelLayout = textMeasurer.measure(
                    text = "O",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                )
                val tx = stringX - labelLayout.size.width / 2f
                val ty = topMargin - 20.dp.toPx() - labelLayout.size.height / 2f
                drawText(labelLayout, topLeft = Offset(tx, ty), color = outlineColor)
            } else if (fret > 0) {
                // Fretted note: draw target circle on fret intersection
                val relFret = fret - startFret + 1
                if (relFret in 1..5) {
                    val circleX = stringX
                    val circleY = topMargin + (relFret - 0.5f) * fretGap

                    // Alternating retro highlight accents based on strings
                    val circleColor = if (s % 2 == 0) Color(0xFFCCCCCC) else Color(0xFFE5E5E5) // Medium & light grey

                    drawCircle(
                        color = circleColor,
                        radius = 8.dp.toPx(),
                        center = Offset(circleX, circleY)
                    )
                    drawCircle(
                        color = outlineColor,
                        radius = 8.dp.toPx(),
                        center = Offset(circleX, circleY),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }
    }
}

/**
 * Helper to calculate which guitar string (0 to 5) corresponds to the touch X-coordinate,
 * using a generous horizontal hitbox threshold (60f) to guarantee extremely reliable hits.
 * Returns null if touch lies completely outside standard string channels.
 */
private fun getClosestStringWithThreshold(
    x: Float,
    leftMarginPx: Float,
    stringGapPx: Float,
    isLeftHanded: Boolean,
    threshold: Float
): Int? {
    for (i in 0..5) {
        val stringX = leftMarginPx + i * stringGapPx
        if (kotlin.math.abs(x - stringX) < threshold) {
            return if (isLeftHanded) (5 - i) else i
        }
    }
    return null
}
