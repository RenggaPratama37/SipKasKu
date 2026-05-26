package com.renium.sipkasku.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.renium.sipkasku.ui.screens.AddTransactionScreen
import com.renium.sipkasku.ui.screens.HomeScreen
import com.renium.sipkasku.ui.screens.SettingsScreen
import com.renium.sipkasku.ui.screens.StatisticsScreen
import com.renium.sipkasku.data.repository.TransactionRepository

@Composable
fun NavGraph(repository: TransactionRepository) {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {

        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                repository = repository
            )
        }

        composable(Screen.AddTransaction.route) {
            AddTransactionScreen(
                navController = navController,
                repository = repository
            )

        }

        composable(Screen.Statistics.route) {
            StatisticsScreen(navController)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(navController)
        }
    }
}
