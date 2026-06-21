package com.payalert.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightScheme = lightColorScheme(
    primary = Accent,
    secondary = Accent,
    tertiary = Accent,
)

private val DarkScheme = darkColorScheme(
    primary = Accent,
    secondary = Accent,
    tertiary = Accent,
)

@Composable
fun PayAlertTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightScheme,
        typography = Typography,
        content = content,
    )
}
