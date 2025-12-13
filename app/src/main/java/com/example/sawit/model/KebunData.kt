package com.example.sawit.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class KebunData(
    var id: Int = 0,
    var namaKebun: String = "",
    var luasLahan: Double = 0.0,
    var lokasiKebun: String = "",
    var jenisBibit: String = "",
    var jumlahTanaman: Int = 0,
    var tahunTanam: String = "",
    var jenisTanah: String = "",
    var imageUrl: String = "" // Optional: untuk foto kebun
) : Parcelable {

    // Constructor tanpa parameter untuk Firebase (WAJIB!)
    constructor() : this(0, "", 0.0, "", "", 0, "", "", "")

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
        return if (jumlahTanaman > 0) {
            String.format("%,d pohon", jumlahTanaman)
        } else {
            "Tidak diisi"
        }
    }

    /**
     * Hitung estimasi jumlah tanaman per hektar
     */
    fun getTanamanPerHektar(): Int {
        return if (luasLahan > 0 && jumlahTanaman > 0) {
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
            if (tahunTanamInt > 0) currentYear - tahunTanamInt else 0
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Get formatted usia kebun
     */
    fun getFormattedUsia(): String {
        val usia = getUsiaTahun()
        return if (usia > 0) "$usia tahun" else "Baru ditanam"
    }

    /**
     * Validasi data kebun
     */
    fun isValid(): Boolean {
        return namaKebun.isNotEmpty() &&
                luasLahan > 0 &&
                lokasiKebun.isNotEmpty() &&
                jenisBibit.isNotEmpty() &&
                tahunTanam.isNotEmpty() &&
                jenisTanah.isNotEmpty()
    }
}