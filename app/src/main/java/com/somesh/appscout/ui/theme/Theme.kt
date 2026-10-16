package com.somesh.appscout.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val PaddockColors = darkColorScheme(
    primary = RacingRed,
    onPrimary = Color.White,
    primaryContainer = RacingRedDeep,
    onPrimaryContainer = Chalk,
    secondary = EmberOrange,
    onSecondary = Asphalt,
    tertiary = TrackLime,
    onTertiary = Asphalt,
    background = Asphalt,
    onBackground = Chalk,
    surface = Carbon,
    onSurface = Chalk,
    surfaceVariant = Graphite,
    onSurfaceVariant = Smoke,
    surfaceContainer = Carbon,
    surfaceContainerHigh = Graphite,
    surfaceContainerHighest = Slate,
    outline = Slate,
    outlineVariant = Color(0xFF2A2F38),
    error = Color(0xFFFF4D4D),
    onError = Color.White
)

/**
 * Mostly square corners. Motorsport UI reads as engineering, not as a consumer
 * chat app, so radii stay small and are used deliberately.
 */
private val PaddockShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(6.dp),
    large = RoundedCornerShape(10.dp),
    extraLarge = RoundedCornerShape(16.dp)
)

@Composable
fun PaddockTheme(content: @Composable () -> Unit) = MaterialTheme(
    colorScheme = PaddockColors,
    typography = Typography,
    shapes = PaddockShapes,
    content = content
)
