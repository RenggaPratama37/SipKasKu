package com.renium.sipkasku.ui.layout

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.renium.sipkasku.navigation.Screen
import com.renium.sipkasku.data.repository.TransactionRepository
import com.renium.sipkasku.ui.screens.AddTransactionScreen
import com.renium.sipkasku.ui.screens.HomeScreen
import com.renium.sipkasku.ui.screens.SettingsScreen
import com.renium.sipkasku.ui.screens.StatisticsScreen
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    repository : TransactionRepository
) {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Home,
        Screen.Statistics,
        Screen.Settings
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SipKasKu") }
            )
        },
        bottomBar = {
            NavigationBar {
                val currentRoute =
                    navController
                        .currentBackStackEntryAsState()
                        .value
                        ?.destination
                        ?.route
                items.forEach { screen ->
                    NavigationBarItem(
                        selected =
                            currentRoute == screen.route,
                        onClick = {
                            navController.navigate(
                                screen.route
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(screen.title)
                        }
                    )
                }
            }
        },
        floatingActionButton = {

            FloatingActionButton(
                onClick = {
                    navController.navigate(
                        Screen.AddTransaction.route
                    )
                }
            ) {
                Icon(
                    imageVector = Screen.AddTransaction.icon,
                    contentDescription = "Add"
                )
            }
        }
    ){
        padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
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
}
