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

    // 7etet eny b3ml request permission men el os eny ab3t notification
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

    //  bat2akd el awl ana 3ndy el permission wala la2
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // shakl el reminder tab lama bados 3ala el reminder icon ely mawgood fel item card
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

                // dah el button b5tar men el date wel time ely 3ayzhom
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
                        if (selectedDateTime != null) {
                            dateFormat.format(selectedDateTime!!.time)
                        } else {
                            "Select Date & Time"
                        },
                        color = Color(0xFF1f2937)
                    )
                }

                // lawo el user msh 3ayz 7aga custom 3amlo three options y5tar menhom b3d sa3a, bokra, fel weekend
                Text(
                    "Quick Options:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6b7280)
                )

                // styling el three options
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickReminderOption(
                        text = "1 hour from now",
                        onClick = {
                            val calendar = Calendar.getInstance()
                            calendar.add(Calendar.HOUR_OF_DAY, 1)
                            selectedDateTime = calendar
                        }
                    )

                    QuickReminderOption(
                        text = "Tomorrow at 9 AM",
                        onClick = {
                            val calendar = Calendar.getInstance()
                            calendar.add(Calendar.DAY_OF_MONTH, 1)
                            calendar.set(Calendar.HOUR_OF_DAY, 9)
                            calendar.set(Calendar.MINUTE, 0)
                            selectedDateTime = calendar
                        }
                    )

                    QuickReminderOption(
                        text = "This weekend",
                        onClick = {
                            val calendar = Calendar.getInstance()
                            // Keep adding days until we reach Saturday
                            while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
                                calendar.add(Calendar.DAY_OF_MONTH, 1)
                            }
                            calendar.set(Calendar.HOUR_OF_DAY, 10)
                            calendar.set(Calendar.MINUTE, 0)
                            selectedDateTime = calendar
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedDateTime != null) {
                        scheduleReminder(context, item, selectedDateTime!!)
                        val formattedDate = dateFormat.format(selectedDateTime!!.time)
                        Toast.makeText(
                            context,
                            "Reminder set for $formattedDate",
                            Toast.LENGTH_SHORT
                        ).show()
                        onDismiss()
                    } else {
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

// el 7eta el banwary fyha el calender wel clock ely ben5tar men el date and time
fun showDateTimePicker(context: Context, onDateTimeSelected: (Calendar) -> Unit) {
    val calendar = Calendar.getInstance()

    // 7etet el calender
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            // Set the selected date
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            // 7etet el clock
            val timePickerDialog = TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    // ben5tar el time ely ana 3ayzo
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    calendar.set(Calendar.SECOND, 0)
                    onDateTimeSelected(calendar)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            )
            timePickerDialog.show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // hena msh b5ly el user y5tar date adym 3n el mawgood fel os
    datePickerDialog.datePicker.minDate = System.currentTimeMillis()
    datePickerDialog.show()
}

// hena b2a best5dm el notification manager 3shan a-schedule el notificaton
fun scheduleReminder(context: Context, item: ItemEntity, calendar: Calendar) {
    // hena b5od el time ely el user e5tar we b7sab delay 3ala ases 3shan lama y5las el delay ab3t el message
    val currentTime = System.currentTimeMillis()
    val selectedTime = calendar.timeInMillis
    val delay = selectedTime - currentTime

    // bab3tlo warning 3shan ye5tar future date/time
    if (delay <= 0) {
        Toast.makeText(context, "Please select a future date and time", Toast.LENGTH_SHORT).show()
        return
    }

    // el data el hanb3tha el le notification worker
    val data = Data.Builder()
        .putString("item_name", item.name)
        .putInt("item_id", item.id)
        .build()

    // ben3ml request lel notification
    val reminderWork = OneTimeWorkRequestBuilder<Reminder>()
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .setInputData(data)
        .addTag("reminder_${item.id}")
        .build()

    // ben schedule el notification
    WorkManager.getInstance(context).enqueue(reminderWork)
}