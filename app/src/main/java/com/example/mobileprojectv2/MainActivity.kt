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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.mobileprojectv2.ui.theme.MobileProjectV2Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// bazwad ay 7aga 3shan a3ml gradlew clean
class MainActivity : ComponentActivity() {

    private lateinit var db: GroceryDatabase
    private var refreshTrigger = mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = GroceryDatabase.getInstance(this)
        enableEdgeToEdge()

        setContent {
            MobileProjectV2Theme {
                GroceryListScreen(db, refreshTrigger)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // ben5aly ye-refresh bel 3afya
        refreshTrigger.value = refreshTrigger.value + 1
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryListScreen(db: GroceryDatabase, refreshTrigger: MutableState<Int>) {
    var itemsList by remember { mutableStateOf(listOf<ItemEntity>()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<ItemEntity?>(null) }
    var showReminderDialog by remember { mutableStateOf(false) }
    var itemForReminder by remember { mutableStateOf<ItemEntity?>(null) }
    val context = LocalContext.current
    val activity = context as ComponentActivity

    // el gozy2a el bengeeb el data men el database
    fun loadItems() {
        activity.lifecycleScope.launch(Dispatchers.IO) {
            val items = db.GroceryDao().getAllItems()
            launch(Dispatchers.Main) {
                itemsList = items
            }
        }
    }

    // ben5aly ye-refresh bel 3afya tany
    LaunchedEffect(refreshTrigger.value) {
        loadItems()
    }

    //dah logic el total cost
    var totalCost = 0.0
    for (item in itemsList) {
        totalCost += item.quantity * item.price
    }

    // dah logic el items ely lesa mashtrynahash
    var remainingCost = 0.0
    for (item in itemsList) {
        if (!item.isBought) {
            remainingCost += item.quantity * item.price
        }
    }

    // 7etet kam item eshtrana
    var boughtCount = 0
    for (item in itemsList) {
        if (item.isBought) {
            boughtCount++
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
                    Column {
                        Text(
                            "My Grocery List",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "${itemsList.size} items • $boughtCount bought",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val intent = Intent(context, AddUpdateItemActivity::class.java)
                    context.startActivity(intent)
                },
                containerColor = Color(0xFF667eea),
                contentColor = Color.White,
                modifier = Modifier
                    .size(64.dp)
                    .shadow(8.dp, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Item",
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color(0xFFF5F7FA))
            ) {
                // el row ely fy el total cost we el remaining cost
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 7etet el total cost
                    SummaryCard(
                        title = "Total Cost",
                        value = "$totalCost EGP",
                        icon = Icons.Default.ShoppingCart,
                        color = Color(0xFF667eea),
                        modifier = Modifier.weight(1f)
                    )

                    // 7etet el remaining cost
                    SummaryCard(
                        title = "Remaining",
                        value = "$remainingCost EGP",
                        icon = Icons.Default.AccountBalanceWallet,
                        color = Color(0xFF06b6d4),
                        modifier = Modifier.weight(1f)
                    )
                }

                // hena benwary ya2ma empty state y3ny mafysh item ya2ma el items bt3tna
                if (itemsList.isEmpty()) {
                    EmptyStateView()
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(itemsList, key = { it.id }) { item ->
                            GroceryItemCard(
                                item = item,
                                onItemClick = {
                                    val intent = Intent(context, AddUpdateItemActivity::class.java)
                                    intent.putExtra("itemId", item.id)
                                    context.startActivity(intent)
                                },
                                onCheckedChange = { isChecked ->
                                    activity.lifecycleScope.launch(Dispatchers.IO) {
                                        val updatedItem = item.copy(isBought = isChecked)
                                        db.GroceryDao().updateItem(updatedItem)
                                        loadItems()
                                    }
                                },
                                onDeleteClick = {
                                    itemToDelete = item
                                    showDeleteDialog = true
                                },
                                onReminderClick = {
                                    itemForReminder = item
                                    showReminderDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    )

    // 7etet mas7a el item men 3ala wagh el ard
    if (showDeleteDialog && itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Item") },
            text = { Text("Are you sure you want to delete ${itemToDelete?.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        activity.lifecycleScope.launch(Dispatchers.IO) {
                            if (itemToDelete != null) {
                                db.GroceryDao().deleteItem(itemToDelete!!)
                            }
                            loadItems()
                        }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFef4444)
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 7etet gebt el laban wala la2 (reminder)
    if (showReminderDialog && itemForReminder != null) {
        ReminderDialog(
            item = itemForReminder!!,
            onDismiss = { showReminderDialog = false }
        )
    }
}

// dy cards el total wel remaining
@Composable
fun SummaryCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(color.copy(alpha = 0.1f), color.copy(alpha = 0.05f))
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = value,
                    fontSize = 18.sp,
                    color = Color(0xFF1f2937),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// dah el item card ely fy kol 7aga men el reminder lel delete wel check box
@Composable
fun GroceryItemCard(
    item: ItemEntity,
    onItemClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onDeleteClick: () -> Unit,
    onReminderClick: () -> Unit
) {
    val itemTotal = item.quantity * item.price

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clickable(onClick = onItemClick),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isBought) Color(0xFFF0FDF4) else Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            //7etet el check box
            Checkbox(
                checked = item.isBought,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF10b981),
                    uncheckedColor = Color(0xFF9ca3af)
                )
            )

            // 7etet el name wel price wel qty
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = item.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (item.isBought) Color(0xFF6b7280) else Color(0xFF1f2937),
                    textDecoration = if (item.isBought) TextDecoration.LineThrough else null
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Chip(
                        text = "Qty: ${item.quantity}",
                        backgroundColor = Color(0xFFdbeafe),
                        textColor = Color(0xFF1e40af)
                    )
                    Chip(
                        text = "${item.price} EGP",
                        backgroundColor = Color(0xFFfef3c7),
                        textColor = Color(0xFF92400e)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Total: $itemTotal EGP",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF667eea)
                )
            }

            // buttons ely 3al gamb ely heya el delete wel reminder
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // gebt el laban wla la2 (reminder)
                IconButton(
                    onClick = onReminderClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFfef3c7))
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Set Reminder",
                        tint = Color(0xFFf59e0b),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // mas7o men 3ala wagh el koun (delete)
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFfee2e2))
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFef4444),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

//styling ely ben7ot fy el price wel qty
@Composable
fun Chip(text: String, backgroundColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}


//el far8 ely 3amlyno (empty state)
@Composable
fun EmptyStateView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingCart,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = Color(0xFFe5e7eb)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your grocery list is empty",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF6b7280)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap the + button to add your first item",
            fontSize = 14.sp,
            color = Color(0xFF9ca3af)
        )
    }
}