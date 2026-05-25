package com.renium.sipkasku.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun AddTransactionScreen(
    navController: NavController
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
    val categories = listOf(
        "Food",
        "Transport",
        "Shopping",
        "Salary",
        "Other"
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
                onValueChange = {
                    amount = it
                },
                label = {
                    Text("Amount")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Category: $selectedCategory"
            )
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
                Spacer(modifier = Modifier.width(8.dp))
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
                    println(title)
                    println(amount)
                    println(selectedCategory)
                    println(isIncome)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}
