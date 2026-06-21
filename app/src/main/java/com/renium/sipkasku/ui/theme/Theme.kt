package com.renium.sipkasku.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = White,
    primaryContainer = LightBlue,
    onPrimaryContainer = NavyDark,

    secondary = NavyLight,
    onSecondary = White,

    tertiary = LeafGreenDark,
    onTertiary = White,

    background = LightBackground,
    onBackground = onLightSurface,
    surface = LightSurface,
    onSurface = onLightSurface
)

private val DarkColors = darkColorScheme(
    primary = PaleBlue,
    onPrimary = NavyDark,
    primaryContainer = NavyPrimary,
    onPrimaryContainer = LightBlue,

    secondary = SkyBlue,
    onSecondary = NavyDark,

    tertiary = LeafGreenLight,
    onTertiary = NavyDark,

    background = DarkBackground,
    onBackground = onDarkSurface,
    surface = DarkSurface,
    onSurface = onDarkSurface
)

@Composable
fun SipKasKuTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colors = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (useDarkTheme) DarkColors else LightColors
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}
