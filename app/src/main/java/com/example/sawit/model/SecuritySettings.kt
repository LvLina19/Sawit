package com.example.sawit.model

data class SecuritySettings(
    val userId: String = "",
    val biometricEnabled: Boolean = false,
    val pinEnabled: Boolean = false,
    val pinCode: String = "", // Hashed
    val twoFactorEnabled: Boolean = false,
    val lastPasswordChange: Long = 0
)