package com.example.plantcare

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            scheduleWateringWork(context)
        }
    }
}

fun scheduleWateringWork(context: Context) {
    val workRequest = PeriodicWorkRequestBuilder<WaterReminderWorker>(1, TimeUnit.HOURS).build()
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "WaterReminderWork",
        ExistingPeriodicWorkPolicy.REPLACE,
        workRequest
    )
}
