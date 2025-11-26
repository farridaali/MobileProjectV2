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

    // 1️⃣ State to hold items
    var itemsList by remember { mutableStateOf(listOf<ItemEntity>()) }

    val scope = rememberCoroutineScope()

    // 2️⃣ Load items from Room
    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            val itemsFromDb = db.GroceryDao().getAllItems()
            itemsList = itemsFromDb
        }
    }

    // 3️⃣ UI
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(itemsList, key = { it.id }) { item ->

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable {
                            // Toggle bought
                            scope.launch(Dispatchers.IO) {
                                val updatedItem = item.copy(isBought = !item.isBought)
                                db.GroceryDao().updateItem(updatedItem)
                                val updatedList = db.GroceryDao().getAllItems()
                                itemsList = updatedList
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

                    // Delete button
                    Button(onClick = {
                        scope.launch(Dispatchers.IO) {
                            db.GroceryDao().deleteItem(item)
                            val updatedList = db.GroceryDao().getAllItems()
                            itemsList = updatedList
                        }
                    }) {
                        Text("Delete")
                    }
                }
            }
        }

        // 4️⃣ Total cost at bottom
        val total = itemsList.sumOf { it.quantity * it.price }
        Text(
            text = "Total: $total EGP",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            style = MaterialTheme.typography.titleLarge
        )
    }
}
