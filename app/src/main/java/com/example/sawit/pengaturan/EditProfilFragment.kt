package com.example.sawit.pengaturan

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.sawit.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException
import okhttp3.*
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class EditProfilFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private val userId by lazy { auth.currentUser?.uid ?: "" }

    private lateinit var btnBack: ImageView
    private lateinit var ivProfilePicture: ImageView
    private lateinit var tvChangePhoto: TextView
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var btnSave: Button

    private var selectedImageUri: Uri? = null
    private var currentImageUrl: String? = null

    private val IMGBB_API_KEY = "75569b59e5eec094e712ea7c3b27c6ea"

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            ivProfilePicture.setImageURI(selectedImageUri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profil, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        initViews(view)
        setupClickListeners()
        loadUserProfile()

        return view
    }

    private fun initViews(view: View) {
        btnBack = view.findViewById(R.id.btnBack)
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture)
        tvChangePhoto = view.findViewById(R.id.tvChangePhoto)
        etName = view.findViewById(R.id.etName)
        etEmail = view.findViewById(R.id.etEmail)
        etPhone = view.findViewById(R.id.etPhone)
        btnSave = view.findViewById(R.id.btnSave)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        ivProfilePicture.setOnClickListener {
            openImagePicker()
        }

        tvChangePhoto.setOnClickListener {
            openImagePicker()
        }

        btnSave.setOnClickListener {
            saveProfile()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun loadUserProfile() {
        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    etName.setText(document.getString("name"))
                    etEmail.setText(document.getString("email"))
                    etPhone.setText(document.getString("phone"))

                    // Load gambar profil jika ada
                    currentImageUrl = document.getString("profileImage")
                    if (!currentImageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(currentImageUrl)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .error(R.drawable.ic_profile_placeholder)
                            .into(ivProfilePicture)
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Gagal memuat profil: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveProfile() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading
        btnSave.isEnabled = false
        Toast.makeText(requireContext(), "Menyimpan...", Toast.LENGTH_SHORT).show()

        // Jika ada gambar baru dipilih, upload dulu
        if (selectedImageUri != null) {
            uploadImageToImgBB(selectedImageUri!!) { imageUrl ->
                updateProfileData(name, email, phone, imageUrl)
            }
        } else {
            // Langsung save tanpa upload gambar baru
            updateProfileData(name, email, phone, currentImageUrl)
        }
    }

    private fun uploadImageToImgBB(uri: Uri, onSuccess: (String) -> Unit) {
        try {
            Toast.makeText(requireContext(), "Mengupload gambar...", Toast.LENGTH_SHORT).show()

            // Compress dan simpan ke file temporary
            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)

            val maxSize = 1024
            val ratio = Math.min(
                maxSize.toFloat() / bitmap.width,
                maxSize.toFloat() / bitmap.height
            )

            val resizedBitmap = if (ratio < 1) {
                Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * ratio).toInt(),
                    (bitmap.height * ratio).toInt(),
                    true
                )
            } else {
                bitmap
            }

            // Save to temporary file
            val tempFile = File(requireContext().cacheDir, "temp_upload_${System.currentTimeMillis()}.jpg")
            val outputStream = java.io.FileOutputStream(tempFile)
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            outputStream.flush()
            outputStream.close()

            android.util.Log.d("ImgBB", "Temp file size: ${tempFile.length() / 1024} KB")

            // Upload file langsung (bukan Base64)
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "image",
                    tempFile.name,
                    tempFile.asRequestBody("image/jpeg".toMediaType())
                )
                .build()

            val request = Request.Builder()
                .url("https://api.imgbb.com/1/upload?key=$IMGBB_API_KEY")
                .post(requestBody)
                .build()

            android.util.Log.d("ImgBB", "Sending file upload request...")

            val client = OkHttpClient.Builder()
                .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    android.util.Log.e("ImgBB", "Upload failed: ${e.message}")
                    e.printStackTrace()

                    // Hapus temp file
                    tempFile.delete()

                    activity?.runOnUiThread {
                        btnSave.isEnabled = true
                        Toast.makeText(requireContext(), "Upload gagal: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val responseBody = response.body?.string()
                        android.util.Log.d("ImgBB", "Response code: ${response.code}")
                        android.util.Log.d("ImgBB", "Response body: $responseBody")

                        // Hapus temp file
                        tempFile.delete()

                        val json = JSONObject(responseBody ?: "")

                        if (json.optBoolean("success", false)) {
                            val imageUrl = json.getJSONObject("data").getString("url")
                            android.util.Log.d("ImgBB", "✅ Upload success! URL: $imageUrl")

                            activity?.runOnUiThread {
                                Toast.makeText(requireContext(), "Upload gambar berhasil!", Toast.LENGTH_SHORT).show()
                                onSuccess(imageUrl)
                            }
                        } else {
                            val errorMsg = if (json.has("error")) {
                                json.getJSONObject("error").optString("message", "Unknown error")
                            } else {
                                "Upload failed with code ${response.code}"
                            }
                            android.util.Log.e("ImgBB", "❌ Upload failed: $errorMsg")

                            activity?.runOnUiThread {
                                btnSave.isEnabled = true
                                Toast.makeText(requireContext(), "Upload gagal: $errorMsg", Toast.LENGTH_LONG).show()
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ImgBB", "Parse error: ${e.message}")
                        e.printStackTrace()

                        activity?.runOnUiThread {
                            btnSave.isEnabled = true
                            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            })

        } catch (e: Exception) {
            android.util.Log.e("ImgBB", "Exception: ${e.message}")
            e.printStackTrace()
            btnSave.isEnabled = true
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateProfileData(name: String, email: String, phone: String, imageUrl: String?) {
        val profileData = hashMapOf(
            "name" to name,
            "email" to email,
            "phone" to phone,
            "updatedAt" to System.currentTimeMillis()
        )

        // Tambahkan profileImage jika ada
        if (!imageUrl.isNullOrEmpty()) {
            profileData["profileImage"] = imageUrl
        }

        firestore.collection("users")
            .document(userId)
            .update(profileData as Map<String, Any>)
            .addOnSuccessListener {
                btnSave.isEnabled = true
                Toast.makeText(requireContext(), "Profil berhasil diupdate", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                btnSave.isEnabled = true
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}