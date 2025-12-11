package com.example.sawit.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PanenData(
    val id: Int = 0,
    val kebunId: Int = 0,
    val tanggalPanen: String = "",
    val lokasi: String = "",
    val tbsMatang: Double = 0.0,
    val tbsTidakMatang: Double = 0.0,
    val tbsKelewatMatang: Double = 0.0,
    val jumlahMatang: Int = 0,
    val jumlahTidakMatang: Int = 0,
    val jumlahKelewatMatang: Int = 0,
    val hargaPerKg: Double = 2000.0, // Harga default per kg
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable {

    /**
     * Hitung total berat TBS
     */
    fun getTotalBerat(): Double {
        return tbsMatang + tbsTidakMatang + tbsKelewatMatang
    }

    /**
     * Hitung total jumlah buah
     */
    fun getTotalJumlah(): Int {
        return jumlahMatang + jumlahTidakMatang + jumlahKelewatMatang
    }

    /**
     * Hitung total pendapatan
     */
    fun getTotalPendapatan(): Double {
        return getTotalBerat() * hargaPerKg
    }

    /**
     * Format pendapatan ke Rupiah
     */
    fun getFormattedPendapatan(): String {
        return String.format("Rp. %,.0f", getTotalPendapatan())
    }

    /**
     * Get bulan dari tanggal
     */
    fun getBulan(): String {
        // Format tanggal: "15 Januari 2024"
        return try {
            tanggalPanen.split(" ").getOrNull(1) ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}