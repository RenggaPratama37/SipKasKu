package com.renium.sipkasku.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.renium.sipkasku.data.repository.TransactionRepository
import com.renium.sipkasku.utils.formatDate
import com.renium.sipkasku.viewmodel.AddTransactionViewModel
import com.renium.sipkasku.viewmodel.TransactionViewModelFactory
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    navController: NavController,
    repository: TransactionRepository
) {

    var title by rememberSaveable {
        mutableStateOf("")
    }

    var amount by rememberSaveable(
        stateSaver = TextFieldValue.Saver
    ) {
        mutableStateOf(TextFieldValue(""))
    }

    var selectedCategory by rememberSaveable {
        mutableStateOf("Food")
    }

    var isIncome by rememberSaveable {
        mutableStateOf(false)
    }

    var expanded by remember {
        mutableStateOf(false)
    }

    var selectedDate by remember {
        mutableLongStateOf(
            System.currentTimeMillis()
        )
    }

    var showDatePicker by remember {
        mutableStateOf(false)
    }

    val categories = listOf(
        "Food",
        "Transport",
        "Shopping",
        "Salary",
        "Other"
    )

    val viewModel: AddTransactionViewModel = viewModel(
        factory = TransactionViewModelFactory(repository)
    )

    Scaffold { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),

            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Add Transaction",
                style = MaterialTheme.typography.headlineMedium
            )

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                },

                label = {
                    Text("Title")
                },

                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = amount,

                onValueChange = { input ->

                    val cleanString = input.text.replace(
                        "[^\\d]".toRegex(),
                        ""
                    )

                    if (cleanString.isEmpty()) {

                        amount = TextFieldValue("")

                    } else {

                        val formatted = NumberFormat
                            .getNumberInstance(
                                Locale("in", "ID")
                            )
                            .format(cleanString.toLong())

                        amount = TextFieldValue(
                            text = formatted,

                            selection = TextRange(
                                formatted.length
                            )
                        )
                    }
                },

                label = {
                    Text("Amount")
                },

                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),

                modifier = Modifier.fillMaxWidth()
            )

            OutlinedButton(
                onClick = {
                    showDatePicker = true
                },

                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    text = formatDate(selectedDate)
                )
            }

            ExposedDropdownMenuBox(
                expanded = expanded,

                onExpandedChange = {
                    expanded = !expanded
                }
            ) {

                OutlinedTextField(
                    value = selectedCategory,

                    onValueChange = {},

                    readOnly = true,

                    label = {
                        Text("Category")
                    },

                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = expanded
                        )
                    },

                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,

                    onDismissRequest = {
                        expanded = false
                    }
                ) {

                    categories.forEach { category ->

                        DropdownMenuItem(
                            text = {
                                Text(category)
                            },

                            onClick = {
                                selectedCategory = category
                                expanded = false
                            }
                        )
                    }
                }
            }

            Row {

                FilterChip(
                    selected = !isIncome,

                    onClick = {
                        isIncome = false
                    },

                    label = {
                        Text("Expense")
                    }
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                FilterChip(
                    selected = isIncome,

                    onClick = {
                        isIncome = true
                    },

                    label = {
                        Text("Income")
                    }
                )
            }

            Button(
                onClick = {

                    viewModel.saveTransaction(
                        title = title,

                        amount = amount.text
                            .replace(".", "")
                            .toDoubleOrNull() ?: 0.0,

                        category = selectedCategory,
                        isIncome = isIncome,
                        date = selectedDate
                    )

                    navController.popBackStack()
                },

                modifier = Modifier.fillMaxWidth()
            ) {

                Text("Save")
            }
        }
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
