package com.example.sawit.model

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.sawit.api.ImgBBUploader
import com.example.sawit.data.model.RiwayatDeteksiModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class RiwayatDeteksiRepository(private val context: Context) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val riwayatCollection = firestore.collection("riwayat_deteksi")
    private val imgBBUploader = ImgBBUploader()

    companion object {
        private const val TAG = "RiwayatRepository"
    }

    /**
     * Dapatkan user ID yang sedang login
     */
    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * Simpan hasil deteksi ke Firestore dengan upload gambar ke ImgBB
     */
    suspend fun saveDeteksi(
        bitmap: Bitmap,
        jenisBuah: String,
        lokasi: String,
        kepercayaan: Int,
        area: Double = 0.0
    ): Result<String> {
        return try {
            // Cek apakah user sudah login
            val userId = getCurrentUserId()
            if (userId == null) {
                Log.e(TAG, "User belum login")
                return Result.failure(Exception("User belum login. Silakan login terlebih dahulu."))
            }

            Log.d(TAG, "Mulai menyimpan deteksi untuk user: $userId")

            // 1. Upload gambar ke ImgBB
            Log.d(TAG, "Mengupload gambar ke ImgBB...")
            val uploadResult = imgBBUploader.uploadImage(bitmap)

            val imageUrl = uploadResult.getOrElse { error ->
                Log.e(TAG, "Gagal upload gambar ke ImgBB", error)
                return Result.failure(Exception("Gagal upload gambar: ${error.message}"))
            }

            Log.d(TAG, "Gambar berhasil diupload: $imageUrl")

            // 2. Buat data riwayat dengan user ID yang login
            val riwayat = RiwayatDeteksiModel(
                imageUrl = imageUrl,
                jenisBuah = jenisBuah,
                lokasi = lokasi,
                tanggal = Timestamp.now(),
                kepercayaan = kepercayaan,
                area = area,
                userId = userId
            )

            // 3. Simpan ke Firestore
            Log.d(TAG, "Menyimpan data ke Firestore...")
            val docRef = riwayatCollection.add(riwayat).await()
            Log.d(TAG, "Data berhasil disimpan dengan ID: ${docRef.id}")

            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Gagal menyimpan deteksi", e)
            Result.failure(e)
        }
    }

    /**
     * Ambil semua riwayat deteksi milik user yang login
     */
    suspend fun getAllRiwayat(): Result<List<RiwayatDeteksiModel>> {
        return try {
            val userId = getCurrentUserId()
            if (userId == null) {
                Log.e(TAG, "User belum login")
                return Result.failure(Exception("User belum login"))
            }

            val snapshot = riwayatCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val riwayatList = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(RiwayatDeteksiModel::class.java)?.copy(
                        id = doc.id
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing document: ${doc.id}", e)
                    null
                }
            }
                // Sort di client side
                .sortedByDescending { it.tanggal }

            Log.d(TAG, "Berhasil mengambil ${riwayatList.size} riwayat untuk user: $userId")
            Result.success(riwayatList)
        } catch (e: Exception) {
            Log.e(TAG, "Gagal mengambil riwayat", e)
            Result.failure(e)
        }
    }
    /**
     * Hapus riwayat berdasarkan ID (hanya jika milik user yang login)
     */
    suspend fun deleteRiwayat(riwayatId: String): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            if (userId == null) {
                Log.e(TAG, "User belum login")
                return Result.failure(Exception("User belum login"))
            }

            // Cek apakah riwayat milik user yang login
            val doc = riwayatCollection.document(riwayatId).get().await()
            val riwayat = doc.toObject(RiwayatDeteksiModel::class.java)

            if (riwayat?.userId != userId) {
                Log.e(TAG, "Tidak dapat menghapus riwayat milik user lain")
                return Result.failure(Exception("Anda tidak memiliki akses untuk menghapus riwayat ini"))
            }

            // Hapus document dari Firestore
            riwayatCollection.document(riwayatId).delete().await()

            Log.d(TAG, "Riwayat berhasil dihapus: $riwayatId")
            Log.d(TAG, "Catatan: Gambar di ImgBB tidak dihapus otomatis (keterbatasan API gratis)")

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Gagal menghapus riwayat", e)
            Result.failure(e)
        }
    }

    /**
     * Cari riwayat berdasarkan query (hanya milik user yang login)
     */
    suspend fun searchRiwayat(query: String): Result<List<RiwayatDeteksiModel>> {
        return try {
            val userId = getCurrentUserId()
            if (userId == null) {
                Log.e(TAG, "User belum login")
                return Result.failure(Exception("User belum login"))
            }

            val snapshot = riwayatCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val allRiwayat = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(RiwayatDeteksiModel::class.java)?.copy(
                        id = doc.id
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing document: ${doc.id}", e)
                    null
                }
            }
                // Sort di client side
                .sortedByDescending { it.tanggal }

            // Filter di client side
            val filteredList = allRiwayat.filter { riwayat ->
                riwayat.jenisBuah.contains(query, ignoreCase = true) ||
                        riwayat.lokasi.contains(query, ignoreCase = true)
            }

            Log.d(TAG, "Hasil pencarian '${query}': ${filteredList.size} item untuk user: $userId")
            Result.success(filteredList)
        } catch (e: Exception) {
            Log.e(TAG, "Gagal mencari riwayat", e)
            Result.failure(e)
        }
    }
    /**
     * Cek apakah user sudah login
     */
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}