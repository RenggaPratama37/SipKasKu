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
    snackbarHostState: SnackbarHostState
) {
    val viewModel: HomeViewModel = viewModel(
        factory = TransactionViewModelFactory(repository)
    )

    val transactions by viewModel
        .transactions
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

            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleLarge
            )

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
