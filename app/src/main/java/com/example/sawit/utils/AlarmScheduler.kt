package com.example.sawit.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.sawit.model.NotificationSchedule
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        private const val TAG = "AlarmScheduler"
    }

    /**
     * Schedule alarm untuk setiap hari dalam repeatDays
     */
    fun scheduleAlarm(schedule: NotificationSchedule) {
        if (schedule.repeatDays.isEmpty()) {
            // One-time alarm
            scheduleOneTimeAlarm(schedule)
        } else {
            // Schedule untuk setiap hari yang dipilih
            schedule.repeatDays.forEach { day ->
                scheduleAlarmForDay(schedule, day)
            }
        }

        Log.d(TAG, "Scheduled alarm for ${schedule.hour}:${schedule.minute}, days: ${schedule.repeatDays}")
    }

    /**
     * Schedule alarm untuk hari tertentu dengan repeat mingguan
     */
    private fun scheduleAlarmForDay(schedule: NotificationSchedule, day: String) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("SCHEDULE_ID", schedule.id)
            putExtra("SCHEDULE_TYPE", schedule.type)
            putExtra("CUSTOM_MESSAGE", schedule.customMessage)
            putExtra("DAY", day)
        }

        // Request code unik per schedule + day
        val requestCode = "${schedule.id}_$day".hashCode()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Hitung waktu alarm untuk hari yang dipilih
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, schedule.hour)
            set(Calendar.MINUTE, schedule.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // Set ke hari yang dipilih
            val targetDay = dayStringToCalendarDay(day)
            val currentDay = get(Calendar.DAY_OF_WEEK)

            var daysToAdd = targetDay - currentDay
            if (daysToAdd < 0) {
                daysToAdd += 7
            } else if (daysToAdd == 0 && timeInMillis <= System.currentTimeMillis()) {
                // Jika hari sama tapi waktu sudah lewat, set untuk minggu depan
                daysToAdd = 7
            }

            add(Calendar.DAY_OF_MONTH, daysToAdd)
        }

        // Schedule dengan repeat mingguan (7 hari)
        try {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY * 7, // Repeat setiap 7 hari
                pendingIntent
            )

            Log.d(TAG, "Alarm set for $day at ${schedule.hour}:${String.format("%02d", schedule.minute)}")
            Log.d(TAG, "Next trigger: ${calendar.time}")
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied for exact alarm", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling alarm", e)
        }
    }

    /**
     * Schedule one-time alarm
     */
    private fun scheduleOneTimeAlarm(schedule: NotificationSchedule) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("SCHEDULE_ID", schedule.id)
            putExtra("SCHEDULE_TYPE", schedule.type)
            putExtra("CUSTOM_MESSAGE", schedule.customMessage)
            putExtra("DAY", "") // Empty day untuk one-time
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, schedule.hour)
            set(Calendar.MINUTE, schedule.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )

            Log.d(TAG, "One-time alarm set at ${calendar.time}")
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied for exact alarm", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling one-time alarm", e)
        }
    }

    /**
     * Cancel semua alarm untuk schedule
     */
    fun cancelAlarm(scheduleId: String) {
        // Cancel untuk semua hari
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

        days.forEach { day ->
            val intent = Intent(context, AlarmReceiver::class.java)
            val requestCode = "${scheduleId}_$day".hashCode()

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
            )

            pendingIntent?.let {
                alarmManager.cancel(it)
                it.cancel()
            }
        }

        // Cancel one-time alarm juga
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )

        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }

        Log.d(TAG, "Cancelled alarm: $scheduleId")
    }

    /**
     * Reschedule semua alarm (untuk boot receiver)
     */
    fun rescheduleAllAlarms(schedules: List<NotificationSchedule>) {
        Log.d(TAG, "Rescheduling ${schedules.size} alarms")
        schedules.forEach { schedule ->
            if (schedule.isEnabled) {
                scheduleAlarm(schedule)
            }
        }
    }

    /**
     * Convert day string ke Calendar constant
     */
    private fun dayStringToCalendarDay(day: String): Int {
        return when (day) {
            "Sun" -> Calendar.SUNDAY
            "Mon" -> Calendar.MONDAY
            "Tue" -> Calendar.TUESDAY
            "Wed" -> Calendar.WEDNESDAY
            "Thu" -> Calendar.THURSDAY
            "Fri" -> Calendar.FRIDAY
            "Sat" -> Calendar.SATURDAY
            else -> Calendar.MONDAY
        }
    }
}