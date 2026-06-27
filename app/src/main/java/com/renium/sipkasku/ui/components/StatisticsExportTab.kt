package com.renium.sipkasku.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.renium.sipkasku.ui.screens.MonthSelector
import com.renium.sipkasku.utils.exportToExcel
import com.renium.sipkasku.utils.shareExcelFile
import com.renium.sipkasku.viewmodel.StatisticsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter
import java.util.Locale

// Tab 4: Export (placeholder — Step 5)
@Composable
fun ExportTab(viewModel: StatisticsViewModel) {
    val exportData by viewModel.exportTransactions.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val monthLabel = selectedMonth.format(
        DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("en-ID"))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MonthSelector(
            selectedMonth = selectedMonth,
            onPrevious = { viewModel.selectMonth(selectedMonth.minusMonths(1)) },
            onNext = { viewModel.selectMonth(selectedMonth.plusMonths(1)) }
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors (
                MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Export Report", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${exportData.size} transactions in ${selectedMonth.format(
                        DateTimeFormatter.ofPattern("MMMM yyyy",
                            Locale.forLanguageTag("en-ID"))
                    )}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            val file = exportToExcel(context, exportData, monthLabel)
                            withContext(Dispatchers.Main) {
                                shareExcelFile(context, file)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = exportData.isNotEmpty()
                ) {
                    Icon(Icons.Default.FileDownload, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Export Excel (.xlsx)")
                }
            }
        }

    }
}
