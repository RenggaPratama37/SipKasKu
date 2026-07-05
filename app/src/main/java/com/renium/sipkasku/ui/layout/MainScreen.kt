package com.renium.sipkasku.ui.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.renium.sipkasku.data.local.Category
import com.renium.sipkasku.data.repository.CategoryRepository
import com.renium.sipkasku.data.repository.PocketRepository
import com.renium.sipkasku.data.repository.RecurringRepository
import com.renium.sipkasku.data.repository.SettingsRepository
import com.renium.sipkasku.data.repository.TransactionRepository
import com.renium.sipkasku.navigation.Screen
import com.renium.sipkasku.ui.screens.AddTransactionScreen
import com.renium.sipkasku.ui.screens.HomeScreen
import com.renium.sipkasku.ui.screens.SettingsScreen
import com.renium.sipkasku.ui.screens.StatisticsScreen
import com.renium.sipkasku.ui.screens.TransactionDetailScreen
import com.renium.sipkasku.ui.screens.settings.AddRecurringScreen
import com.renium.sipkasku.ui.screens.settings.AppearanceSettingsScreen
import com.renium.sipkasku.ui.screens.settings.CategorySettingsScreen
import com.renium.sipkasku.ui.screens.settings.PocketSettingsScreen
import com.renium.sipkasku.ui.screens.settings.RecurringSettingsScreen
import com.renium.sipkasku.ui.theme.AppsColors
import com.renium.sipkasku.viewmodel.CategoryViewModel
import com.renium.sipkasku.viewmodel.CategoryViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    repository: TransactionRepository,
    pocketRepository: PocketRepository? = null,
    categoryRepository: CategoryRepository? = null,
    recurringRepository: RecurringRepository? = null,
    settingsRepository: SettingsRepository? = null
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

    val scope = rememberCoroutineScope()

    if (categoryRepository == null || recurringRepository == null) return

    val categoryViewModel: CategoryViewModel = viewModel (
        factory = CategoryViewModelFactory(
            categoryRepository = categoryRepository,
            transactionRepository = repository,
            recurringRepository = recurringRepository
        )
    )

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            )
        },

        topBar = {
            TopAppBar(
                expandedHeight = 64.dp,
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

                            "transaction_detail/{transactionId}" -> "Transaction Detail"
                            "edit_transaction/{transactionId}" -> "Edit Transaction"

                            "add_recurring" -> "Create Recurring Plan"

                            else -> "SipKasku"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        color = AppsColors.LeafGreen,
                        fontWeight = FontWeight.Bold
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
            AnimatedVisibility (
                visible =  isRootScreen,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
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
                    },
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
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
                    repository = repository,
                    snackbarHostState = snackbarHostState,
                    pocketRepository = pocketRepository,
                    navController = navController
                )
            }

            composable(
                Screen.Statistics.route
            ) {
                StatisticsScreen(
                    repository = repository,
                    categoryRepository = categoryRepository,
                    pocketRepository = pocketRepository
                )
            }

            composable(
                Screen.Settings.route
            ) {
                SettingsScreen(
                    navController = navController
                )
            }

            composable(
                Screen.AddTransaction.route
            ) {
                AddTransactionScreen(
                    navController = navController,
                    repository = repository,
                    pocketRepository = pocketRepository,
                    categoryRepository = categoryRepository
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
                    pocketRepository = pocketRepository
                )
            }

            composable("category_settings") {

                val incomeCategories by remember(categoryRepository) {
                    categoryRepository.getByType("INCOME")
                }.collectAsState(initial = emptyList())

                val expenseCategories by remember(categoryRepository) {
                    categoryRepository.getByType("EXPENSE")
                }.collectAsState(initial = emptyList())

                CategorySettingsScreen(
                    incomeCategories = incomeCategories,
                    expenseCategories = expenseCategories,

                    onAddCategory = { name, type ->
                        scope.launch {
                            categoryRepository.insert(
                                Category(
                                    name = name,
                                    type = type
                                )
                            )
                        }
                    },

                    onRequestDeleteCategory = {
                        categoryViewModel.requestDeleteCategory(it)
                    },

                    pendingDeleteCategory = categoryViewModel.pendingDeleteCategory,

                    pendingDeleteCount = categoryViewModel.pendingDeleteTransactionCount,

                    pendingDeleteRecurringCount = categoryViewModel.pendingDeleteRecurringCount,

                    onConfirmDelete = { categoryViewModel.confirmDeleteCategory() },

                    onCancelDelete = { categoryViewModel.cancelDeleteCategory() }
                )
            }

            composable("recurring_settings") {
                RecurringSettingsScreen(
                    navController = navController,
                    recurringRepository = recurringRepository,
                    categoryRepository = categoryRepository,
                    pocketRepository = pocketRepository
                )
            }

            composable(
                "transaction_detail/{transactionId}",
                arguments = listOf(navArgument("transactionId") { type = NavType.IntType})
            ) { backStack ->
                val txId = backStack.arguments?.getInt("transactionId")?: return@composable
                TransactionDetailScreen(
                    transactionId = txId,
                    navController= navController,
                    transactionRepository = repository,
                    categoryRepository = categoryRepository,
                    pocketRepository = pocketRepository
                )
            }

            composable(
                "edit_transaction/{transactionId}",
                arguments = listOf(navArgument("transactionId") { type = NavType.IntType})
            ) { backStack ->
                val txId = backStack.arguments?.getInt("transactionId") ?: return@composable
                AddTransactionScreen(
                    navController = navController,
                    repository = repository,
                    pocketRepository = pocketRepository,
                    categoryRepository = categoryRepository,
                    transactionId = txId
                )
            }

            composable(
                "add_recurring"
            ) {
                AddRecurringScreen(
                    navController = navController,
                    recurringRepository = recurringRepository,
                    categoryRepository = categoryRepository,
                    pocketRepository = pocketRepository
                )
            }
        }
    }
}
