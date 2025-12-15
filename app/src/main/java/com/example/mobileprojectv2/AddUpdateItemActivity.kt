package com.example.mobileprojectv2

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.mobileprojectv2.ui.theme.MobileProjectV2Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddUpdateItemActivity : ComponentActivity() {

    private lateinit var db: GroceryDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = GroceryDatabase.getInstance(this)

        val itemId = intent.getIntExtra("itemId", -1)

        setContent {
            MobileProjectV2Theme {
                AddUpdateItemScreen(db, itemId, { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUpdateItemScreen(db: GroceryDatabase, itemId: Int, onComplete: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val isUpdateMode = itemId != -1

    //7etet el edit hy-load el info bt3t el item 3shan ne3ml 3ala edit
    LaunchedEffect(itemId) {
        if (isUpdateMode) {
            activity.lifecycleScope.launch(Dispatchers.IO) {
                val item = db.GroceryDao().getItemById(itemId)
                launch(Dispatchers.Main) {
                    name = item.name
                    quantity = item.quantity.toString()
                    price = item.price.toString()
                }
            }
        }
    }

    val gradientColors = listOf(
        Color(0xFF667eea),
        Color(0xFF764ba2)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isUpdateMode) "Update Item" else "Add New Item",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onComplete) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier
                    .background(
                        brush = Brush.horizontalGradient(gradientColors)
                    )
                    .statusBarsPadding()
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7FA))
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Dah el plus icon ely fo2 input
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        brush = Brush.linearGradient(gradientColors),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isUpdateMode) Icons.Default.Edit else Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // dah el name field ely ben7ot fy el esm el item
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Item Name") },
                enabled = !isUpdateMode,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = Color(0xFF667eea)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color(0xFFF3F4F6),
                    cursorColor = Color(0xFF667eea),
                    focusedBorderColor = Color(0xFF667eea),
                    unfocusedBorderColor = Color(0xFFd1d5db),
                    focusedLabelColor = Color(0xFF667eea)
                ),
                singleLine = true
            )

            // dah el qty field ely ben7ot fy 3aded el items ely hanshtryha
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Quantity") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Numbers,
                        contentDescription = null,
                        tint = Color(0xFF667eea)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    cursorColor = Color(0xFF667eea),
                    focusedBorderColor = Color(0xFF667eea),
                    unfocusedBorderColor = Color(0xFFd1d5db),
                    focusedLabelColor = Color(0xFF667eea)
                ),
                singleLine = true
            )

            // dah el price field ely ben7ot fy el se3r
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price (EGP)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = null,
                        tint = Color(0xFF667eea)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    cursorColor = Color(0xFF667eea),
                    focusedBorderColor = Color(0xFF667eea),
                    unfocusedBorderColor = Color(0xFFd1d5db),
                    focusedLabelColor = Color(0xFF667eea)
                ),
                singleLine = true
            )

            // 7atet el total ely betzhar fel a5r b3d mabn7ot el info bt3t el item
            if (quantity.isNotEmpty() && price.isNotEmpty()) {
                val qty = quantity.toIntOrNull() ?: 0
                val prc = price.toDoubleOrNull() ?: 0.0
                val total = qty * prc

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFdbeafe)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Cost:",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1e40af)
                        )
                        Text(
                            text = "$total EGP",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1e3a8a)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // zorar el add item ely byzahr ta7t b3d maben7ot el info
            Button(
                onClick = {

                    val qty = quantity.toIntOrNull()
                    val prc = price.toDoubleOrNull()

                    if (name.isEmpty() || qty == null || prc == null) {
                        Toast.makeText(
                            context,
                            "Please enter valid values",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    if (isLoading) return@Button

                    isLoading = true
                    activity.lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            if (itemId == -1) {
                                // Add new item
                                val newItem = ItemEntity(
                                    name = name,
                                    quantity = qty,
                                    price = prc
                                )
                                db.GroceryDao().insertItem(newItem)
                            } else {

                                val existingItem = db.GroceryDao().getItemById(itemId)
                                val updatedItem = existingItem.copy(
                                    quantity = qty,
                                    price = prc
                                )
                                db.GroceryDao().updateItem(updatedItem)
                            }

                            launch(Dispatchers.Main) {
                                val message = if (isUpdateMode) {
                                    "Item updated successfully"
                                } else {
                                    "Item added successfully"
                                }
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                isLoading = false
                                onComplete()
                            }
                        } catch (e: Exception) {
                            launch(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    "Error: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                isLoading = false
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(gradientColors)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isUpdateMode) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (isUpdateMode) "Update Item" else "Add Item",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}