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
import com.example.mobileprojectv2.R.drawable.ic_stat_name

class Reminder(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        // begneeb info 3en el item ely han3molo reminder
        val itemName = inputData.getString("item_name") ?: "Item"
        val itemId = inputData.getInt("item_id", -1)

        // benb3t el notification
        sendNotification(itemName, itemId)

        return Result.success()
    }

    private fun sendNotification(itemName: String, itemId: Int) {
        // bengeeb el notification manager
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "grocery_reminders"

        // ben3ml channel 3shan neb3t fyha el reminder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Grocery Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Reminders for grocery items"
            channel.enableLights(true)
            channel.enableVibration(true)
            notificationManager.createNotificationChannel(channel)
        }

        // 7etet el intent 3shan lama el user ydos 3al notification yero7 3al app
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        if (itemId != -1) {
            intent.putExtra("itemId", itemId)
        }

        //pending intent mestanya el user yedos 3al notification
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            itemId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // hena ben3ml shakl el notification
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(ic_stat_name)
            .setContentTitle("🛒 Grocery Reminder")
            .setContentText("Don't forget to buy: $itemName")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Don't forget to buy: $itemName\n\nTap to view your grocery list.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        // bentofy el os bel notification
        notificationManager.notify(itemId, notification)
    }
}