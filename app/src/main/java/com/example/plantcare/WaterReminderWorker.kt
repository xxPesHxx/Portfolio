package com.example.plantcare

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.plantcare.functions.PlantRepository

class WaterReminderWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val plantRepository = PlantRepository()
        val plants = plantRepository.getPlant()
        val currentTime = System.currentTimeMillis()

        plants.forEach { plant ->
            val lastWateredTime = plant.lastWatered.seconds * 1000
            val nextWateringTime = lastWateredTime + (plant.waterInterval * 24 * 60 * 60 * 1000)

            if (nextWateringTime - currentTime <= 24 * 60 * 60 * 1000) {
                sendNotification(plant)
            }
        }
        return Result.success()
    }


    private fun sendNotification(plant: Plants) {
        val channelId = "water_reminder_channel"
        val notificationId = (System.currentTimeMillis() % 10000).toInt()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Powiadomienia o podlewaniu", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle("Czas podlać roślinę!")
            .setContentText("Podlej: ${plant.name}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                NotificationManagerCompat.from(applicationContext).notify(notificationId, notification)
            }
        } else {
            NotificationManagerCompat.from(applicationContext).notify(notificationId, notification)
        }
    }
}
