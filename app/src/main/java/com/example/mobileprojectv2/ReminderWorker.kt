package com.example.mobileprojectv2

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class ReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val itemName = inputData.getString("item_name") ?: "Item"
        val itemId = inputData.getInt("item_id", -1)

        sendNotification(itemName, itemId)

        return Result.success()
    }

    private fun sendNotification(itemName: String, itemId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "grocery_reminders"

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Grocery Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for grocery items"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent to open the app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            if (itemId != -1) {
                putExtra("itemId", itemId)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            itemId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🛒 Grocery Reminder")
            .setContentText("Don't forget to buy: $itemName")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Don't forget to buy: $itemName\n\nTap to view your grocery list."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        notificationManager.notify(itemId, notification)
    }
}