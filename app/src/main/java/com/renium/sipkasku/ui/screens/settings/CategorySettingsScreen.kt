package com.renium.sipkasku.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.renium.sipkasku.data.local.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySettingsScreen(
    incomeCategories: List<Category>,
    expenseCategories: List<Category>,
    onAddCategory: (name: String, type: String) -> Unit,
    onDeleteCategory: (Category) -> Unit
) {
    var newCategoryName by remember { mutableStateOf("") }
    var newCategoryType by remember { mutableStateOf("EXPENSE") }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }


    var incomeExpanded by remember { mutableStateOf(true) }
    var expenseExpanded by remember { mutableStateOf(true) }

    categoryToDelete?.let { category ->
        AlertDialog(
            onDismissRequest = {
                categoryToDelete = null
            },
            title = {
                Text("Delete Category")
            },
            text = {
                Text("Delete \"${category.name}\"?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteCategory(category)
                        categoryToDelete = null
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        categoryToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Add Category",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = {
                            newCategoryName = it
                        },
                        label = {
                            Text("Category Name")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = newCategoryType == "INCOME",
                            onClick = {
                                newCategoryType = "INCOME"
                            },
                            label = {
                                Text("Income")
                            },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )

                        FilterChip(
                            selected = newCategoryType == "EXPENSE",
                            onClick = {
                                newCategoryType = "EXPENSE"
                            },
                            label = {
                                Text("Expense")
                            },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.error,
                                selectedLabelColor = MaterialTheme.colorScheme.onError
                            )
                        )
                    }

                    Button(
                        onClick = {
                            onAddCategory(
                                newCategoryName.trim(),
                                newCategoryType
                            )
                            newCategoryName = ""
                        },
                        enabled = newCategoryName.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )

                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )

                        Text("Add Category")
                    }
                }
            }
        }

        item {
            CategorySection(
                title = "Income Categories",
                expanded = incomeExpanded,
                onExpandedChange = {
                    incomeExpanded = !incomeExpanded
                }
            )
        }

        if (incomeExpanded) {
            items(
                items = incomeCategories,
                key = { it.id }
            ) { category ->
                CategoryItemCard(
                    category = category,
                    onDeleteClick = {
                        categoryToDelete = category
                    }
                )
            }
        }

        item {
            CategorySection(
                title = "Expense Categories",
                expanded = expenseExpanded,
                onExpandedChange = {
                    expenseExpanded = !expenseExpanded
                }
            )
        }

        if (expenseExpanded) {
            items(
                items = expenseCategories,
                key = { it.id }
            ) { category ->
                CategoryItemCard(
                    category = category,
                    onDeleteClick = {
                        categoryToDelete = category
                    }
                )
            }
        }

        item {
            Spacer(
                modifier = Modifier.height(24.dp)
            )
        }
    }


}

@Composable
private fun CategorySection(
    title: String,
    expanded: Boolean,
    onExpandedChange: () -> Unit
) {
    Card(
        onClick = onExpandedChange,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 14.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )


            Icon(
                imageVector = if (expanded) {
                    Icons.Default.KeyboardArrowUp
                } else {
                    Icons.Default.KeyboardArrowDown
                },
                contentDescription = null
            )
        }
    }


}

@Composable
private fun CategoryItemCard(
    category: Category,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )


            IconButton(
                onClick = onDeleteClick
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Category",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }


}
