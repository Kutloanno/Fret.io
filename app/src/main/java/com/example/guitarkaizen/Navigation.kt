package com.example.guitarkaizen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.runtime.NavKey
import com.example.guitarkaizen.ui.screens.practice.PracticeScreen
import com.example.guitarkaizen.ui.screens.resources.ResourcesScreen
import com.example.guitarkaizen.ui.screens.minigames.MiniGamesScreen
import com.example.guitarkaizen.ui.screens.minigames.ScaleDegreeGameScreen
import com.example.guitarkaizen.ui.screens.metronome.MetronomeScreen
import com.example.guitarkaizen.ui.screens.settings.SettingsScreen
import com.example.guitarkaizen.Tuner
import com.example.guitarkaizen.ui.screens.tuner.TunerScreen
import com.example.guitarkaizen.NoteFinder
import com.example.guitarkaizen.ui.screens.notefinder.NoteFinderScreen
import com.example.guitarkaizen.ui.screens.eartraining.EarTrainerHubScreen
import com.example.guitarkaizen.ui.screens.eartraining.EarTrainerArenaScreen
import com.example.guitarkaizen.ui.screens.eartraining.EarTrainerBuilderScreen
import com.example.guitarkaizen.ui.screens.eartraining.EarTrainerSettingsScreen
import com.example.guitarkaizen.ui.screens.chordlibrary.ChordLibraryScreen
import com.example.guitarkaizen.ChordLibrary
import com.example.guitarkaizen.ui.screens.fretboardexplorer.FretboardExplorerScreen
import com.example.guitarkaizen.FretboardExplorer




@Composable
fun MainNavigation() {
  val backStack = rememberNavBackStack(Practice)
  val currentKey = backStack.lastOrNull() ?: Practice

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.background
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // 1. Poolsuite Retro Header Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
          .border(2.dp, MaterialTheme.colorScheme.outline)
          .background(MaterialTheme.colorScheme.background)
          .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Settings Icon (☼ sun icon) -> Navigates to Settings!
        Box(
          modifier = Modifier
            .size(36.dp)
            .border(1.5.dp, MaterialTheme.colorScheme.outline)
            .background(MaterialTheme.colorScheme.surface)
            .clickable { backStack.add(Settings) },
          contentAlignment = Alignment.Center
        ) {
          Text("☼", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.weight(1f))

        // Center Title in blocky monospace
        Text(
          text = "FRET.IO",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
          letterSpacing = 2.sp,
          color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.weight(1f))

        // Dynamic stereo LED dot box
        Box(
          modifier = Modifier
            .size(36.dp)
            .border(1.5.dp, MaterialTheme.colorScheme.outline)
            .background(MaterialTheme.colorScheme.surface),
          contentAlignment = Alignment.Center
        ) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .background(Color(0xFF81C784)) // Green active LED
          )
        }
      }

      // 2. Active Screen Nav Container
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .background(MaterialTheme.colorScheme.background)
      ) {
        NavDisplay(
          backStack = backStack,
          onBack = {
            if (backStack.lastOrNull() != Practice) {
              backStack.clear()
              backStack.add(Practice)
            } else {
              backStack.removeLastOrNull()
            }
          },
          entryProvider =
            entryProvider {
              entry<Practice> {
                PracticeScreen(onNavigate = { key -> backStack.add(key) })
              }
              entry<Resources> {
                ResourcesScreen()
              }
              entry<MiniGames> {
                MiniGamesScreen(onNavigate = { key -> backStack.add(key) })
              }
              entry<Metronome> {
                MetronomeScreen(onBackClick = { backStack.removeLastOrNull() })
              }
              entry<Settings> {
                SettingsScreen(onBackClick = { backStack.removeLastOrNull() })
              }
              entry<ScaleDegreeGame> {
                ScaleDegreeGameScreen(onBackClick = { backStack.removeLastOrNull() })
              }
              entry<Tuner> {
                TunerScreen(onBackClick = { backStack.removeLastOrNull() })
              }
              entry<NoteFinder> {
                NoteFinderScreen(onBackClick = { backStack.removeLastOrNull() })
              }
              entry<EarTrainerHub> {
                EarTrainerHubScreen(
                  onBackClick = { backStack.removeLastOrNull() },
                  onNavigate = { key -> backStack.add(key) }
                )
              }
              entry<EarTrainerArena> {
                EarTrainerArenaScreen(onBackClick = { backStack.removeLastOrNull() })
              }
              entry<EarTrainerBuilder> {
                EarTrainerBuilderScreen(onBackClick = { backStack.removeLastOrNull() })
              }
              entry<EarTrainerSettings> {
                EarTrainerSettingsScreen(onBackClick = { backStack.removeLastOrNull() })
              }
              entry<ChordLibrary> {
                ChordLibraryScreen(onBackClick = { backStack.removeLastOrNull() })
              }
              entry<FretboardExplorer> {
                FretboardExplorerScreen(onBackClick = { backStack.removeLastOrNull() })
              }

            },
        )
      }

      // 3. Poolsuite Segmented Dock Navigation
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(72.dp)
          .border(2.dp, MaterialTheme.colorScheme.outline)
          .background(MaterialTheme.colorScheme.surface)
      ) {
        // Tab 1: Practice
        DockItem(
          title = "Practice",
          icon = Icons.Default.PlayArrow,
          isSelected = currentKey is Practice || currentKey is Metronome || currentKey is Settings || currentKey is Tuner || currentKey is NoteFinder || currentKey is EarTrainerHub || currentKey is EarTrainerArena || currentKey is EarTrainerBuilder || currentKey is EarTrainerSettings || currentKey is ChordLibrary || currentKey is FretboardExplorer,
          modifier = Modifier.weight(1f),
          onClick = {
            if (currentKey !is Practice) {
              backStack.clear()
              backStack.add(Practice)
            }
          }
        )

        // Column Divider
        Box(modifier = Modifier.fillMaxHeight().width(2.dp).background(MaterialTheme.colorScheme.outline))

        // Tab 2: Resources
        DockItem(
          title = "Resources",
          icon = Icons.Default.List,
          isSelected = currentKey is Resources,
          modifier = Modifier.weight(1f),
          onClick = {
            if (currentKey !is Resources) {
              backStack.clear()
              backStack.add(Resources)
            }
          }
        )

        // Column Divider
        Box(modifier = Modifier.fillMaxHeight().width(2.dp).background(MaterialTheme.colorScheme.outline))

        // Tab 3: Mini-Games (Arcade / Game screen stay highlighted)
        DockItem(
          title = "Arcade",
          icon = Icons.Default.Star,
          isSelected = currentKey is MiniGames || currentKey is ScaleDegreeGame,
          modifier = Modifier.weight(1f),
          onClick = {
            if (currentKey !is MiniGames) {
              backStack.clear()
              backStack.add(MiniGames)
            }
          }
        )
      }
    }
  }
}

@Composable
fun DockItem(
  title: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  isSelected: Boolean,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  val background = if (isSelected) Color(0xFFE5E5E5) else MaterialTheme.colorScheme.surface
  val textColor = MaterialTheme.colorScheme.onSurface

  Box(
    modifier = modifier
      .fillMaxHeight()
      .background(background)
      .clickable(onClick = onClick)
      .padding(4.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(
        imageVector = icon,
        contentDescription = title,
        tint = textColor,
        modifier = Modifier.size(24.dp)
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = title.uppercase(),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
        color = textColor,
        textAlign = TextAlign.Center
      )
    }
  }
}
