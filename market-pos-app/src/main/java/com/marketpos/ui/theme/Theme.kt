package com.marketpos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.marketpos.domain.model.AppThemeMode

private val LightColors = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF1F6A5A),
    secondary = androidx.compose.ui.graphics.Color(0xFF4E635D),
    tertiary = androidx.compose.ui.graphics.Color(0xFF8B5E34)
)
private val DarkColors = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF80D8C3),
    secondary = androidx.compose.ui.graphics.Color(0xFFB5CCC4),
    tertiary = androidx.compose.ui.graphics.Color(0xFFFFC58A)
)

@Composable
fun MarketPosTheme(
    themeMode: AppThemeMode = AppThemeMode.LIGHT,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (themeMode == AppThemeMode.DARK) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
