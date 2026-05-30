package com.renium.sipkasku.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.renium.sipkasku.data.local.TransactionEntity
import androidx.compose.ui.graphics.Color
import com.renium.sipkasku.utils.formatRupiah
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun TransactionItem(
    transaction: TransactionEntity,
    pocketName: String? = null
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
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.title, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (!pocketName.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(pocketName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

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
