package com.example.sawit.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.sawit.model.KebunData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * KebunManager - Singleton class untuk manage data kebun
 * Menggunakan SharedPreferences untuk menyimpan data
 */
class KebunManager private constructor(context: Context) {

    private val sharedPreferences: SharedPreferences
    private val gson: Gson = Gson()

    companion object {
        private const val PREF_NAME = "KebunPreferences"
        private const val KEY_KEBUN_LIST = "kebun_list"
        private const val KEY_LAST_ID = "last_id"

        @Volatile
        private var INSTANCE: KebunManager? = null

        fun getInstance(context: Context): KebunManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: KebunManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    init {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Simpan data kebun baru
     */
    fun saveKebun(kebunData: KebunData): Boolean {
        return try {
            // Get existing list
            val kebunList = getAllKebun().toMutableList()

            // Generate ID baru
            val newId = getNextId()
            val kebunWithId = kebunData.copy(id = newId)

            // Tambahkan ke list
            kebunList.add(kebunWithId)

            // Save ke SharedPreferences
            saveKebunList(kebunList)

            // Update last ID
            saveLastId(newId)

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Ambil semua data kebun
     */
    fun getAllKebun(): List<KebunData> {
        return try {
            val json = sharedPreferences.getString(KEY_KEBUN_LIST, null)
            if (json != null) {
                val type = object : TypeToken<List<KebunData>>() {}.type
                gson.fromJson(json, type)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Ambil kebun berdasarkan ID
     */
    fun getKebunById(id: Int): KebunData? {
        return getAllKebun().find { it.id == id }
    }

    /**
     * Update data kebun
     */
    fun updateKebun(kebunData: KebunData): Boolean {
        return try {
            val kebunList = getAllKebun().toMutableList()
            val index = kebunList.indexOfFirst { it.id == kebunData.id }

            if (index != -1) {
                kebunList[index] = kebunData
                saveKebunList(kebunList)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Hapus data kebun
     */
    fun deleteKebun(id: Int): Boolean {
        return try {
            val kebunList = getAllKebun().toMutableList()
            val removed = kebunList.removeIf { it.id == id }

            if (removed) {
                saveKebunList(kebunList)
            }

            removed
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Hapus semua data kebun
     */
    fun clearAllKebun(): Boolean {
        return try {
            sharedPreferences.edit().clear().apply()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Cek apakah ada data kebun
     */
    fun hasKebun(): Boolean {
        return getAllKebun().isNotEmpty()
    }

    /**
     * Get total jumlah kebun
     */
    fun getKebunCount(): Int {
        return getAllKebun().size
    }

    // ========== PRIVATE HELPER FUNCTIONS ==========

    private fun saveKebunList(kebunList: List<KebunData>) {
        val json = gson.toJson(kebunList)
        sharedPreferences.edit().putString(KEY_KEBUN_LIST, json).apply()
    }

    private fun getNextId(): Int {
        val lastId = sharedPreferences.getInt(KEY_LAST_ID, 0)
        return lastId + 1
    }

    private fun saveLastId(id: Int) {
        sharedPreferences.edit().putInt(KEY_LAST_ID, id).apply()
    }
}