package com.example.mobileprojectv2

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")

        // Save token to SharedPreferences or send to your server
        getSharedPreferences("FCM", Context.MODE_PRIVATE)
            .edit()
            .putString("token", token)
            .apply()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d("FCM", "From: ${message.from}")

        // Check if message contains a notification payload
        message.notification?.let {
            val title = it.title ?: "Grocery Reminder"
            val body = it.body ?: "Don't forget to buy your items!"
            sendNotification(title, body)
        }

        // Check if message contains a data payload
        if (message.data.isNotEmpty()) {
            Log.d("FCM", "Message data payload: ${message.data}")
            handleDataPayload(message.data)
        }
    }

    private fun handleDataPayload(data: Map<String, String>) {
        val title = data["title"] ?: "Grocery Reminder"
        val body = data["body"] ?: "Check your grocery list!"
        val itemId = data["itemId"]?.toIntOrNull()

        sendNotification(title, body, itemId)
    }

    private fun sendNotification(title: String, messageBody: String, itemId: Int? = null) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            itemId?.let { putExtra("itemId", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = "grocery_reminders"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Grocery Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for grocery item reminders"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}