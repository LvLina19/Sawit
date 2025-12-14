package com.example.sawit.utils

import android.content.Context
import android.util.Log
import com.example.sawit.model.PanenData
import com.google.firebase.database.*

/**
 * PanenManager - Singleton class untuk manage data panen dengan Firebase
 */
class PanenManager private constructor(context: Context) {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val panenRef: DatabaseReference = database.child("panen")

    // Untuk cache local (optional, bisa digunakan saat offline)
    private var cachedPanenList: MutableList<PanenData> = mutableListOf()

    // Map untuk menyimpan Firebase key dengan panen ID
    private val panenKeyMap: MutableMap<Int, String> = mutableMapOf()

    companion object {
        private const val TAG = "PanenManager"

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

    /**
     * Simpan data panen baru ke Firebase
     */
    fun savePanen(panenData: PanenData, onComplete: (Boolean, String?) -> Unit) {
        try {
            // Generate ID unik dari Firebase
            val newPanenRef = panenRef.push()
            val firebaseKey = newPanenRef.key

            if (firebaseKey == null) {
                onComplete(false, "Failed to generate Firebase key")
                return
            }

            // Generate ID integer dari timestamp
            val panenId = System.currentTimeMillis().toInt()

            // Set ID ke panenData
            val panenWithId = panenData.copy(id = panenId)

            // Simpan ke Firebase
            newPanenRef.setValue(panenWithId)
                .addOnSuccessListener {
                    cachedPanenList.add(panenWithId)
                    panenKeyMap[panenId] = firebaseKey
                    Log.d(TAG, "Panen saved successfully with ID: $panenId")
                    onComplete(true, null)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error saving panen", exception)
                    onComplete(false, exception.message)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in savePanen", e)
            onComplete(false, e.message)
        }
    }

    /**
     * Ambil semua data panen dari Firebase
     */
    fun getAllPanen(onComplete: (List<PanenData>?) -> Unit) {
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "🔍 GETTING ALL PANEN FROM FIREBASE")

        panenRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "")
                Log.d(TAG, "📡 FIREBASE RESPONSE:")
                Log.d(TAG, "   Total children: ${snapshot.childrenCount}")

                val panenList = mutableListOf<PanenData>()
                panenKeyMap.clear()

                try {
                    var index = 0
                    for (childSnapshot in snapshot.children) {
                        val panen = childSnapshot.getValue(PanenData::class.java)

                        if (panen != null) {
                            panenList.add(panen)
                            childSnapshot.key?.let { key ->
                                panenKeyMap[panen.id] = key
                            }

                            Log.d(TAG, "   [$index] ID: ${panen.id} | KebunID: ${panen.kebunId} | Tanggal: ${panen.tanggalPanen} | Berat: ${panen.getTotalBerat()} Kg")
                        } else {
                            Log.w(TAG, "   [$index] NULL panen for key: ${childSnapshot.key}")
                        }

                        index++
                    }

                    cachedPanenList = panenList
                    Log.d(TAG, "")
                    Log.d(TAG, "📊 LOADED ${panenList.size} total panen from Firebase")
                    Log.d(TAG, "═══════════════════════════════════════")

                    onComplete(panenList)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ ERROR parsing panen data", e)
                    Log.d(TAG, "═══════════════════════════════════════")
                    onComplete(cachedPanenList.ifEmpty { null })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "❌ ERROR loading panen: ${error.message}")
                Log.d(TAG, "═══════════════════════════════════════")
                onComplete(if (cachedPanenList.isNotEmpty()) cachedPanenList else null)
            }
        })
    }

    /**
     * Ambil data panen berdasarkan kebun ID
     */
    fun getPanenByKebunId(kebunId: Int, onComplete: (List<PanenData>) -> Unit) {
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "🔍 QUERYING PANEN BY KEBUN ID")
        Log.d(TAG, "   Target Kebun ID: $kebunId")
        Log.d(TAG, "   Query: orderByChild('kebunId').equalTo($kebunId)")

        panenRef.orderByChild("kebunId").equalTo(kebunId.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "")
                    Log.d(TAG, "📡 FIREBASE RESPONSE:")
                    Log.d(TAG, "   Snapshot exists: ${snapshot.exists()}")
                    Log.d(TAG, "   Children count: ${snapshot.childrenCount}")

                    val panenList = mutableListOf<PanenData>()

                    try {
                        var index = 0
                        for (childSnapshot in snapshot.children) {
                            Log.d(TAG, "")
                            Log.d(TAG, "   Processing child [$index]:")
                            Log.d(TAG, "      Firebase Key: ${childSnapshot.key}")

                            val panen = childSnapshot.getValue(PanenData::class.java)

                            if (panen != null) {
                                Log.d(TAG, "      ✅ Panen ID: ${panen.id}")
                                Log.d(TAG, "      ✅ Kebun ID: ${panen.kebunId}")
                                Log.d(TAG, "      ✅ Tanggal: ${panen.tanggalPanen}")
                                Log.d(TAG, "      ✅ Total Berat: ${panen.getTotalBerat()} Kg")

                                panenList.add(panen)
                                childSnapshot.key?.let { key ->
                                    panenKeyMap[panen.id] = key
                                }
                            } else {
                                Log.e(TAG, "      ❌ NULL panen object!")
                            }

                            index++
                        }

                        // Sort by timestamp descending (terbaru dulu)
                        panenList.sortByDescending { it.timestamp }

                        Log.d(TAG, "")
                        Log.d(TAG, "📊 QUERY RESULT:")
                        Log.d(TAG, "   Found ${panenList.size} panen for kebun ID: $kebunId")
                        Log.d(TAG, "═══════════════════════════════════════")

                        onComplete(panenList)
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ ERROR getting panen by kebun ID", e)
                        Log.d(TAG, "═══════════════════════════════════════")
                        onComplete(emptyList())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "❌ DATABASE ERROR: ${error.message}")
                    Log.d(TAG, "═══════════════════════════════════════")
                    onComplete(emptyList())
                }
            })
    }

    /**
     * Listen real-time changes untuk kebun tertentu
     */
    fun listenToPanenByKebunId(kebunId: Int, onChange: (List<PanenData>) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val panenList = mutableListOf<PanenData>()

                try {
                    for (childSnapshot in snapshot.children) {
                        val panen = childSnapshot.getValue(PanenData::class.java)
                        if (panen != null && panen.kebunId == kebunId) {
                            panenList.add(panen)
                            childSnapshot.key?.let { key ->
                                panenKeyMap[panen.id] = key
                            }
                        }
                    }

                    // Sort by timestamp descending
                    panenList.sortByDescending { it.timestamp }

                    Log.d(TAG, "Real-time update: ${panenList.size} panen for kebun $kebunId")
                    onChange(panenList)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in real-time listener", e)
                    onChange(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Real-time listener cancelled: ${error.message}")
                onChange(emptyList())
            }
        }

        panenRef.addValueEventListener(listener)
        return listener
    }

    /**
     * Remove listener
     */
    fun removeListener(listener: ValueEventListener) {
        panenRef.removeEventListener(listener)
        Log.d(TAG, "Listener removed")
    }

    /**
     * Ambil panen berdasarkan ID
     */
    fun getPanenById(id: Int, onComplete: (PanenData?) -> Unit) {
        // Cek di cache dulu
        val cachedPanen = cachedPanenList.find { it.id == id }
        if (cachedPanen != null) {
            onComplete(cachedPanen)
            return
        }

        // Jika tidak ada di cache, ambil dari Firebase
        panenRef.orderByChild("id").equalTo(id.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val panen = snapshot.children.firstOrNull()?.getValue(PanenData::class.java)
                    onComplete(panen)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Error getting panen by ID: ${error.message}")
                    onComplete(null)
                }
            })
    }

    /**
     * Update data panen
     */
    fun updatePanen(panenData: PanenData, onComplete: (Boolean, String?) -> Unit) {
        val firebaseKey = panenKeyMap[panenData.id]

        if (firebaseKey != null) {
            // Update menggunakan Firebase key yang sudah disimpan
            panenRef.child(firebaseKey).setValue(panenData)
                .addOnSuccessListener {
                    // Update cache
                    val index = cachedPanenList.indexOfFirst { it.id == panenData.id }
                    if (index != -1) {
                        cachedPanenList[index] = panenData
                    }
                    Log.d(TAG, "Panen updated successfully")
                    onComplete(true, null)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error updating panen", exception)
                    onComplete(false, exception.message)
                }
        } else {
            // Fallback: cari menggunakan query
            panenRef.orderByChild("id").equalTo(panenData.id.toDouble())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val childSnapshot = snapshot.children.firstOrNull()

                        if (childSnapshot != null) {
                            childSnapshot.ref.setValue(panenData)
                                .addOnSuccessListener {
                                    childSnapshot.key?.let { key ->
                                        panenKeyMap[panenData.id] = key
                                    }
                                    onComplete(true, null)
                                }
                                .addOnFailureListener { exception ->
                                    onComplete(false, exception.message)
                                }
                        } else {
                            onComplete(false, "Panen not found")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        onComplete(false, error.message)
                    }
                })
        }
    }

    /**
     * Hapus data panen
     */
    fun deletePanen(id: Int, onComplete: (Boolean, String?) -> Unit) {
        val firebaseKey = panenKeyMap[id]

        if (firebaseKey != null) {
            // Hapus menggunakan Firebase key yang sudah disimpan
            panenRef.child(firebaseKey).removeValue()
                .addOnSuccessListener {
                    cachedPanenList.removeIf { it.id == id }
                    panenKeyMap.remove(id)
                    Log.d(TAG, "Panen deleted successfully")
                    onComplete(true, null)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error deleting panen", exception)
                    onComplete(false, exception.message)
                }
        } else {
            // Fallback: cari dan hapus menggunakan query
            panenRef.orderByChild("id").equalTo(id.toDouble())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val childSnapshot = snapshot.children.firstOrNull()

                        if (childSnapshot != null) {
                            childSnapshot.ref.removeValue()
                                .addOnSuccessListener {
                                    cachedPanenList.removeIf { it.id == id }
                                    panenKeyMap.remove(id)
                                    Log.d(TAG, "Panen deleted successfully (fallback)")
                                    onComplete(true, null)
                                }
                                .addOnFailureListener { exception ->
                                    Log.e(TAG, "Error deleting panen (fallback)", exception)
                                    onComplete(false, exception.message)
                                }
                        } else {
                            Log.w(TAG, "Panen not found for deletion")
                            onComplete(false, "Panen not found")
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
     * Hapus semua panen dari kebun tertentu
     */
    fun deletePanenByKebunId(kebunId: Int, onComplete: (Boolean, String?) -> Unit) {
        panenRef.orderByChild("kebunId").equalTo(kebunId.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var deletedCount = 0
                    var totalCount = snapshot.childrenCount.toInt()

                    if (totalCount == 0) {
                        onComplete(true, null)
                        return
                    }

                    for (childSnapshot in snapshot.children) {
                        childSnapshot.ref.removeValue()
                            .addOnSuccessListener {
                                deletedCount++
                                if (deletedCount == totalCount) {
                                    // Bersihkan cache
                                    cachedPanenList.removeIf { it.kebunId == kebunId }
                                    Log.d(TAG, "All panen for kebun $kebunId deleted")
                                    onComplete(true, null)
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e(TAG, "Error deleting panen item", exception)
                                onComplete(false, exception.message)
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    onComplete(false, error.message)
                }
            })
    }

    /**
     * Hitung total panen untuk kebun tertentu
     */
    fun getTotalBeratByKebunId(kebunId: Int, onComplete: (Double) -> Unit) {
        getPanenByKebunId(kebunId) { panenList ->
            val totalBerat = panenList.sumOf { it.getTotalBerat() }
            onComplete(totalBerat)
        }
    }

    /**
     * Hitung total pendapatan untuk kebun tertentu
     */
    fun getTotalPendapatanByKebunId(kebunId: Int, onComplete: (Double) -> Unit) {
        getPanenByKebunId(kebunId) { panenList ->
            val totalPendapatan = panenList.sumOf { it.getTotalPendapatan() }
            onComplete(totalPendapatan)
        }
    }

    /**
     * Get statistik panen berdasarkan bulan
     */
    fun getStatistikByBulan(kebunId: Int, onComplete: (Map<String, Double>) -> Unit) {
        getPanenByKebunId(kebunId) { panenList ->
            val statistikMap = panenList
                .groupBy { it.getBulan() }
                .mapValues { entry -> entry.value.sumOf { it.getTotalBerat() } }

            onComplete(statistikMap)
        }
    }

    /**
     * Get cached data (untuk offline mode)
     */
    fun getCachedPanen(): List<PanenData> {
        return cachedPanenList.toList()
    }

    /**
     * Cek apakah ada data panen untuk kebun tertentu
     */
    fun hasPanenForKebun(kebunId: Int): Boolean {
        return cachedPanenList.any { it.kebunId == kebunId }
    }

    /**
     * Get jumlah panen untuk kebun tertentu
     */
    fun getPanenCountByKebunId(kebunId: Int): Int {
        return cachedPanenList.count { it.kebunId == kebunId }
    }
}