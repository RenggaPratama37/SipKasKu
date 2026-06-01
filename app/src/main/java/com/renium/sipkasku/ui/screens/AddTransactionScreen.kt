package com.renium.sipkasku.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import com.renium.sipkasku.data.repository.TransactionRepository
import com.renium.sipkasku.utils.formatDate
import com.renium.sipkasku.viewmodel.AddTransactionViewModel
import com.renium.sipkasku.viewmodel.TransactionViewModelFactory
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    navController: NavController,
    repository: TransactionRepository,
    pocketRepository: com.renium.sipkasku.data.repository.PocketRepository? = null,
    categoryRepository: com.renium.sipkasku.data.repository.CategoryRepository? = null,
    settingsRepository: com.renium.sipkasku.data.repository.SettingsRepository? = null
) {

    var title by rememberSaveable { mutableStateOf("") }

    var amount by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }

    var selectedCategory by rememberSaveable { mutableStateOf("Food") }

    // null = not chosen yet. true = income, false = expense
    var isIncome by rememberSaveable { mutableStateOf<Boolean?>(null) }

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
    val incomeCategories: List<com.renium.sipkasku.data.local.Category> = if (categoryRepository != null) {
        categoryRepository.getByType("INCOME").collectAsState(initial = emptyList()).value
    } else {
        remember { mutableStateOf(emptyList<com.renium.sipkasku.data.local.Category>()) }.value
    }

    val expenseCategories: List<com.renium.sipkasku.data.local.Category> = if (categoryRepository != null) {
        categoryRepository.getByType("EXPENSE").collectAsState(initial = emptyList()).value
    } else {
        remember { mutableStateOf(emptyList<com.renium.sipkasku.data.local.Category>()) }.value
    }

    val categories = when (isIncome) {
        true -> if (incomeCategories.isEmpty()) listOf("Salary", "Other") else incomeCategories.map { c -> c.name }
        false -> if (expenseCategories.isEmpty()) listOf("Food", "Transport", "Shopping", "Other") else expenseCategories.map { c -> c.name }
        null -> listOf()
    }

    val viewModel: AddTransactionViewModel = viewModel(
        factory = TransactionViewModelFactory(repository, pocketRepository)
    )

    // load pockets if repository provided (pocket is mandatory)
    val pockets by pocketRepository?.getAllPockets()?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList<com.renium.sipkasku.data.local.Pocket>()) }

    // pocket mandatory enforced
    val pocketMandatory = true

    var selectedPocketId by rememberSaveable { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()
    var showCreatePocketDialog by remember { mutableStateOf(false) }
    var newPocketName by rememberSaveable { mutableStateOf("") }

    val incomeColor = Color(0xFF2E7D32)
    val expenseColor = Color(0xFFD32F2F)

    // local validation state
    var showValidation by remember { mutableStateOf(false) }
    var showBalanceError by remember { mutableStateOf(false) }

    // parsed amount (raw number) for validation and balance checks
    val parsedAmount: Double = amount.text.replace(".", "").toDoubleOrNull() ?: 0.0
    val selectedPocketBalance: Double = pockets.firstOrNull { it.id == selectedPocketId }?.balance ?: 0.0
    val insufficientBalance = (isIncome == false) && selectedPocketId != null && parsedAmount > selectedPocketBalance

    Scaffold { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(text = "Add Transaction", style = MaterialTheme.typography.headlineMedium)

            // Segmented toggle: Expense / Income
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { isIncome = false; showValidation = false },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isIncome == false) expenseColor else MaterialTheme.colorScheme.surface),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Expense", color = if (isIncome == false) Color.White else MaterialTheme.colorScheme.onSurface)
                }

                Button(
                    onClick = { isIncome = true; showValidation = false },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isIncome == true) incomeColor else MaterialTheme.colorScheme.surface),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Income", color = if (isIncome == true) Color.White else MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Animated reveal of the form after choice
            AnimatedVisibility(visible = isIncome != null, enter = fadeIn(), exit = fadeOut()) {

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { input ->
                            val cleanString = input.text.replace("[^\\d]".toRegex(), "")
                            if (cleanString.isEmpty()) {
                                amount = TextFieldValue("")
                            } else {
                                val formatted = NumberFormat.getNumberInstance(Locale("id", "ID")).format(cleanString.toLong())
                                amount = TextFieldValue(text = formatted, selection = TextRange(formatted.length))
                            }
                        },
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) { Text(text = formatDate(selectedDate)) }

                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(value = selectedCategory, onValueChange = {}, readOnly = true, label = { Text("Category") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())

                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            categories.forEach { category -> DropdownMenuItem(text = { Text(category) }, onClick = { selectedCategory = category; expanded = false }) }
                        }
                    }

                    // Pocket selection (mandatory)
                    if (pocketRepository != null) {
                        var pocketExpanded by remember { mutableStateOf(false) }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            ExposedDropdownMenuBox(expanded = pocketExpanded, onExpandedChange = { pocketExpanded = !pocketExpanded }, modifier = Modifier.weight(1f)) {
                                val selectedPocket = pockets.firstOrNull { it.id == selectedPocketId }
                                val pocketLabel = selectedPocket?.let { "${it.name} — ${com.renium.sipkasku.utils.formatRupiah(it.balance)}" } ?: "Select pocket"
                                OutlinedTextField(value = pocketLabel, onValueChange = {}, readOnly = true, label = { Text("Pocket") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pocketExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())

                                ExposedDropdownMenu(expanded = pocketExpanded, onDismissRequest = { pocketExpanded = false }) {
                                    if (pockets.isEmpty()) {
                                        DropdownMenuItem(text = { Text("No pockets yet") }, onClick = { pocketExpanded = false })
                                    } else {
                                        pockets.forEach { pocket -> DropdownMenuItem(text = { Text("${pocket.name} — ${com.renium.sipkasku.utils.formatRupiah(pocket.balance)}") }, onClick = { selectedPocketId = pocket.id; pocketExpanded = false }) }
                                    }
                                }
                            }

                            // quick add pocket button
                            IconButton(onClick = { showCreatePocketDialog = true }) {
                                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add pocket")
                            }
                        }

                        if (pockets.isEmpty()) {
                            Text("No pockets found. Please create one.", color = expenseColor)
                        }
                    }

                    // validation helper
                    if (showValidation) {
                        val amt = amount.text.replace(".", "").toDoubleOrNull() ?: 0.0
                        if (title.isBlank()) Text("Please enter a title", color = expenseColor)
                        else if (amt <= 0.0) Text("Please enter an amount greater than zero", color = expenseColor)
                    }

                    // Disable Save when pocket not chosen (mandatory) or insufficient balance
                    val saveEnabled = !(pocketMandatory && selectedPocketId == null) && !insufficientBalance

                    if (pocketMandatory && selectedPocketId == null) {
                        Text("Pocket is required", color = expenseColor)
                    }

                    if (insufficientBalance) {
                        Text("Insufficient pocket balance for this expense.", color = expenseColor)
                    }

                    Button(
                        onClick = {
                            val parsed = amount.text.replace(".", "").toDoubleOrNull() ?: 0.0
                            if (title.isBlank() || parsed <= 0.0) {
                                showValidation = true
                                return@Button
                            }

                            if (pocketMandatory && selectedPocketId == null) {
                                showValidation = true
                                return@Button
                            }

                            scope.launch {
                                val ok = viewModel.trySaveTransaction(title = title, amount = parsed, category = selectedCategory, isIncome = isIncome
                                    ?: false, date = selectedDate, pocketId = selectedPocketId)
                                if (ok) {
                                    navController.popBackStack()
                                } else {
                                    showBalanceError = true
                                }
                            }
                        },
                        enabled = saveEnabled,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isIncome == true) incomeColor else expenseColor)
                    ) { Text("Save", color = Color.White) }
                }
            }
        }
    }

    if (showCreatePocketDialog && pocketRepository != null) {
        AlertDialog(
            onDismissRequest = { showCreatePocketDialog = false },
            title = { Text("Create Pocket") },
            text = {
                Column {
                    Text("You have no pockets. Create one to continue.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = newPocketName, onValueChange = { newPocketName = it }, label = { Text("Pocket name") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newPocketName.isNotBlank()) {
                        scope.launch {
                            val id = pocketRepository.insertPocket(com.renium.sipkasku.data.local.Pocket(name = newPocketName))
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
                    navController.navigate(com.renium.sipkasku.navigation.Screen.Settings.route)
                }) { Text("Open Settings") }
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
