package com.example.sawit.model

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.sawit.api.ImgBBUploader
import com.example.sawit.data.model.RiwayatDeteksiModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class RiwayatDeteksiRepository(private val context: Context) {

    private val firestore = FirebaseFirestore.getInstance()
    private val riwayatCollection = firestore.collection("riwayat_deteksi")
    private val imgBBUploader = ImgBBUploader()

    companion object {
        private const val TAG = "RiwayatRepository"
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
            Log.d(TAG, "Mulai menyimpan deteksi...")

            // 1. Upload gambar ke ImgBB (bukan Firebase Storage)
            Log.d(TAG, "Mengupload gambar ke ImgBB...")
            val uploadResult = imgBBUploader.uploadImage(bitmap)

            val imageUrl = uploadResult.getOrElse { error ->
                Log.e(TAG, "Gagal upload gambar ke ImgBB", error)
                return Result.failure(Exception("Gagal upload gambar: ${error.message}"))
            }

            Log.d(TAG, "Gambar berhasil diupload: $imageUrl")

            // 2. Buat data riwayat
            val riwayat = RiwayatDeteksiModel(
                imageUrl = imageUrl,
                jenisBuah = jenisBuah,
                lokasi = lokasi,
                tanggal = Timestamp.now(),
                kepercayaan = kepercayaan,
                area = area,
                userId = "default_user" // Ganti dengan user ID jika ada autentikasi
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
     * Ambil semua riwayat deteksi
     */
    suspend fun getAllRiwayat(): Result<List<RiwayatDeteksiModel>> {
        return try {
            val snapshot = riwayatCollection
                .orderBy("tanggal", Query.Direction.DESCENDING)
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

            Log.d(TAG, "Berhasil mengambil ${riwayatList.size} riwayat")
            Result.success(riwayatList)
        } catch (e: Exception) {
            Log.e(TAG, "Gagal mengambil riwayat", e)
            Result.failure(e)
        }
    }

    /**
     * Hapus riwayat berdasarkan ID
     * Catatan: Gambar di ImgBB tidak bisa dihapus via API (hanya bisa manual di dashboard)
     */
    suspend fun deleteRiwayat(riwayatId: String): Result<Unit> {
        return try {
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
     * Cari riwayat berdasarkan query
     */
    suspend fun searchRiwayat(query: String): Result<List<RiwayatDeteksiModel>> {
        return try {
            val snapshot = riwayatCollection
                .orderBy("tanggal", Query.Direction.DESCENDING)
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

            // Filter di client side karena Firestore tidak support full-text search
            val filteredList = allRiwayat.filter { riwayat ->
                riwayat.jenisBuah.contains(query, ignoreCase = true) ||
                        riwayat.lokasi.contains(query, ignoreCase = true)
            }

            Log.d(TAG, "Hasil pencarian '${query}': ${filteredList.size} item")
            Result.success(filteredList)
        } catch (e: Exception) {
            Log.e(TAG, "Gagal mencari riwayat", e)
            Result.failure(e)
        }
    }
}