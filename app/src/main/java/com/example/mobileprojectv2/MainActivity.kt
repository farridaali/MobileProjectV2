package com.example.mobileprojectv2

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryListScreen(db: GroceryDatabase) {
    var itemsList by remember { mutableStateOf(listOf<ItemEntity>()) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Load items from DB
    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            itemsList = db.GroceryDao().getAllItems()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Grocery List", fontSize = 22.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EE)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                context.startActivity(
                    Intent(context, AddUpdateItemActivity::class.java)
                )
            }) {
                Text("+", fontSize = 24.sp)
            }
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color(0xFFF2F2F2))
            ) {

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(itemsList, key = { it.id }) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable {
                                    val intent = Intent(context, AddUpdateItemActivity::class.java)
                                    intent.putExtra("itemId", item.id)
                                    context.startActivity(intent)
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = item.name, fontSize = 20.sp, color = Color(0xFF333333))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Qty: ${item.quantity}, Price: ${item.price} EGP",
                                        fontSize = 14.sp,
                                        color = Color(0xFF666666)
                                    )
                                    Text(
                                        text = "Total: ${item.quantity * item.price} EGP",
                                        fontSize = 14.sp,
                                        color = Color(0xFF666666)
                                    )
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Checkbox(
                                        checked = item.isBought,
                                        onCheckedChange = { isChecked ->
                                            scope.launch(Dispatchers.IO) {
                                                val updatedItem = item.copy(isBought = isChecked)
                                                db.GroceryDao().updateItem(updatedItem)
                                                itemsList = db.GroceryDao().getAllItems()
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF6200EE))
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = {
                                            scope.launch(Dispatchers.IO) {
                                                db.GroceryDao().deleteItem(item)
                                                itemsList = db.GroceryDao().getAllItems()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("Delete", fontSize = 12.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                val total = itemsList.sumOf { it.quantity * it.price }
                Text(
                    text = "Total Cost: $total EGP",
                    fontSize = 20.sp,
                    color = Color(0xFF6200EE),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }
    )
}