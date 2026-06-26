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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = AppsColors.NavyPrimary,
    onPrimary = Color.White,
    primaryContainer = AppsColors.LightBlue,
    onPrimaryContainer = AppsColors.NavyDark,

    secondary = AppsColors.NavyLight,
    onSecondary = Color.White,

    tertiary = AppsColors.LeafGreenDark,
    onTertiary = Color.White,

    background = AppsColors.LightBackground,
    onBackground = AppsColors.onLightSurface,
    surface = AppsColors.LightSurface,
    onSurface = AppsColors.onLightSurface
)

private val DarkColors = darkColorScheme(
    primary = AppsColors.PaleBlue,
    onPrimary = AppsColors.NavyDark,
    primaryContainer = AppsColors.NavyPrimary,
    onPrimaryContainer = AppsColors.LightBlue,

    secondary = AppsColors.SkyBlue,
    onSecondary = AppsColors.NavyDark,

    tertiary = AppsColors.LeafGreenLight,
    onTertiary = AppsColors.NavyDark,

    background = AppsColors.DarkBackground,
    onBackground = AppsColors.onDarkSurface,
    surface = AppsColors.DarkSurface,
    onSurface = AppsColors.onDarkSurface
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
