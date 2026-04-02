package com.example.stockentry

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stockentry.data.AppDatabase
import com.example.stockentry.data.StockEntry
import com.example.stockentry.data.StockRepository
import com.example.stockentry.network.SheetsService
import com.example.stockentry.ui.StockViewModel
import com.example.stockentry.ui.StockViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val dao = AppDatabase.getInstance(applicationContext).stockEntryDao()
        val repository = StockRepository(dao, SheetsService(BuildConfig.SHEETS_WEBHOOK_URL))

        setContent {
            MaterialTheme {
                StockApp(repository)
            }
        }
    }
}

@Composable
private fun StockApp(repository: StockRepository) {
    val vm: StockViewModel = viewModel(factory = StockViewModelFactory(repository))
    val state by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.message) {
        state.message?.let { msg ->
            scope.launch { snackbarHostState.showSnackbar(msg) }
            vm.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        StockScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            entries = state.entries,
            loading = state.loading,
            onAdd = vm::addEntry,
            onDelete = vm::deleteEntry,
            onSync = vm::syncEntries
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StockScreen(
    modifier: Modifier = Modifier,
    entries: List<StockEntry>,
    loading: Boolean,
    onAdd: (String, String, String) -> Unit,
    onDelete: (StockEntry) -> Unit,
    onSync: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedType by rememberSaveable { mutableStateOf("wood") }
    var rate by rememberSaveable { mutableStateOf("") }
    var qty by rememberSaveable { mutableStateOf("") }

    Column(modifier = modifier) {
        Text("Create New Entry", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = selectedType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Product") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                listOf("wood", "nariyal").forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = {
                            selectedType = item
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = rate,
            onValueChange = { rate = it },
            label = { Text("Rate") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = qty,
            onValueChange = { qty = it },
            label = { Text("Quantity") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = {
                onAdd(selectedType, rate, qty)
                rate = ""
                qty = ""
            }) { Text("Save Entry") }
            Button(onClick = onSync, enabled = !loading) { Text(if (loading) "Syncing..." else "Sync to Sheet") }
        }

        Spacer(Modifier.height(20.dp))
        Text("Last 10 Entries", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(entries, key = { it.id }) { entry ->
                EntryCard(entry = entry, onDelete = onDelete)
            }
        }
    }
}

@Composable
private fun EntryCard(entry: StockEntry, onDelete: (StockEntry) -> Unit) {
    val formatter = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(entry.productType.uppercase(Locale.getDefault()), fontWeight = FontWeight.Bold)
            Text("Rate: ${entry.rate}")
            Text("Qty: ${entry.quantity}")
            Text("Time: ${formatter.format(Date(entry.createdAt))}")
            Text("Status: ${if (entry.synced) "Synced" else "Not synced"}")
            TextButton(onClick = { onDelete(entry) }) {
                Text("Delete")
            }
        }
    }
}
