package com.example.sawit.utils

import android.content.Context
import android.util.Log
import com.example.sawit.model.KebunData
import com.google.firebase.database.*
import com.google.firebase.database.DatabaseReference

/**
 * KebunManager - Singleton class untuk manage data kebun dengan Firebase
 */
class KebunManager private constructor(context: Context) {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val kebunRef: DatabaseReference = database.child("kebun")

    // Untuk cache local (optional, bisa digunakan saat offline)
    private var cachedKebunList: MutableList<KebunData> = mutableListOf()

    // Map untuk menyimpan Firebase key dengan kebun ID
    private val kebunKeyMap: MutableMap<Int, String> = mutableMapOf()

    companion object {
        private const val TAG = "KebunManager"

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
        // Enable offline persistence
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        } catch (e: Exception) {
            Log.e(TAG, "Persistence already enabled", e)
        }
    }

    /**
     * Simpan data kebun baru ke Firebase
     */
    fun saveKebun(kebunData: KebunData, onComplete: (Boolean, String?) -> Unit) {
        try {
            // Generate ID unik dari Firebase
            val newKebunRef = kebunRef.push()
            val firebaseKey = newKebunRef.key

            if (firebaseKey == null) {
                onComplete(false, "Failed to generate Firebase key")
                return
            }

            // Generate ID integer dari timestamp
            val kebunId = System.currentTimeMillis().toInt()

            // Set ID ke kebunData
            val kebunWithId = kebunData.copy(id = kebunId)

            // Simpan ke Firebase
            newKebunRef.setValue(kebunWithId)
                .addOnSuccessListener {
                    cachedKebunList.add(kebunWithId)
                    kebunKeyMap[kebunId] = firebaseKey
                    Log.d(TAG, "Kebun saved successfully with ID: $kebunId")
                    onComplete(true, null)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error saving kebun", exception)
                    onComplete(false, exception.message)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in saveKebun", e)
            onComplete(false, e.message)
        }
    }

    /**
     * Ambil semua data kebun dari Firebase
     */
    fun getAllKebun(onComplete: (List<KebunData>?) -> Unit) {
        kebunRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val kebunList = mutableListOf<KebunData>()
                kebunKeyMap.clear()

                try {
                    for (childSnapshot in snapshot.children) {
                        val kebun = childSnapshot.getValue(KebunData::class.java)
                        if (kebun != null) {
                            kebunList.add(kebun)
                            childSnapshot.key?.let { key ->
                                kebunKeyMap[kebun.id] = key
                            }
                        }
                    }

                    cachedKebunList = kebunList
                    Log.d(TAG, "Loaded ${kebunList.size} kebun from Firebase")
                    onComplete(kebunList)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing kebun data", e)
                    onComplete(cachedKebunList.ifEmpty { null })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error loading kebun: ${error.message}")
                // Jika error, return cached data
                onComplete(if (cachedKebunList.isNotEmpty()) cachedKebunList else null)
            }
        })
    }

    /**
     * Listen real-time changes (untuk auto-update UI)
     */
    fun listenToKebunChanges(onChange: (List<KebunData>) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val kebunList = mutableListOf<KebunData>()
                kebunKeyMap.clear()

                try {
                    for (childSnapshot in snapshot.children) {
                        val kebun = childSnapshot.getValue(KebunData::class.java)
                        if (kebun != null) {
                            kebunList.add(kebun)
                            childSnapshot.key?.let { key ->
                                kebunKeyMap[kebun.id] = key
                            }
                        }
                    }

                    cachedKebunList = kebunList
                    Log.d(TAG, "Real-time update: ${kebunList.size} kebun")
                    onChange(kebunList)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in real-time listener", e)
                    onChange(cachedKebunList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Real-time listener cancelled: ${error.message}")
                onChange(cachedKebunList)
            }
        }

        kebunRef.addValueEventListener(listener)
        return listener
    }

    /**
     * Remove listener
     */
    fun removeListener(listener: ValueEventListener) {
        kebunRef.removeEventListener(listener)
        Log.d(TAG, "Listener removed")
    }

    /**
     * Ambil kebun berdasarkan ID
     */
    fun getKebunById(id: Int, onComplete: (KebunData?) -> Unit) {
        // Cek di cache dulu
        val cachedKebun = cachedKebunList.find { it.id == id }
        if (cachedKebun != null) {
            onComplete(cachedKebun)
            return
        }

        // Jika tidak ada di cache, ambil dari Firebase
        kebunRef.orderByChild("id").equalTo(id.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val kebun = snapshot.children.firstOrNull()?.getValue(KebunData::class.java)
                    onComplete(kebun)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Error getting kebun by ID: ${error.message}")
                    onComplete(null)
                }
            })
    }

    /**
     * Update data kebun
     */
    fun updateKebun(kebunData: KebunData, onComplete: (Boolean, String?) -> Unit) {
        val firebaseKey = kebunKeyMap[kebunData.id]

        if (firebaseKey != null) {
            // Update menggunakan Firebase key yang sudah disimpan
            kebunRef.child(firebaseKey).setValue(kebunData)
                .addOnSuccessListener {
                    // Update cache
                    val index = cachedKebunList.indexOfFirst { it.id == kebunData.id }
                    if (index != -1) {
                        cachedKebunList[index] = kebunData
                    }
                    Log.d(TAG, "Kebun updated successfully")
                    onComplete(true, null)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error updating kebun", exception)
                    onComplete(false, exception.message)
                }
        } else {
            // Fallback: cari menggunakan query
            kebunRef.orderByChild("id").equalTo(kebunData.id.toDouble())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val childSnapshot = snapshot.children.firstOrNull()

                        if (childSnapshot != null) {
                            childSnapshot.ref.setValue(kebunData)
                                .addOnSuccessListener {
                                    childSnapshot.key?.let { key ->
                                        kebunKeyMap[kebunData.id] = key
                                    }
                                    onComplete(true, null)
                                }
                                .addOnFailureListener { exception ->
                                    onComplete(false, exception.message)
                                }
                        } else {
                            onComplete(false, "Kebun not found")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        onComplete(false, error.message)
                    }
                })
        }
    }

    /**
     * Hapus data kebun
     */
    fun deleteKebun(id: Int, onComplete: (Boolean, String?) -> Unit) {
        val firebaseKey = kebunKeyMap[id]

        if (firebaseKey != null) {
            // Hapus menggunakan Firebase key yang sudah disimpan
            kebunRef.child(firebaseKey).removeValue()
                .addOnSuccessListener {
                    cachedKebunList.removeIf { it.id == id }
                    kebunKeyMap.remove(id)
                    Log.d(TAG, "Kebun deleted successfully")
                    onComplete(true, null)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error deleting kebun", exception)
                    onComplete(false, exception.message)
                }
        } else {
            // Fallback: cari dan hapus menggunakan query
            kebunRef.orderByChild("id").equalTo(id.toDouble())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val childSnapshot = snapshot.children.firstOrNull()

                        if (childSnapshot != null) {
                            childSnapshot.ref.removeValue()
                                .addOnSuccessListener {
                                    cachedKebunList.removeIf { it.id == id }
                                    kebunKeyMap.remove(id)
                                    Log.d(TAG, "Kebun deleted successfully (fallback)")
                                    onComplete(true, null)
                                }
                                .addOnFailureListener { exception ->
                                    Log.e(TAG, "Error deleting kebun (fallback)", exception)
                                    onComplete(false, exception.message)
                                }
                        } else {
                            Log.w(TAG, "Kebun not found for deletion")
                            onComplete(false, "Kebun not found")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "Error in delete query: ${error.message}")
                        onComplete(false, error.message)
                    }
                })
        }
    }

    /**
     * Hapus semua data kebun
     */
    fun clearAllKebun(onComplete: (Boolean, String?) -> Unit) {
        kebunRef.removeValue()
            .addOnSuccessListener {
                cachedKebunList.clear()
                kebunKeyMap.clear()
                Log.d(TAG, "All kebun cleared")
                onComplete(true, null)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error clearing all kebun", exception)
                onComplete(false, exception.message)
            }
    }

    /**
     * Cek apakah ada data kebun (dari cache)
     */
    fun hasKebun(): Boolean {
        return cachedKebunList.isNotEmpty()
    }

    /**
     * Get total jumlah kebun (dari cache)
     */
    fun getKebunCount(): Int {
        return cachedKebunList.size
    }

    /**
     * Get cached data (untuk offline mode)
     */
    fun getCachedKebun(): List<KebunData> {
        return cachedKebunList.toList()
    }
}


//package com.example.sawit.utils
//
//import android.content.Context
//import android.content.SharedPreferences
//import com.example.sawit.model.KebunData
//import com.google.gson.Gson
//import com.google.gson.reflect.TypeToken
//
///**
// * KebunManager - Singleton class untuk manage data kebun
// * Menggunakan SharedPreferences untuk menyimpan data
// */
//class KebunManager private constructor(context: Context) {
//
//    private val sharedPreferences: SharedPreferences
//    private val gson: Gson = Gson()
//
//    companion object {
//        private const val PREF_NAME = "KebunPreferences"
//        private const val KEY_KEBUN_LIST = "kebun_list"
//        private const val KEY_LAST_ID = "last_id"
//
//        @Volatile
//        private var INSTANCE: KebunManager? = null
//
//        fun getInstance(context: Context): KebunManager {
//            return INSTANCE ?: synchronized(this) {
//                INSTANCE ?: KebunManager(context.applicationContext).also {
//                    INSTANCE = it
//                }
//            }
//        }
//    }
//
//    init {
//        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
//    }
//
//    /**
//     * Simpan data kebun baru
//     */
//    fun saveKebun(kebunData: KebunData): Boolean {
//        return try {
//            // Get existing list
//            val kebunList = getAllKebun().toMutableList()
//
//            // Generate ID baru
//            val newId = getNextId()
//            val kebunWithId = kebunData.copy(id = newId)
//
//            // Tambahkan ke list
//            kebunList.add(kebunWithId)
//
//            // Save ke SharedPreferences
//            saveKebunList(kebunList)
//
//            // Update last ID
//            saveLastId(newId)
//
//            true
//        } catch (e: Exception) {
//            e.printStackTrace()
//            false
//        }
//    }
//
//    /**
//     * Ambil semua data kebun
//     */
//    fun getAllKebun(): List<KebunData> {
//        return try {
//            val json = sharedPreferences.getString(KEY_KEBUN_LIST, null)
//            if (json != null) {
//                val type = object : TypeToken<List<KebunData>>() {}.type
//                gson.fromJson(json, type)
//            } else {
//                emptyList()
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            emptyList()
//        }
//    }
//
//    /**
//     * Ambil kebun berdasarkan ID
//     */
//    fun getKebunById(id: Int): KebunData? {
//        return getAllKebun().find { it.id == id }
//    }
//
//    /**
//     * Update data kebun
//     */
//    fun updateKebun(kebunData: KebunData): Boolean {
//        return try {
//            val kebunList = getAllKebun().toMutableList()
//            val index = kebunList.indexOfFirst { it.id == kebunData.id }
//
//            if (index != -1) {
//                kebunList[index] = kebunData
//                saveKebunList(kebunList)
//                true
//            } else {
//                false
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            false
//        }
//    }
//
//    /**
//     * Hapus data kebun
//     */
//    fun deleteKebun(id: Int): Boolean {
//        return try {
//            val kebunList = getAllKebun().toMutableList()
//            val removed = kebunList.removeIf { it.id == id }
//
//            if (removed) {
//                saveKebunList(kebunList)
//            }
//
//            removed
//        } catch (e: Exception) {
//            e.printStackTrace()
//            false
//        }
//    }
//
//    /**
//     * Hapus semua data kebun
//     */
//    fun clearAllKebun(): Boolean {
//        return try {
//            sharedPreferences.edit().clear().apply()
//            true
//        } catch (e: Exception) {
//            e.printStackTrace()
//            false
//        }
//    }
//
//    /**
//     * Cek apakah ada data kebun
//     */
//    fun hasKebun(): Boolean {
//        return getAllKebun().isNotEmpty()
//    }
//
//    /**
//     * Get total jumlah kebun
//     */
//    fun getKebunCount(): Int {
//        return getAllKebun().size
//    }
//
//    // ========== PRIVATE HELPER FUNCTIONS ==========
//
//    private fun saveKebunList(kebunList: List<KebunData>) {
//        val json = gson.toJson(kebunList)
//        sharedPreferences.edit().putString(KEY_KEBUN_LIST, json).apply()
//    }
//
//    private fun getNextId(): Int {
//        val lastId = sharedPreferences.getInt(KEY_LAST_ID, 0)
//        return lastId + 1
//    }
//
//    private fun saveLastId(id: Int) {
//        sharedPreferences.edit().putInt(KEY_LAST_ID, id).apply()
//    }
//}