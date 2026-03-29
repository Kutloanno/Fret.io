package com.example.guitarkaizen.ui.screens.chordlibrary

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.guitarkaizen.ui.screens.eartraining.EarTrainerAudioEngine
import com.example.guitarkaizen.ui.screens.practice.RetroBox
import com.example.guitarkaizen.ui.screens.practice.RetroButton
import com.example.guitarkaizen.ui.screens.tuner.RetroDropdown

@Composable
fun ChordLibraryScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChordLibraryViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    // Instantiate and manage the polyphonic sampler audio engine
    val audioEngine = remember { EarTrainerAudioEngine() }

    LaunchedEffect(Unit) {
        audioEngine.load(context)
    }

    DisposableEffect(Unit) {
        onDispose {
            audioEngine.release()
        }
    }

    val rootsList = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    val qualitiesList = listOf(
        "Major", "Minor", "Diminished", "Augmented", "Maj7", "Min7", "7", "m7b5", "Dim7", "sus2", "sus4", "add9", "min9", "maj9"
    )

    val activeVoicing = viewModel.getCurrentVoicing()
    val totalVoicings = viewModel.getVoicingsCount()

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
                text = "STUDIO / CHORD LIBRARY",
                color = MaterialTheme.colorScheme.background,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }

        // B. Selection Panel (Root & Quality Dropdowns Side-By-Side)
        RetroBox(shadowOffset = 3.dp) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "CHORD SELECTOR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RetroDropdown(
                        label = "Root",
                        options = rootsList,
                        selectedOption = state.selectedRoot,
                        onOptionSelected = { viewModel.setRoot(it) },
                        optionToString = { it },
                        modifier = Modifier.weight(1f)
                    )

                    RetroDropdown(
                        label = "Quality",
                        options = qualitiesList,
                        selectedOption = state.selectedQuality,
                        onOptionSelected = { viewModel.setQuality(it) },
                        optionToString = { it },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // C. Center Stage: Fretboard Grid Canvas or Empty warning
        RetroBox(shadowOffset = 4.dp, backgroundColor = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "${state.selectedRoot} ${state.selectedQuality}".uppercase(),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                if (activeVoicing != null && state.isValidSelection) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ChordGridCanvas(
                            voicing = activeVoicing,
                            isLeftHanded = state.isLeftHanded,
                            onStringTriggered = { stringIdx ->
                                viewModel.playSingleNote(stringIdx, audioEngine)
                            },
                            modifier = Modifier
                                .width(180.dp)
                                .fillMaxHeight()
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .border(1.5.dp, MaterialTheme.colorScheme.outline)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "NO CHORD SHAPES FOUND IN DATABASE.\n\nSELECTION HAS NO PLAYABLE SHAPES WITHIN THE 15TH FRET.",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }

        // D. strumming and Cycle Variations Controls
        RetroBox(shadowOffset = 3.dp) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Strum Trigger Button
                val isStrumEnabled = activeVoicing != null && state.isValidSelection
                val strumColor = if (isStrumEnabled) Color(0xFFCCCCCC) else Color.LightGray.copy(alpha = 0.5f)

                RetroButton(
                    text = "STRUM CHORD",
                    onClick = {
                        if (isStrumEnabled) {
                            viewModel.strumCurrentVoicing(audioEngine)
                        }
                    },
                    backgroundColor = strumColor,
                    modifier = Modifier.fillMaxWidth()
                )

                // Variation Pager Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val activeIndex = state.currentVoicingIndex
                    val hasPrev = activeIndex > 0 && state.isValidSelection
                    val hasNext = activeIndex < totalVoicings - 1 && state.isValidSelection

                    val prevBg = if (hasPrev) MaterialTheme.colorScheme.surface else Color.LightGray.copy(alpha = 0.3f)
                    val nextBg = if (hasNext) MaterialTheme.colorScheme.surface else Color.LightGray.copy(alpha = 0.3f)

                    RetroButton(
                        text = "< PREV",
                        onClick = { if (hasPrev) viewModel.setVoicingIndex(activeIndex - 1) },
                        backgroundColor = prevBg,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "SHAPE\n${if (totalVoicings > 0) (activeIndex + 1) else 0} / $totalVoicings",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(96.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    RetroButton(
                        text = "NEXT >",
                        onClick = { if (hasNext) viewModel.setVoicingIndex(activeIndex + 1) },
                        backgroundColor = nextBg,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // E. Footer Left-Handed Toggle Preference
        RetroButton(
            text = if (state.isLeftHanded) "LEFT-HANDED PLAY: ACTIVE" else "LEFT-HANDED PLAY: INACTIVE",
            onClick = { viewModel.toggleLeftHanded() },
            backgroundColor = if (state.isLeftHanded) Color(0xFFCCCCCC) else Color(0xFFE5E5E5),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
