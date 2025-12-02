package com.example.mobileprojectv2

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.work.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun ReminderDialog(
    item: ItemEntity,
    onDismiss: () -> Unit
) {
    var selectedDateTime by remember { mutableStateOf<Calendar?>(null) }
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())

    // Permission launcher for notifications (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(
                context,
                "Notification permission is required for reminders",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Check and request notification permission
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED -> {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = Color(0xFFf59e0b)
                )
                Text(
                    "Set Reminder",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Remind me to buy: ${item.name}",
                    fontSize = 16.sp,
                    color = Color(0xFF6b7280)
                )

                // Date and Time Picker Button
                OutlinedButton(
                    onClick = {
                        showDateTimePicker(context) { calendar ->
                            selectedDateTime = calendar
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color(0xFF667eea)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        selectedDateTime?.let { dateFormat.format(it.time) }
                            ?: "Select Date & Time",
                        color = Color(0xFF1f2937)
                    )
                }

                // Quick reminder options
                Text(
                    "Quick Options:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6b7280)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickReminderOption(
                        text = "1 hour from now",
                        onClick = {
                            selectedDateTime = Calendar.getInstance().apply {
                                add(Calendar.HOUR_OF_DAY, 1)
                            }
                        }
                    )
                    QuickReminderOption(
                        text = "Tomorrow at 9 AM",
                        onClick = {
                            selectedDateTime = Calendar.getInstance().apply {
                                add(Calendar.DAY_OF_MONTH, 1)
                                set(Calendar.HOUR_OF_DAY, 9)
                                set(Calendar.MINUTE, 0)
                            }
                        }
                    )
                    QuickReminderOption(
                        text = "This weekend",
                        onClick = {
                            selectedDateTime = Calendar.getInstance().apply {
                                while (get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
                                    add(Calendar.DAY_OF_MONTH, 1)
                                }
                                set(Calendar.HOUR_OF_DAY, 10)
                                set(Calendar.MINUTE, 0)
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedDateTime?.let { calendar ->
                        scheduleReminder(context, item, calendar)
                        Toast.makeText(
                            context,
                            "Reminder set for ${dateFormat.format(calendar.time)}",
                            Toast.LENGTH_SHORT
                        ).show()
                        onDismiss()
                    } ?: run {
                        Toast.makeText(
                            context,
                            "Please select a date and time",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF667eea)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Set Reminder")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF6b7280))
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun QuickReminderOption(text: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFf3f4f6)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                color = Color(0xFF1f2937)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF9ca3af),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun showDateTimePicker(context: Context, onDateTimeSelected: (Calendar) -> Unit) {
    val calendar = Calendar.getInstance()

    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    calendar.set(Calendar.SECOND, 0)
                    onDateTimeSelected(calendar)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            ).show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.minDate = System.currentTimeMillis()
    }.show()
}

private fun scheduleReminder(context: Context, item: ItemEntity, calendar: Calendar) {
    val delay = calendar.timeInMillis - System.currentTimeMillis()

    if (delay <= 0) {
        Toast.makeText(context, "Please select a future date and time", Toast.LENGTH_SHORT).show()
        return
    }

    val data = Data.Builder()
        .putString("item_name", item.name)
        .putInt("item_id", item.id)
        .build()

    val reminderWork = OneTimeWorkRequestBuilder<ReminderWorker>()
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .setInputData(data)
        .addTag("reminder_${item.id}")
        .build()

    WorkManager.getInstance(context).enqueue(reminderWork)
}