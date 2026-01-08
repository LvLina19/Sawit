package com.example.sawit.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.sawit.Dashboard
import com.example.sawit.R
import java.util.*

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getStringExtra("SCHEDULE_ID") ?: ""
        val scheduleType = intent.getStringExtra("SCHEDULE_TYPE") ?: ""
        val customMessage = intent.getStringExtra("CUSTOM_MESSAGE") ?: ""
        val day = intent.getStringExtra("DAY") ?: ""

        Log.d("AlarmReceiver", "Alarm received - Type: $scheduleType, Day: $day, Time: ${Date()}")

        // Validasi: Cek apakah hari ini sesuai dengan schedule
        // Jika day kosong berarti one-time alarm, langsung tampilkan
        if (day.isNotEmpty() && !isToday(day)) {
            Log.d("AlarmReceiver", "Not today, skipping. Expected: $day, Current: ${getCurrentDay()}")
            return
        }

        val (title, message, icon) = when (scheduleType) {
            "water" -> Triple(
                "Waktunya Minum Air! 💧",
                "Jangan lupa minum air putih untuk menjaga kesehatan",
                R.drawable.ic_water_drop
            )
            "exercise" -> Triple(
                "Waktunya Olahraga! 💪",
                "Luangkan waktu untuk berolahraga dan jaga kesehatan tubuh",
                R.drawable.ic_exercise
            )
            "sleep" -> Triple(
                "Waktunya Tidur! 😴",
                "Istirahat yang cukup penting untuk kesehatan Anda",
                R.drawable.ic_sleep
            )
            "check_farm" -> Triple(
                "Cek Kebun Sawit Anda! 🌴",
                "Periksa kondisi kebun dan catat perkembangannya",
                R.drawable.ic_farm
            )
            "custom" -> Triple(
                "Pengingat",
                customMessage,
                R.drawable.ic_bell
            )
            else -> Triple(
                "Pengingat",
                "Anda memiliki pengingat",
                R.drawable.ic_bell
            )
        }

        showNotification(context, title, message, icon)
        Log.d("AlarmReceiver", "Notification shown successfully")
    }

    /**
     * Cek apakah hari ini sesuai dengan day parameter
     */
    private fun isToday(day: String): Boolean {
        val currentDay = getCurrentDay()
        return currentDay == day
    }

    /**
     * Get current day string
     */
    private fun getCurrentDay(): String {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Mon"
            Calendar.TUESDAY -> "Tue"
            Calendar.WEDNESDAY -> "Wed"
            Calendar.THURSDAY -> "Thu"
            Calendar.FRIDAY -> "Fri"
            Calendar.SATURDAY -> "Sat"
            Calendar.SUNDAY -> "Sun"
            else -> ""
        }
    }

    private fun showNotification(context: Context, title: String, message: String, iconRes: Int) {
        val channelId = "sawit_reminder_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel untuk Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Pengingat Sawit",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi pengingat untuk aplikasi Sawit"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                enableLights(true)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    null
                )
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent untuk buka app saat notifikasi di-tap
        val intent = Intent(context, Dashboard::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        // Show notification dengan ID unik
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)

        Log.d("AlarmReceiver", "Notification posted with ID: $notificationId")
    }
}