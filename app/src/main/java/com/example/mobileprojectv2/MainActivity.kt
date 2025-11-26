package com.example.mobileprojectv2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mobileprojectv2.ui.theme.MobileProjectV2Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var db: GroceryDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = GroceryDatabase.getInstance(this)

        enableEdgeToEdge()
        setContent {
            MobileProjectV2Theme {

                GroceryListScreen(db)

            }
        }
    }
}

@Composable
fun GroceryListScreen(db: GroceryDatabase) {

    var itemsList by remember { mutableStateOf(listOf<ItemEntity>()) }
    val scope = rememberCoroutineScope()

    // New state for showing Add Item dialog
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            val itemsFromDb = db.GroceryDao().getAllItems()
            itemsList = itemsFromDb
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(itemsList, key = { it.id }) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable {
                            scope.launch(Dispatchers.IO) {
                                val updatedItem = item.copy(isBought = !item.isBought)
                                db.GroceryDao().updateItem(updatedItem)
                                itemsList = db.GroceryDao().getAllItems()
                            }
                        },
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = item.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Quantity: ${item.quantity}, Price: ${item.price} EGP",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Total: ${item.quantity * item.price} EGP",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Bought: ${if (item.isBought) "Yes" else "No"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Button(onClick = {
                        scope.launch(Dispatchers.IO) {
                            db.GroceryDao().deleteItem(item)
                            itemsList = db.GroceryDao().getAllItems()
                        }
                    }) {
                        Text("Delete")
                    }
                }
            }
        }

        // Total cost at bottom
        val total = itemsList.sumOf { it.quantity * it.price }
        Text(
            text = "Total: $total EGP",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            style = MaterialTheme.typography.titleLarge
        )

        // ➊ Add Item button
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Add Item")
        }

        // ➋ Add Item Dialog
        if (showAddDialog) {
            var name by remember { mutableStateOf("") }
            var quantity by remember { mutableStateOf("") }
            var price by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add New Item") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") }
                        )
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { quantity = it },
                            label = { Text("Quantity") }
                        )
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Price") }
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        scope.launch(Dispatchers.IO) {
                            val newItem = ItemEntity(
                                id = 0, // Auto-generated by Room
                                name = name,
                                quantity = quantity.toIntOrNull() ?: 1,
                                price = price.toDoubleOrNull() ?: 0.0,
                                isBought = false
                            )
                            db.GroceryDao().insertItem(newItem)
                            itemsList = db.GroceryDao().getAllItems()
                        }
                        showAddDialog = false
                    }) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    Button(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
