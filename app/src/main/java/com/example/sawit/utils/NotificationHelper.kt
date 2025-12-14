package com.example.sawit.utils


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent

import android.content.Context

import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.sawit.R
import com.example.sawit.model.NotificationSchedule
import java.util.*

// ===== NotificationHelper =====
class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "fitness_reminders"
        const val CHANNEL_NAME = "Fitness Reminders"
        const val CHANNEL_DESC = "Pengingat untuk aktivitas kesehatan"
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(schedule: NotificationSchedule) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pendingIntent = PendingIntent.getActivity(
            context,
            schedule.id.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(getNotificationTitle(schedule.type))
            .setContentText(schedule.getDefaultMessage())
            .setSmallIcon(getNotificationIcon(schedule.type))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(schedule.id.hashCode(), notification)
    }

    private fun getNotificationTitle(type: String): String {
        return when (type) {
            "water" -> "Pengingat Minum Air"
            "exercise" -> "Pengingat Olahraga"
            "sleep" -> "Pengingat Istirahat"
            else -> "Pengingat FitApp"
        }
    }

    private fun getNotificationIcon(type: String): Int {
        return when (type) {
            "water" -> R.drawable.ic_water_drop
            "exercise" -> R.drawable.ic_fitness
            "sleep" -> R.drawable.ic_sleep
            else -> R.drawable.ic_notification
        }
    }
}
