package com.example.sawit.model

data class UserSettings(
    val userId: String = "",
    val language: String = "id",
    val theme: String = "system",
    val notificationsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
)