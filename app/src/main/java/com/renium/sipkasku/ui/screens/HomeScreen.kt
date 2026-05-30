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
import androidx.compose.material.icons.filled.Sort
import androidx.navigation.NavController
import com.renium.sipkasku.data.repository.TransactionRepository
import com.renium.sipkasku.ui.components.BalanceCard
import com.renium.sipkasku.ui.components.EmptyState
import com.renium.sipkasku.ui.components.SwipeableTransactionItem
import com.renium.sipkasku.viewmodel.HomeViewModel
import com.renium.sipkasku.viewmodel.TransactionViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    navController: NavController,
    repository: TransactionRepository,
    snackbarHostState: SnackbarHostState,
    pocketRepository: com.renium.sipkasku.data.repository.PocketRepository? = null
) {
    val viewModel: HomeViewModel = viewModel(
        factory = TransactionViewModelFactory(repository)
    )

    val transactions by viewModel
        .visibleTransactions
        .collectAsState()

    val balance = transactions.sumOf { transaction ->
        if (transaction.isIncome) transaction.amount else -transaction.amount
    }

    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            BalanceCard(balance = balance)

            Spacer(modifier = Modifier.height(24.dp))

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
                var sortExpanded by remember { mutableStateOf(false) }
                IconButton(onClick = { sortExpanded = true }) {
                    Icon(Icons.Default.Sort, contentDescription = "Sort")
                }
                DropdownMenu(expanded = sortExpanded, onDismissRequest = { sortExpanded = false }) {
                    DropdownMenuItem(text = { Text("Date ↓") }, onClick = { viewModel.setSort(com.renium.sipkasku.viewmodel.HomeViewModel.SortType.DATE_DESC); sortExpanded = false })
                    DropdownMenuItem(text = { Text("Date ↑") }, onClick = { viewModel.setSort(com.renium.sipkasku.viewmodel.HomeViewModel.SortType.DATE_ASC); sortExpanded = false })
                    DropdownMenuItem(text = { Text("Amount ↓") }, onClick = { viewModel.setSort(com.renium.sipkasku.viewmodel.HomeViewModel.SortType.AMOUNT_DESC); sortExpanded = false })
                    DropdownMenuItem(text = { Text("Amount ↑") }, onClick = { viewModel.setSort(com.renium.sipkasku.viewmodel.HomeViewModel.SortType.AMOUNT_ASC); sortExpanded = false })
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Single row of filter chips (All / Income / Expense)
            val currentFilter by viewModel.currentFilter.collectAsState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = currentFilter == com.renium.sipkasku.viewmodel.HomeViewModel.FilterType.ALL,
                    onClick = { viewModel.setFilter(com.renium.sipkasku.viewmodel.HomeViewModel.FilterType.ALL) },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = currentFilter == com.renium.sipkasku.viewmodel.HomeViewModel.FilterType.INCOME,
                    onClick = { viewModel.setFilter(com.renium.sipkasku.viewmodel.HomeViewModel.FilterType.INCOME) },
                    label = { Text("Income") }
                )
                FilterChip(
                    selected = currentFilter == com.renium.sipkasku.viewmodel.HomeViewModel.FilterType.EXPENSE,
                    onClick = { viewModel.setFilter(com.renium.sipkasku.viewmodel.HomeViewModel.FilterType.EXPENSE) },
                    label = { Text("Expense") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (transactions.isEmpty()) {

                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    EmptyState()
                }

            } else {

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(transactions) { transaction ->

                        SwipeableTransactionItem(
                            transaction = transaction,
                            onDelete = {

                                viewModel.deleteTransaction(transaction)

                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Transaction deleted",
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
}
