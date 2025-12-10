package com.example.sawit.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class KebunData(
    val id: Int = 0,
    val namaKebun: String = "",
    val luasLahan: Double = 0.0,
    val lokasiKebun: String = "",
    val jenisBibit: String = "",
    val jumlahTanaman: Int = 0,
    val tahunTanam: String = "",
    val jenisTanah: String = ""
) : Parcelable {

    /**
     * Format luas lahan dengan 2 desimal
     */
    fun getFormattedLuas(): String {
        return String.format("%.2f Ha", luasLahan)
    }

    /**
     * Format jumlah tanaman dengan thousand separator
     */
    fun getFormattedJumlahTanaman(): String {
        return String.format("%,d pohon", jumlahTanaman)
    }

    /**
     * Hitung estimasi jumlah tanaman per hektar
     */
    fun getTanamanPerHektar(): Int {
        return if (luasLahan > 0) {
            (jumlahTanaman / luasLahan).toInt()
        } else {
            0
        }
    }

    /**
     * Dapatkan usia kebun dalam tahun
     */
    fun getUsiaTahun(): Int {
        return try {
            val tahunTanamInt = tahunTanam.split(" ").lastOrNull()?.toIntOrNull() ?: 0
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            currentYear - tahunTanamInt
        } catch (e: Exception) {
            0
        }
    }
}