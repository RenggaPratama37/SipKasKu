package com.renium.sipkasku.ui.layout

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.renium.sipkasku.data.repository.TransactionRepository
import com.renium.sipkasku.navigation.Screen
import com.renium.sipkasku.ui.screens.AddTransactionScreen
import com.renium.sipkasku.ui.screens.HomeScreen
import com.renium.sipkasku.ui.screens.SettingsScreen
import com.renium.sipkasku.ui.screens.StatisticsScreen
import com.renium.sipkasku.ui.screens.settings.AppearanceSettingsScreen
import com.renium.sipkasku.ui.screens.settings.PocketSettingsScreen
import com.renium.sipkasku.ui.screens.settings.CategorySettingsScreen
import com.renium.sipkasku.ui.screens.settings.RecurringSettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    repository: TransactionRepository,
    pocketRepository: com.renium.sipkasku.data.repository.PocketRepository? = null,
    categoryRepository: com.renium.sipkasku.data.repository.CategoryRepository? = null,
    recurringRepository: com.renium.sipkasku.data.repository.RecurringRepository? = null,
    settingsRepository: com.renium.sipkasku.data.repository.SettingsRepository? = null
) {

    val navController = rememberNavController()

    val items = listOf(
        Screen.Home,
        Screen.Statistics,
        Screen.Settings
    )

    val currentRoute by navController
        .currentBackStackEntryAsState()

    val route = currentRoute
        ?.destination
        ?.route
    
     
    val isRootScreen = route in listOf(
        Screen.Home.route,
        Screen.Statistics.route,
        Screen.Settings.route
    )

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            )
        },

        topBar = {
            TopAppBar(
                expandedHeight = 58.dp,
                title = {
                    Text(
                        text = when(route) {
                            Screen.Home.route -> "SipKasKu"
                            Screen.Statistics.route -> "Statistics"
                            Screen.Settings.route -> "Settings"
                            Screen.AddTransaction.route -> "Add Transaction"

                            "pocket_settings" -> "Pocket Settings"
                            "category_settings" -> "Category Settings"
                            "appearance_settings" -> "Appearance Settings"
                            "recurring_settings" -> "Systematic Recurring Transaction"

                            else -> "SipKasku"
                        }
                    )
                },
                navigationIcon = {
                    if(!isRootScreen) {
                        IconButton(
                            onClick = {
                                navController.popBackStack()
                            }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                }
            )
        },

        bottomBar = {
            NavigationBar {
                items.forEach { screen ->
                    NavigationBarItem(
                        selected =
                            route == screen.route,
                        onClick = {
                            navController.navigate(
                                screen.route
                            ) {
                                popUpTo(
                                    Screen.Home.route
                                )
                                launchSingleTop = true
                            }
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

            if (
                route == Screen.Home.route
            ) {
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
        }
    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(
                Screen.Home.route
            ) {

                HomeScreen(
                    navController = navController,
                    repository = repository,
                    snackbarHostState = snackbarHostState,
                    pocketRepository = pocketRepository
                )
            }

            composable(
                Screen.Statistics.route
            ) {
                StatisticsScreen(
                    navController = navController,
                    repository = repository,
                    categoryRepository = categoryRepository
                )
            }

            composable(
                Screen.Settings.route
            ) {
                SettingsScreen(
                    navController = navController,
                    settingsRepository = settingsRepository
                )
            }

            composable(
                Screen.AddTransaction.route
            ) {
                AddTransactionScreen(
                    navController = navController,
                    repository = repository,
                    pocketRepository = pocketRepository,
                    categoryRepository = categoryRepository,
                    settingsRepository = settingsRepository
                )
            }

            composable("appearance_settings") {
                AppearanceSettingsScreen(
                    settingsRepository = settingsRepository
                )
            }

            composable("pocket_settings") {
                PocketSettingsScreen(
                    transactionRepository = repository,
                    pocketRepository = pocketRepository,
                    settingsRepository = settingsRepository
                )
            }

            composable("category_settings") {
                CategorySettingsScreen(
                    transactionRepository = repository,
                    categoryRepository = categoryRepository,
                    settingsRepository = settingsRepository
                )
            }

            composable("recurring_settings") {
                RecurringSettingsScreen(
                    recurringRepository = recurringRepository,
                    settingsRepository = settingsRepository
                )
            }
        }
    }
}
