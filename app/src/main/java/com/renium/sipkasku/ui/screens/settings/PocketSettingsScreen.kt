package com.renium.sipkasku.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.renium.sipkasku.data.local.Pocket
import com.renium.sipkasku.data.repository.PocketRepository
import com.renium.sipkasku.data.repository.SettingsRepository
import kotlinx.coroutines.launch

@Composable
fun PocketSettingsScreen(
    pocketRepository: PocketRepository?,
    settingsRepository: SettingsRepository?
) {

    val scope = rememberCoroutineScope()

    val pockets by pocketRepository?.getAllPockets()
        ?.collectAsState(initial = emptyList())
        ?: remember {
            mutableStateOf(emptyList<Pocket>())
        }

    var newPocketName by remember {
        mutableStateOf("")
    }

    val pocketMandatory by settingsRepository?.isPocketMandatory()
        ?.collectAsState(initial = false)
        ?: remember {
            mutableStateOf(false)
        }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        item {

            Text(
                "Pocket Settings",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            pockets.forEach { p ->

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(p.name)

                        Text(
                            "Balance: ${p.balance}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    IconButton(
                        onClick = {
                            scope.launch {
                                pocketRepository?.deletePocket(p)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null
                        )
                    }
                }
            }

            OutlinedTextField(
                value = newPocketName,
                onValueChange = {
                    newPocketName = it
                },
                label = {
                    Text("Pocket Name")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (newPocketName.isNotBlank()) {

                        scope.launch {
                            pocketRepository?.insertPocket(
                                Pocket(name = newPocketName)
                            )

                            newPocketName = ""
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Pocket")
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Checkbox(
                    checked = pocketMandatory,
                    onCheckedChange = { checked ->
                        scope.launch {
                            settingsRepository?.setPocketMandatory(checked)
                        }
                    }
                )

                Text("Require pocket for transactions")
            }
        }
    }
}
