package com.renium.sipkasku.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.renium.sipkasku.data.local.TransactionEntity
import com.renium.sipkasku.data.repository.CategoryRepository
import com.renium.sipkasku.data.repository.PocketRepository
import com.renium.sipkasku.data.repository.TransactionRepository
import com.renium.sipkasku.navigation.Screen
import com.renium.sipkasku.ui.theme.AppsColors
import com.renium.sipkasku.utils.formatRupiah
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transactionId: Int,
    navController: NavController,
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository?,
    pocketRepository: PocketRepository?
) {
    val scope = rememberCoroutineScope()

    var transaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var categoryName by remember { mutableStateOf<String?>(null) }
    var pocketName by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(transactionId) {
        transaction = transactionRepository.getById(transactionId)
        transaction?.categoryId?.let { catId ->
            categoryName = categoryRepository?.getCategoryById(catId)?.name
        }
        transaction?.pocketId?.let { pocketId ->
            pocketName = pocketRepository?.getPocketById(pocketId)?.name
        }
    }

    val tx = transaction

    if(tx == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    val dateStr = Instant.ofEpochMilli(tx.date)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))

    if(showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Transaction") },
            text = { Text("Are you sure to delete \"${tx.title}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        tx.pocketId?.let  { pocketId->
                            val delta = if (tx.isIncome) -tx.amount else tx.amount
                            pocketRepository?.adjustBalance(pocketId, delta)
                        }
                        transactionRepository.deleteTransaction(tx)
                        navController.popBackStack()
                    }
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("cancel")
                }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FloatingActionButton(
                    onClick = { showDeleteDialog = true },
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                FloatingActionButton(
                    onClick = {
                        navController.navigate(Screen.EditTransaction.createRoute(tx.id))
                    }
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor =
                        if (tx.isIncome)
                            AppsColors.IncomeColor.copy(alpha = 0.1f)
                        else
                            AppsColors.ExpenseColor.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text (
                        if (tx.isIncome) "Income" else "Expense",
                        style = MaterialTheme.typography. labelLarge,
                        color = if (tx.isIncome) AppsColors.IncomeColor else AppsColors.ExpenseColor
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        formatRupiah(tx.amount),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (tx.isIncome) AppsColors.IncomeColor else AppsColors.ExpenseColor
                    )
                }
            }
            DetailSection(label = "Title", value = tx.title)
            DetailSection(label = "Date", value = dateStr)
            DetailSection(label = "Category", value = categoryName ?: "-")
            DetailSection(label = "Pocket", value = pocketName ?: "-")
            DetailSection(label = "Note", value = tx.note?: "")
        }
    }
}

@Composable
private fun DetailSection(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}