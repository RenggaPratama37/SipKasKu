package com.renium.sipkasku.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.renium.sipkasku.data.local.TransactionEntity
import androidx.compose.ui.graphics.Color
import com.renium.sipkasku.utils.formatRupiah

@Composable
fun TransactionItem(
    transaction: TransactionEntity
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
                text = formatRupiah(transaction.amount),
                color =
                    if (transaction.isIncome)
                        Color.Green
                    else
                        Color.Red
            )
        }
    }
}
