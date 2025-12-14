package com.example.sawit.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.sawit.model.PanenData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PanenManager private constructor(context: Context) {

    private val sharedPreferences: SharedPreferences
    private val gson: Gson = Gson()

    companion object {
        private const val PREF_NAME = "PanenPreferences"
        private const val KEY_PANEN_LIST = "panen_list"
        private const val KEY_LAST_ID = "last_id"

        @Volatile
        private var INSTANCE: PanenManager? = null

        fun getInstance(context: Context): PanenManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PanenManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    init {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Simpan data panen baru
     */
    fun savePanen(panenData: PanenData): Boolean {
        return try {
            val panenList = getAllPanen().toMutableList()
            val newId = getNextId()
            val panenWithId = panenData.copy(id = newId)
            panenList.add(panenWithId)
            savePanenList(panenList)
            saveLastId(newId)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Ambil semua data panen
     */
    fun getAllPanen(): List<PanenData> {
        return try {
            val json = sharedPreferences.getString(KEY_PANEN_LIST, null)
            if (json != null) {
                val type = object : TypeToken<List<PanenData>>() {}.type
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
     * Ambil panen berdasarkan kebun ID
     */
    fun getPanenByKebunId(kebunId: Int): List<PanenData> {
        return getAllPanen().filter { it.kebunId == kebunId }
    }

    /**
     * Ambil total pendapatan per kebun
     */
    fun getTotalPendapatanByKebun(kebunId: Int): Double {
        return getPanenByKebunId(kebunId).sumOf { it.getTotalPendapatan() }
    }

    /**
     * Ambil data untuk grafik (per bulan)
     */
    fun getGrafikData(kebunId: Int): Map<String, Double> {
        return getPanenByKebunId(kebunId)
            .groupBy { it.getBulan() }
            .mapValues { entry -> entry.value.sumOf { it.getTotalPendapatan() } }
    }

    /**
     * Delete panen
     */
    fun deletePanen(id: Int): Boolean {
        return try {
            val panenList = getAllPanen().toMutableList()
            val removed = panenList.removeIf { it.id == id }
            if (removed) {
                savePanenList(panenList)
            }
            removed
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Clear all panen data
     */
    fun clearAllPanen(): Boolean {
        return try {
            sharedPreferences.edit().clear().apply()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Private helper functions
    private fun savePanenList(panenList: List<PanenData>) {
        val json = gson.toJson(panenList)
        sharedPreferences.edit().putString(KEY_PANEN_LIST, json).apply()
    }

    private fun getNextId(): Int {
        val lastId = sharedPreferences.getInt(KEY_LAST_ID, 0)
        return lastId + 1
    }

    private fun saveLastId(id: Int) {
        sharedPreferences.edit().putInt(KEY_LAST_ID, id).apply()
    }
}