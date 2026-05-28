package com.renium.sipkasku.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.renium.sipkasku.data.repository.TransactionRepository
import com.renium.sipkasku.utils.formatRupiah
import com.renium.sipkasku.viewmodel.StatisticsViewModel
import com.renium.sipkasku.viewmodel.TransactionViewModelFactory

@Composable
fun StatisticsScreen(
    navController: NavController,
    repository: TransactionRepository
) {
    val viewModel: StatisticsViewModel = viewModel(
        factory = TransactionViewModelFactory(repository)
    )
    val income by viewModel
        .totalIncome
        .collectAsState()

    val expense by viewModel
        .totalExpense
        .collectAsState()
    val balance = income - expense

    Column(
        modifier = Modifier
            .padding(16.dp),

        verticalArrangement =
            Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text("Total Income")

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = formatRupiah(income),
                    style = MaterialTheme
                        .typography
                        .headlineSmall
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text("Total Expense")

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = formatRupiah(expense),
                    style = MaterialTheme
                        .typography
                        .headlineSmall
                )
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text("Current Balance")
                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = formatRupiah(balance),
                    style = MaterialTheme
                        .typography
                        .headlineSmall
                )
            }
        }
    }

}
