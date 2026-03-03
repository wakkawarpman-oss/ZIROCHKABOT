package com.zirochka.pos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = ZiroBlue,
    secondary = ZiroGold,
    background = ZiroSurface,
    surface = ZiroSurface
)

private val DarkColors = darkColorScheme(
    primary = ZiroGold,
    secondary = ZiroBlue,
    background = ZiroNight,
    surface = ZiroNight
)

@Composable
fun ZirochkaTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
