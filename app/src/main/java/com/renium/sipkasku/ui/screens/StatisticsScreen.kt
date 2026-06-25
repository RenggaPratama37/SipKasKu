package com.renium.sipkasku.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.renium.sipkasku.data.local.Pocket
import com.renium.sipkasku.data.repository.CategoryRepository
import com.renium.sipkasku.data.repository.PocketRepository
import com.renium.sipkasku.data.repository.TransactionRepository
import com.renium.sipkasku.ui.components.CategoriesTab
import com.renium.sipkasku.ui.components.ExportTab
import com.renium.sipkasku.ui.components.OverviewTab
import com.renium.sipkasku.ui.components.TrendsTab
import com.renium.sipkasku.viewmodel.StatisticsViewModel
import com.renium.sipkasku.viewmodel.StatisticsViewModelFactory
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun StatisticsScreen(
    navController: NavController,
    repository: TransactionRepository,
    categoryRepository: CategoryRepository? = null,
    pocketRepository: PocketRepository? = null
) {
    val viewModel: StatisticsViewModel = viewModel(
        factory = StatisticsViewModelFactory(
            transactionRepository = repository,
            categoryRepository = categoryRepository!!,
            pocketRepository = pocketRepository!!
        )
    )

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Trends", "Categories", "Export")

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow (
            selectedTabIndex = selectedTab,
            edgePadding = 16.dp
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, style = MaterialTheme.typography.labelMedium) }
                )
            }
        }

        when (selectedTab) {
            0 -> OverviewTab(viewModel)
            1 -> TrendsTab(viewModel)
            2 -> CategoriesTab(viewModel)
            3 -> ExportTab(viewModel)
        }
    }
}

// Shared Composables
@Composable
fun MonthSelector(
    selectedMonth: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val fmt = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("en", "ID"))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
        }
        Text(
            selectedMonth.format(fmt),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        IconButton(
            onClick = onNext,
            enabled = selectedMonth < YearMonth.now()
        ) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
        }
    }
}

@Composable
fun PocketFilter(
    pockets: List<Pocket>,
    selectedPocketId: Int?,
    onSelect: (Int?) -> Unit
) {
    if (pockets.isEmpty()) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedPocketId == null,
            onClick = { onSelect(null) },
            label = { Text("All Pockets") }
        )
        pockets.forEach { pocket ->
            FilterChip(
                selected = selectedPocketId == pocket.id,
                onClick = { onSelect(pocket.id) },
                label = { Text(pocket.name) }
            )
        }
    }
}
