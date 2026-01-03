package com.example.sawit.api

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class ImgBBUploader {

    companion object {
        private const val TAG = "ImgBBUploader"
        private const val IMGBB_API_KEY = "75569b59e5eec094e712ea7c3b27c6ea" // Ganti dengan API key dari https://api.imgbb.com/
        private const val IMGBB_UPLOAD_URL = "https://api.imgbb.com/1/upload"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun uploadImage(bitmap: Bitmap): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Mulai upload ke ImgBB...")

            // Convert bitmap ke Base64
            val base64Image = bitmapToBase64(bitmap)

            // Buat request body
            val formBody = FormBody.Builder()
                .add("key", IMGBB_API_KEY)
                .add("image", base64Image)
                .build()

            // Buat request
            val request = Request.Builder()
                .url(IMGBB_UPLOAD_URL)
                .post(formBody)
                .build()

            // Execute request
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            Log.d(TAG, "Response code: ${response.code}")

            if (response.isSuccessful && responseBody != null) {
                val jsonResponse = JSONObject(responseBody)

                if (jsonResponse.getBoolean("success")) {
                    val imageUrl = jsonResponse.getJSONObject("data")
                        .getString("url")

                    Log.d(TAG, "Upload berhasil: $imageUrl")
                    Result.success(imageUrl)
                } else {
                    val error = jsonResponse.optJSONObject("error")?.optString("message")
                        ?: "Upload gagal"
                    Log.e(TAG, "Upload gagal: $error")
                    Result.failure(Exception(error))
                }
            } else {
                val error = "HTTP Error: ${response.code} - $responseBody"
                Log.e(TAG, error)
                Result.failure(Exception(error))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Exception saat upload", e)
            Result.failure(e)
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        // Compress untuk memperkecil ukuran (ImgBB gratis max 32MB)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos)
        val imageBytes = baos.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP)
    }
}