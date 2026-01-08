package com.example.sawit.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.sawit.model.NotificationSchedule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Device booted, rescheduling alarms")
            rescheduleAlarms(context)
        }
    }

    private fun rescheduleAlarms(context: Context) {
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Log.d(TAG, "No user logged in, skipping alarm reschedule")
            return
        }

        val firestore = FirebaseFirestore.getInstance()
        val alarmScheduler = AlarmScheduler(context)

        firestore.collection("notification_schedules")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isEnabled", true)
            .get()
            .addOnSuccessListener { documents ->
                val schedules = documents.mapNotNull { doc ->
                    try {
                        doc.toObject(NotificationSchedule::class.java)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing schedule: ${doc.id}", e)
                        null
                    }
                }

                alarmScheduler.rescheduleAllAlarms(schedules)
                Log.d(TAG, "Rescheduled ${schedules.size} alarms")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error rescheduling alarms", e)
            }
    }
}