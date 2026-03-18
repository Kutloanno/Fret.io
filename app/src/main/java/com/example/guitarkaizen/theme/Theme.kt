package com.example.guitarkaizen.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
  primary = PoolsuiteWhite,
  secondary = PoolsuiteMediumGrey,
  tertiary = PoolsuiteLightGrey,
  background = PoolsuiteDarkBg,
  surface = PoolsuiteDarkSurface,
  onBackground = PoolsuiteWhite,
  onSurface = PoolsuiteWhite,
  outline = PoolsuiteWhite,
  surfaceVariant = PoolsuiteDarkSurface,
  onSurfaceVariant = PoolsuiteWhite
)

private val LightColorScheme = lightColorScheme(
  primary = PoolsuiteBlack,
  secondary = PoolsuiteLightGrey,
  tertiary = PoolsuiteMediumGrey,
  background = PoolsuiteBeige,
  surface = PoolsuiteWhite,
  onBackground = PoolsuiteBlack,
  onSurface = PoolsuiteBlack,
  outline = PoolsuiteBlack,
  surfaceVariant = PoolsuiteBeige,
  onSurfaceVariant = PoolsuiteBlack
)

// sharp borders
val PoolsuiteShapes = Shapes(
  small = RoundedCornerShape(0.dp),
  medium = RoundedCornerShape(0.dp),
  large = RoundedCornerShape(0.dp)
)

@Composable
fun GuitarKaizenTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    shapes = PoolsuiteShapes,
    content = content
  )
}
