package com.renium.sipkasku.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.NavController
import com.renium.sipkasku.data.repository.CategoryRepository
import com.renium.sipkasku.data.repository.PocketRepository
import com.renium.sipkasku.data.repository.TransactionRepository
import com.renium.sipkasku.navigation.Screen
import com.renium.sipkasku.ui.components.BalanceCard
import com.renium.sipkasku.ui.components.EmptyState
import com.renium.sipkasku.ui.components.TransactionItem
import com.renium.sipkasku.viewmodel.HomeViewModel
import com.renium.sipkasku.viewmodel.TransactionViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    repository: TransactionRepository,
    snackbarHostState: SnackbarHostState,
    pocketRepository: PocketRepository? = null,
    categoryRepository: CategoryRepository? = null,
    navController: NavController
) {
    val viewModel: HomeViewModel = viewModel(
        factory = TransactionViewModelFactory(repository, pocketRepository)
    )

    val transactions by viewModel
        .visibleTransactions
        .collectAsState()

    val allTransactions by viewModel
        .transactions
        .collectAsState()

    val currentSort by viewModel
        .currentSort
        .collectAsState()
    
    // load pockets map if repository provided
    val pocketsList by pocketRepository?.getAllPockets()?.collectAsState(initial = emptyList())
        ?: remember { mutableStateOf(emptyList()) }
    val pocketsMap = remember(pocketsList) { pocketsList.associateBy { it.id } }

    val categoriesList by categoryRepository?.getAll()?.collectAsState(initial = emptyList())
        ?: remember { mutableStateOf(emptyList<com.renium.sipkasku.data.local.Category>()) }
    val categoriesMap = remember(categoriesList) { categoriesList.associateBy { it.id } }

    val totalIncome = allTransactions
        .filter{it.isIncome}
        .sumOf{ transaction -> transaction.amount }

    val totalExpense = allTransactions
        .filter{!it.isIncome}
        .sumOf { transaction -> transaction.amount}

    val totalBalance = allTransactions.sumOf { transaction ->
        if (transaction.isIncome)
            transaction.amount
        else
            -transaction.amount
    }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = 8.dp,
                vertical = 4.dp
            )
    ) {

        BalanceCard(totalBalance = totalBalance, totalIncome = totalIncome, totalExpense = totalExpense)

        Spacer(modifier = Modifier.height(4.dp))

        // Header: title + sort (sort aligned to the right like a file manager)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.weight(1f))

            // Sort dropdown on the right
            var sortExpanded by rememberSaveable{mutableStateOf(false)}
            Box{
                ElevatedCard(
                    onClick = {
                        sortExpanded = true
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 8.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            when(currentSort){
                                HomeViewModel.SortType.DATE_DESC -> "Date ↓"
                                HomeViewModel.SortType.DATE_ASC -> "Date ↑"
                                HomeViewModel.SortType.AMOUNT_DESC -> "Amount ↓"
                                HomeViewModel.SortType.AMOUNT_ASC -> "Amount ↑"
                            }
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.Sort,
                            contentDescription = null
                        )
                    }
                }
                DropdownMenu( expanded = sortExpanded, onDismissRequest = { sortExpanded = false }) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (
                                    currentSort == HomeViewModel.SortType.DATE_ASC || currentSort == HomeViewModel.SortType.DATE_DESC) {
                                    if (currentSort == HomeViewModel.SortType.DATE_ASC)
                                        "Date ↑"
                                    else
                                        "Date ↓"
                                } else {
                                    "Date"
                                }
                            )
                        },
                        onClick = {
                            viewModel.setSort(
                                when (currentSort) {
                                    HomeViewModel.SortType.DATE_ASC -> HomeViewModel.SortType.DATE_DESC
                                else ->
                                    HomeViewModel.SortType.DATE_ASC
                                }
                            )
                            sortExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (currentSort == HomeViewModel.SortType.AMOUNT_ASC || currentSort == HomeViewModel.SortType.AMOUNT_DESC) {
                                    if (currentSort == HomeViewModel.SortType.AMOUNT_ASC) "Amount ↑"
                                    else "Amount ↓"
                                } else {
                                    "Amount"
                                }
                            )
                        },
                        onClick = {
                            viewModel.setSort(
                                when (currentSort) {
                                    HomeViewModel.SortType.AMOUNT_ASC ->
                                        HomeViewModel.SortType.AMOUNT_DESC
                               else -> HomeViewModel.SortType.AMOUNT_ASC
                                }
                            )
                            sortExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Single row of filter chips (All / Income / Expense)
        val currentFilter by viewModel.currentFilter.collectAsState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FilterChip(
                selected = currentFilter == HomeViewModel.FilterType.ALL,
                onClick = { viewModel.setFilter(HomeViewModel.FilterType.ALL) },
                label = { Text("All") }
            )
            FilterChip(
                selected = currentFilter == HomeViewModel.FilterType.INCOME,
                onClick = { viewModel.setFilter(HomeViewModel.FilterType.INCOME) },
                label = { Text("Income") }
            )
            FilterChip(
                selected = currentFilter == HomeViewModel.FilterType.EXPENSE,
                onClick = { viewModel.setFilter(HomeViewModel.FilterType.EXPENSE) },
                label = { Text("Expense") }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (transactions.isEmpty()) {

            Box(
                modifier = Modifier.weight(1f)
            ) {
                EmptyState()
            }

        } else {

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(
                    items = transactions,
                    key = { it.id }
                ) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        pocketName = transaction.pocketId?.let { pocketsMap[it]?.name },
                        categoryName = transaction.categoryId?.let { categoriesMap[it]?.name },
                        onClick = {
                            navController.navigate(Screen.TransactionDetail.createRoute(transaction.id))
                        },
                        onEdit = {
                            navController.navigate(Screen.EditTransaction.createRoute(transaction.id))
                        },
                        onDelete = {
                            viewModel.deleteTransaction(transaction)
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Transaction has deleted",
                                    actionLabel = "UNDO"
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.restoreTransaction(transaction)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

