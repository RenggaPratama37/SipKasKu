package com.renium.sipkasku.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.renium.sipkasku.data.local.Category
import com.renium.sipkasku.data.local.Pocket
import com.renium.sipkasku.data.repository.CategoryRepository
import com.renium.sipkasku.data.repository.PocketRepository
import com.renium.sipkasku.data.repository.SettingsRepository
import com.renium.sipkasku.data.repository.TransactionRepository
import com.renium.sipkasku.navigation.Screen.Settings
import com.renium.sipkasku.utils.formatDate
import com.renium.sipkasku.utils.formatRupiah
import com.renium.sipkasku.viewmodel.AddTransactionViewModel
import com.renium.sipkasku.viewmodel.TransactionViewModelFactory
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    navController: NavController,
    repository: TransactionRepository,
    pocketRepository: PocketRepository? = null,
    categoryRepository: CategoryRepository? = null,
    settingsRepository: SettingsRepository? = null
) {

    var title by rememberSaveable { mutableStateOf("") }

    var amount by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue("")
        )
    }

    var selectedCategory by rememberSaveable { mutableStateOf("") }

    // null = not chosen yet. true = income, false = expense
    var isIncome by rememberSaveable { mutableStateOf(false) }

    var expanded by remember {
        mutableStateOf(false)
    }

    var selectedDate by remember {
        mutableLongStateOf(
            System.currentTimeMillis()
        )
    }

    var showDatePicker by remember { mutableStateOf(false) }

    // categories loaded from repository if available; fallback to defaults
    val incomeCategories: List<Category> = categoryRepository?.getByType("INCOME")
        ?.collectAsState(initial = emptyList())?.value
        ?: remember { mutableStateOf(emptyList<Category>()) }.value

    val expenseCategories: List<Category> = categoryRepository?.getByType("EXPENSE")
        ?.collectAsState(initial = emptyList())?.value
        ?: remember { mutableStateOf(emptyList<Category>()) }.value

    val categories = when (isIncome) {
        true -> incomeCategories.map { c -> c.name }
        false -> expenseCategories.map { c -> c.name }
    }

    // map selectedCategory name -> categoryId
    val categoryId = when (isIncome) {
        true -> incomeCategories.firstOrNull { it.name == selectedCategory }?.id
        false -> expenseCategories.firstOrNull { it.name == selectedCategory }?.id
    }

    LaunchedEffect(isIncome, categories) {
        if (selectedCategory !in categories) {
            selectedCategory = ""
        }
    }

    val viewModel: AddTransactionViewModel = viewModel(
        factory = TransactionViewModelFactory(
            repository,
            pocketRepository
        )
    )

    val tooltipState = rememberTooltipState()

    // load pockets if repository provided (pocket is mandatory)
    val pockets by pocketRepository?.getAllPockets()?.collectAsState(initial = emptyList())
        ?: remember { mutableStateOf(emptyList()) }

    // pocket mandatory enforced
    val pocketMandatory = true

    var selectedPocketId by rememberSaveable { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()
    var showCreatePocketDialog by remember { mutableStateOf(false) }
    var newPocketName by rememberSaveable { mutableStateOf("") }
    var showCreateCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by rememberSaveable { mutableStateOf("") }

    val incomeColor = Color(0xFF2E7D32)
    val expenseColor = Color(0xFFD32F2F)

    // local validation state
    var showValidation by remember { mutableStateOf(false) }
    var showBalanceError by remember { mutableStateOf(false) }

    // parsed amount (raw number) for validation and balance checks
    val parsedAmount: Double = amount.text.replace(".", "").toDoubleOrNull() ?: 0.0
    val selectedPocketBalance: Double =
        pockets.firstOrNull { it.id == selectedPocketId }?.balance ?: 0.0
    val insufficientBalance =
        !isIncome && selectedPocketId != null && parsedAmount > selectedPocketBalance


    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { isIncome = false; showValidation = false; selectedCategory = "" },
                    colors = ButtonDefaults.buttonColors(containerColor = if (!isIncome) expenseColor else MaterialTheme.colorScheme.surface),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "Expense",
                        color = if (!isIncome) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }

                Button(
                    onClick = { isIncome = true; showValidation = false; selectedCategory = "" },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isIncome) incomeColor else MaterialTheme.colorScheme.surface),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "Income",
                        color = if (isIncome) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        item {
            // Segmented toggle: Expense / Income
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { input ->
                        val cleanString = input.text.replace("\\D".toRegex(), "")
                        if (cleanString.isEmpty()) {
                            amount = TextFieldValue("")
                        } else {
                            val formatted = NumberFormat.getNumberInstance(Locale("id", "ID"))
                                .format(cleanString.toLong())
                            amount = TextFieldValue(
                                text = formatted,
                                selection = TextRange(formatted.length)
                            )
                        }
                    },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(text = formatDate(selectedDate)) }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            placeholder = { Text("Select Category") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.heightIn(max = 160.dp)
                        ) {
                            if (categories.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No categories yet") },
                                    onClick = { expanded = false })
                            } else {
                                categories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category) },
                                        onClick = { selectedCategory = category; expanded = false })
                                }
                            }
                        }
                    }

                    IconButton(onClick = { showCreateCategoryDialog = true }) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Category")
                    }
                }

                // Pocket selection (mandatory)
                if (pocketRepository != null) {
                    var pocketExpanded by remember { mutableStateOf(false) }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = pocketExpanded,
                            onExpandedChange = { pocketExpanded = !pocketExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            val selectedPocket = pockets.firstOrNull { it.id == selectedPocketId }
                            val pocketLabel =
                                selectedPocket?.let { "${it.name} — ${formatRupiah(it.balance)}" }
                                    ?: ""
                            OutlinedTextField(
                                value = pocketLabel,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Pocket") },
                                placeholder = { Text("Select pocket") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pocketExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = pocketExpanded,
                                onDismissRequest = { pocketExpanded = false },
                                modifier = Modifier.heightIn(max = 160.dp)
                            ) {
                                if (pockets.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("No pockets yet") },
                                        onClick = { pocketExpanded = false })
                                } else {
                                    pockets.forEach { pocket ->
                                        DropdownMenuItem(
                                            text = {
                                                Text("${pocket.name} — ${formatRupiah(pocket.balance)}")
                                            },
                                            onClick = {
                                                selectedPocketId = pocket.id; pocketExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // quick add pocket button
                        IconButton(onClick = { showCreatePocketDialog = true }) {
                            Icon(imageVector = Icons.Filled.Add, contentDescription = "Add pocket")
                        }
                    }

                }

                // validation helper
                if (showValidation) {
                    val amt = amount.text.replace(".", "").toDoubleOrNull() ?: 0.0
                    when {
                        title.isBlank() -> Text("Please enter a title", color = expenseColor)
                        amt <= 0.0 -> Text(
                            "Please enter an amount greater than zero",
                            color = expenseColor
                        )

                        selectedCategory.isBlank() -> Text(
                            "Please select a category",
                            color = expenseColor
                        )
                    }
                }

                // Disable Save when pocket not chosen (mandatory) or insufficient balance
                val saveEnabled =
                    !(pocketMandatory && selectedPocketId == null) && !insufficientBalance

                if (insufficientBalance) {
                    Text("Insufficient pocket balance for this expense.", color = expenseColor)
                }

                Button(
                    onClick = {
                        val parsed = amount.text.replace(".", "").toDoubleOrNull() ?: 0.0
                        if (title.isBlank() || parsed <= 0.0 || selectedCategory.isBlank()) {
                            showValidation = true
                            return@Button
                        }

                        if (pocketMandatory && selectedPocketId == null) {
                            showValidation = true
                            return@Button
                        }

                        scope.launch {
                            val ok = viewModel.trySaveTransaction(
                                title = title,
                                amount = parsed,
                                categoryId = categoryId,
                                isIncome = isIncome,
                                date = selectedDate,
                                pocketId = selectedPocketId
                            )
                            if (ok) {
                                navController.popBackStack()
                            } else {
                                showBalanceError = true
                            }
                        }
                    },
                    enabled = saveEnabled,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isIncome) incomeColor else expenseColor)
                ) { Text("Save", color = Color.White) }
            }
        }

    }

    if (showCreatePocketDialog && pocketRepository != null) {
        AlertDialog(
            onDismissRequest = { showCreatePocketDialog = false },
            title = { Text("Create Pocket") },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Create a new pocket")
                        Spacer(modifier = Modifier.width(4.dp))
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                            tooltip = {
                                PlainTooltip {
                                    Text("Pocket is where you keep your money and take it out")
                                }
                            },
                            state = tooltipState
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "Info",
                                modifier = Modifier.clickable {}
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Example: Cash, E-Wallet, Bank Account")
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = newPocketName,
                        onValueChange = { newPocketName = it },
                        label = { Text("Pocket name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newPocketName.isNotBlank()) {
                        scope.launch {
                            val id = pocketRepository.insertPocket(Pocket(name = newPocketName))
                            selectedPocketId = id.toInt()
                            newPocketName = ""
                            showCreatePocketDialog = false
                        }
                    }
                }) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCreatePocketDialog = false
                    navController.navigate(Settings.route)
                }) { Text("Open Settings") }
            }
        )
    }

    if (showCreateCategoryDialog && categoryRepository != null) {
        AlertDialog(
            onDismissRequest = { showCreateCategoryDialog = false },
            title = { Text("Create Category") },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Create a new category")
                        Spacer(modifier = Modifier.width(4.dp))

                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                            tooltip = {
                                PlainTooltip {
                                    Text("Categories are labels to group each of your income and expenses.")
                                }
                            }, state = tooltipState
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "Info",
                                modifier = Modifier.clickable {}
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Example: Shopping, Salary, etc")
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("Category Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newCategoryName.isNotBlank()) {
                        val type = if (isIncome) "INCOME" else "EXPENSE"
                        scope.launch {
                            categoryRepository.insert(
                                Category(
                                    name = newCategoryName.trim(),
                                    type = type
                                )
                            )
                            selectedCategory = newCategoryName.trim()
                            newCategoryName = ""
                            showCreateCategoryDialog = false
                        }
                    }
                }) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateCategoryDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDatePicker) {

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
        )

        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        selectedDate = datePickerState
                            .selectedDateMillis
                            ?: System.currentTimeMillis()

                        showDatePicker = false
                    }
                ) {

                    Text("OK")
                }
            }
        ) {

            DatePicker(
                state = datePickerState
            )
        }
    }
}
