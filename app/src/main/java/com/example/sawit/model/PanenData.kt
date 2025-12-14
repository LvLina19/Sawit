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
    val hargaPerKg: Double = 2000.0,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable {

    // Constructor tanpa parameter untuk Firebase (WAJIB!)
    constructor() : this(0, 0, "", "", 0.0, 0.0, 0.0, 0, 0, 0, 2000.0, System.currentTimeMillis())

    /**
     * Hitung total berat TBS
     */
    fun getTotalBerat(): Double {
        return tbsMatang + tbsTidakMatang + tbsKelewatMatang
    }

    /**
     * Format total berat
     */
    fun getFormattedTotalBerat(): String {
        return String.format("%.2f kg", getTotalBerat())
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
        return String.format("Rp %,.0f", getTotalPendapatan())
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

    /**
     * Get tahun dari tanggal
     */
    fun getTahun(): String {
        return try {
            tanggalPanen.split(" ").lastOrNull() ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Format tanggal pendek (dd/MM/yyyy)
     */
    fun getFormattedTanggalPendek(): String {
        return try {
            val parts = tanggalPanen.split(" ")
            val day = parts[0]
            val month = when (parts[1]) {
                "Januari" -> "01"
                "Februari" -> "02"
                "Maret" -> "03"
                "April" -> "04"
                "Mei" -> "05"
                "Juni" -> "06"
                "Juli" -> "07"
                "Agustus" -> "08"
                "September" -> "09"
                "Oktober" -> "10"
                "November" -> "11"
                "Desember" -> "12"
                else -> "00"
            }
            val year = parts[2]
            "$day/$month/$year"
        } catch (e: Exception) {
            tanggalPanen
        }
    }

    /**
     * Hitung persentase TBS Matang
     */
    fun getPersentaseMatang(): Double {
        val total = getTotalBerat()
        return if (total > 0) (tbsMatang / total) * 100 else 0.0
    }

    /**
     * Hitung persentase TBS Tidak Matang
     */
    fun getPersentaseTidakMatang(): Double {
        val total = getTotalBerat()
        return if (total > 0) (tbsTidakMatang / total) * 100 else 0.0
    }

    /**
     * Hitung persentase TBS Kelewat Matang
     */
    fun getPersentaseKelewatMatang(): Double {
        val total = getTotalBerat()
        return if (total > 0) (tbsKelewatMatang / total) * 100 else 0.0
    }

    /**
     * Get kualitas panen (berdasarkan persentase matang)
     */
    fun getKualitasPanen(): String {
        val persentaseMatang = getPersentaseMatang()
        return when {
            persentaseMatang >= 80 -> "Sangat Baik"
            persentaseMatang >= 60 -> "Baik"
            persentaseMatang >= 40 -> "Cukup"
            else -> "Kurang"
        }
    }
}


//package com.example.sawit.model
//
//import android.os.Parcelable
//import kotlinx.parcelize.Parcelize
//
//@Parcelize
//data class PanenData(
//    val id: Int = 0,
//    val kebunId: Int = 0,
//    val tanggalPanen: String = "",
//    val lokasi: String = "",
//    val tbsMatang: Double = 0.0,
//    val tbsTidakMatang: Double = 0.0,
//    val tbsKelewatMatang: Double = 0.0,
//    val jumlahMatang: Int = 0,
//    val jumlahTidakMatang: Int = 0,
//    val jumlahKelewatMatang: Int = 0,
//    val hargaPerKg: Double = 2000.0, // Harga default per kg
//    val timestamp: Long = System.currentTimeMillis()
//) : Parcelable {
//
//    /**
//     * Hitung total berat TBS
//     */
//    fun getTotalBerat(): Double {
//        return tbsMatang + tbsTidakMatang + tbsKelewatMatang
//    }
//
//    /**
//     * Hitung total jumlah buah
//     */
//    fun getTotalJumlah(): Int {
//        return jumlahMatang + jumlahTidakMatang + jumlahKelewatMatang
//    }
//
//    /**
//     * Hitung total pendapatan
//     */
//    fun getTotalPendapatan(): Double {
//        return getTotalBerat() * hargaPerKg
//    }
//
//    /**
//     * Format pendapatan ke Rupiah
//     */
//    fun getFormattedPendapatan(): String {
//        return String.format("Rp. %,.0f", getTotalPendapatan())
//    }
//
//    /**
//     * Get bulan dari tanggal
//     */
//    fun getBulan(): String {
//        // Format tanggal: "15 Januari 2024"
//        return try {
//            tanggalPanen.split(" ").getOrNull(1) ?: ""
//        } catch (e: Exception) {
//            ""
//        }
//    }
//}