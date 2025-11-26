package com.example.mobileprojectv2

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileprojectv2.ui.theme.MobileProjectV2Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.platform.LocalContext



class AddUpdateItemActivity : ComponentActivity() {

    private lateinit var db: GroceryDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = GroceryDatabase.getInstance(this)

        val itemId = intent.getIntExtra("itemId", -1)

        setContent {
            MobileProjectV2Theme {
                AddUpdateItemScreen(db, itemId)
            }
        }
    }
}

@Composable
fun AddUpdateItemScreen(db: GroceryDatabase, itemId: Int) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(itemId) {
        if (itemId != -1) {
            scope.launch(Dispatchers.IO) {
                val item = db.GroceryDao().getItemById(itemId)
                name = item.name
                quantity = item.quantity.toString()
                price = item.price.toString()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F2))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (itemId == -1) "Add New Item" else "Update Item",
            fontSize = 24.sp,
            color = Color(0xFF6200EE)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Item Name") },
            enabled = itemId == -1,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = Color(0xFF6200EE),
                focusedIndicatorColor = Color(0xFF6200EE),
                unfocusedIndicatorColor = Color.Gray
            )

        )

        OutlinedTextField(
            value = quantity,
            onValueChange = { quantity = it },
            label = { Text("Quantity") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = Color(0xFF6200EE),
                focusedIndicatorColor = Color(0xFF6200EE),
                unfocusedIndicatorColor = Color.Gray
            )
        )

        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Price") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = Color(0xFF6200EE),
                focusedIndicatorColor = Color(0xFF6200EE),
                unfocusedIndicatorColor = Color.Gray
            )
        )

        Button(
            onClick = {
                val qty = quantity.toIntOrNull()
                val prc = price.toDoubleOrNull()
                if (qty == null || prc == null || name.isEmpty()) {
                    Toast.makeText(
                        context,
                        "Please enter valid values",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }

                scope.launch(Dispatchers.IO) {
                    if (itemId == -1) {
                        db.GroceryDao().insertItem(ItemEntity(name = name, quantity = qty, price = prc))
                    } else {
                        val updatedItem = db.GroceryDao().getItemById(itemId)
                            .copy(quantity = qty, price = prc)
                        db.GroceryDao().updateItem(updatedItem)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (itemId == -1) "Add Item" else "Update Item", color = Color.White, fontSize = 18.sp)
        }
    }
}