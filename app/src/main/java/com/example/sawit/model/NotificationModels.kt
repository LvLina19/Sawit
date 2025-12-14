package com.example.sawit.model


data class NotificationSchedule(
    val id: String = "",
    val userId: String = "",
    val hour: Int = 0,
    val minute: Int = 0,
    val type: String = "", // water, exercise, sleep, check_farm, custom
    val customMessage: String = "",
    val isEnabled: Boolean = true,
    val repeatDays: List<String> = listOf(), // Mon, Tue, Wed, Thu, Fri, Sat, Sun
    val createdAt: Long = 0L
) {
    /**
     * Get default message based on notification type
     */
    fun getDefaultMessage(): String {
        return if (customMessage.isNotEmpty()) {
            customMessage
        } else {
            when (type) {
                "water" -> "Jangan lupa minum air putih untuk menjaga kesehatan tubuh Anda! 💧"
                "exercise" -> "Waktunya bergerak! Luangkan waktu untuk berolahraga hari ini 💪"
                "sleep" -> "Istirahat yang cukup penting untuk kesehatan. Selamat beristirahat! 😴"
                "check_farm" -> "Saatnya cek kebun sawit Anda dan catat perkembangannya 🌴"
                else -> "Anda memiliki pengingat"
            }
        }
    }

    /**
     * Get notification title based on type
     */
    fun getNotificationTitle(): String {
        return when (type) {
            "water" -> "Waktunya Minum Air! 💧"
            "exercise" -> "Waktunya Olahraga! 💪"
            "sleep" -> "Waktunya Tidur! 😴"
            "check_farm" -> "Cek Kebun Sawit Anda! 🌴"
            "custom" -> "Pengingat"
            else -> "Pengingat Sawit"
        }
    }

    /**
     * Get notification icon resource based on type
     */
    fun getNotificationIcon(): Int {
        return when (type) {
            "water" -> com.example.sawit.R.drawable.ic_water_drop
            "exercise" -> com.example.sawit.R.drawable.ic_exercise
            "sleep" -> com.example.sawit.R.drawable.ic_sleep
            "check_farm" -> com.example.sawit.R.drawable.ic_farm
            else -> com.example.sawit.R.drawable.ic_bell
        }
    }
}