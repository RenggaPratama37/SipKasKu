package com.renium.sipkasku.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

import com.renium.sipkasku.data.repository.TransactionRepository
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

    var amount by rememberSaveable {
        mutableStateOf("")
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

            // TITLE
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

            // AMOUNT
            OutlinedTextField(
                value = amount,

                onValueChange = { input ->

                    val cleanString = input.replace(
                        "[^\\d]".toRegex(),
                        ""
                    )

                    if (cleanString.isNotEmpty()) {

                        amount = NumberFormat
                            .getNumberInstance(
                                Locale("in", "ID")
                            )
                            .format(
                                cleanString.toLong()
                            )

                    } else {
                        amount = ""
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

            // CATEGORY DROPDOWN
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
                        ExposedDropdownMenuDefaults
                            .TrailingIcon(
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

            // TYPE CHIP
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

            // SAVE BUTTON
            Button(
                onClick = {

                    viewModel.saveTransaction(
                        title = title,

                        amount = amount
                            .replace(".", "")
                            .toDoubleOrNull() ?: 0.0,

                        category = selectedCategory,

                        isIncome = isIncome
                    )

                    navController.popBackStack()
                },

                modifier = Modifier.fillMaxWidth()
            ) {

                Text("Save")
            }
        }
    }
}
