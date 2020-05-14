package com.example.silikahorlogo.ui

import androidx.compose.Composable
import androidx.ui.foundation.isSystemInDarkTheme
import androidx.ui.graphics.Color
import androidx.ui.material.MaterialTheme
import androidx.ui.material.darkColorPalette
import androidx.ui.material.lightColorPalette

private val DarkColorPalette = darkColorPalette(
    primary = purple200,
    primaryVariant = purple700,
    secondary = teal200
)

private val LightColorPalette = lightColorPalette(
    primary = purple500,
    primaryVariant = purple700,
    secondary = teal200,
    background = Color.Black,
    onBackground = Color.White

    /* Other default colors to override
background = Color.White,
surface = Color.White,
onPrimary = Color.White,
onSecondary = Color.Black,
onBackground = Color.Black,
onSurface = Color.Black,
*/
)

@Composable
fun SilikaHorloĝoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = darkColorPalette(),
        typography = typography,
        shapes = shapes,
        content = content
    )
}