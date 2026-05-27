package com.renium.sipkasku.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {

    object Home : Screen(
        route = "home",
        title = "Home",
        icon = Icons.Default.Home
    )

    object Statistics : Screen(
        route = "statistics",
        title = "Statistics",
        icon = Icons.Default.BarChart
    )

    object Settings : Screen(
        route = "settings",
        title = "Settings",
        icon = Icons.Default.Settings
    )

    object AddTransaction : Screen(
        route = "add_transaction",
        title = "Add",
        icon = Icons.Default.Add
    )
}
