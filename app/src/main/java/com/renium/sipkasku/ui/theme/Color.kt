package com.renium.sipkasku.ui.theme

import androidx.compose.ui.graphics.Color
import android.graphics.Color as AndroidColor

object AppsColors {
    // Extracted color from Apps logo
    // Main color from wallet
    val NavyPrimary = Color(0xFF003354)
    val NavyLight = Color(0xFF004D7A)
    val NavyDark = Color(0XFF001E35)

    // Accent color from leaf
    val LeafGreen = Color(0xFF24A633)
    val LeafGreenDark = Color(0xFF006E1C)
    val LeafGreenLight = Color(0XFF7CFF7B)

    // Accent color (Light Mode)
    val LightBackground = Color(0xFFF8F9FC)
    val LightSurface = Color(0xFFFFFFFF)
    val onLightSurface = Color(0xFF191C1E)

    // Accent color (Dark Mode)
    val DarkBackground = Color(0xFF0B141A)
    val DarkSurface = Color(0xFF111F29)
    val onDarkSurface = Color(0xFFE2E2E5)

    // Additional color
    val LightBlue = Color(0xFFD4E4FF)
    val SkyBlue = Color(0xFF86CFFF)
    val PaleBlue = Color(0xFF9ECAFF)

    // Transaction color
    val IncomeColor = Color(0xFF16A34A)
    val ExpenseColor = Color(0xFFE53935)

    //Chart Colors
    val chartColors = listOf(
        AndroidColor.rgb(33, 150, 243),
        AndroidColor.rgb(76, 175, 80),
        AndroidColor.rgb(255, 152, 0),
        AndroidColor.rgb(233, 30, 99),
        AndroidColor.rgb(156, 39, 176),
        AndroidColor.rgb(0, 188, 212),
        AndroidColor.rgb(255, 87, 34),
        AndroidColor.rgb(96, 125, 139)
    )
}
