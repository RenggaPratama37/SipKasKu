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

@Composable
fun HomeScreen(
    navController: NavController
) {
    val transactions = listOf(
        Transaction("Coffee", 25000.0, false),
        Transaction("Food", 50000.0, false),
        Transaction("Salary", 3000000.0, true)
    )

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
                balance = 2925000.0
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
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
