package com.renium.sipkasku.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.renium.sipkasku.model.Transaction
import androidx.compose.ui.graphics.Color

@Composable
fun TransactionItem(
    transaction: Transaction
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(transaction.title)
            Text(
                text =
                    if (transaction.isIncome)
                        "+ Rp ${transaction.amount}"
                    else
                        "- Rp ${transaction.amount}",
                color =
                    if (transaction.isIncome)
                        Color.Green
                    else
                        Color.Red
            )
        }
    }
}
