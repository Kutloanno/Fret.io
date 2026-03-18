package com.example.guitarkaizen.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Thread-safe global reactive state manager for the application theme.
 * Toggling ThemeConfig.isDarkTheme automatically triggers recomposition of the root GuitarKaizenTheme.
 */
object ThemeConfig {
  var isDarkTheme by mutableStateOf(false)
}
