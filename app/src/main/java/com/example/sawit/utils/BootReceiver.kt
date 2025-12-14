package com.example.sawit.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule semua alarm yang aktif
            rescheduleAlarms(context)
        }
    }

    private fun rescheduleAlarms(context: Context) {
        // Load all active schedules from Firebase
        // Then reschedule them
        val alarmScheduler = AlarmScheduler(context)

        // TODO: Load dari Firebase
        // alarmScheduler.rescheduleAllAlarms(schedules)
    }
}