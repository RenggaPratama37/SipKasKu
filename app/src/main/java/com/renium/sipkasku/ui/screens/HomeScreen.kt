package com.renium.sipkasku.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.renium.sipkasku.navigation.Screen
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.renium.sipkasku.model.Transaction
import com.renium.sipkasku.ui.components.BalanceCard
import com.renium.sipkasku.ui.components.TransactionItem
import com.renium.sipkasku.ui.components.EmptyState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.renium.sipkasku.viewmodel.HomeViewModel
import com.renium.sipkasku.viewmodel.TransactionViewModelFactory
import com.renium.sipkasku.data.repository.TransactionRepository
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun HomeScreen(
    navController: NavController,
    repository: TransactionRepository
) {

    val viewModel: HomeViewModel = viewModel(
        factory = TransactionViewModelFactory(repository)
    )

    val transactions by viewModel
        .transactions
        .collectAsState()

    val balance = transactions.sumOf { transaction ->
        if (transaction.isIncome) {
            transaction.amount
        } else {
            -transaction.amount
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.AddTransaction.route)
                }
            ) {
                Text("+")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            BalanceCard(
                balance = balance
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (transactions.isEmpty()) {
                Box(Modifier.weight(1f)) {
                    EmptyState()
                }
            } else {
                LazyColumn(
                    Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    items(transactions) { transaction ->

                        TransactionItem(
                            transaction = transaction
                        )
                    }
                }
            }
        }
    }
}
