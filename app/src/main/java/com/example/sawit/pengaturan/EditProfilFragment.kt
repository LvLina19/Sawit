package com.example.sawit.pengaturan

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.sawit.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class EditProfilFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private val userId by lazy { auth.currentUser?.uid ?: "" }

    // Views
    private lateinit var btnBack: ImageView
    private lateinit var ivProfilePicture: ImageView
    private lateinit var tvChangePhoto: TextView
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var btnSave: Button
    private lateinit var progressBar: ProgressBar

    // Image
    private var selectedImageUri: Uri? = null
    private var currentProfileUrl: String? = null

    // ImgBB API Key - GANTI DENGAN API KEY KAMU
    private val IMGBB_API_KEY = "90fbc1f39611ae72363916cb201bbf3b"
    private val IMGBB_UPLOAD_URL = "https://api.imgbb.com/1/upload"

    // Coroutine scope
    private val fragmentScope = CoroutineScope(Dispatchers.Main + Job())

    // Activity Result Launchers
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                displaySelectedImage(uri)
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            imageBitmap?.let {
                selectedImageUri = getImageUriFromBitmap(it)
                ivProfilePicture.setImageBitmap(it)
            }
        }
    }

    private val multiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }

        if (allGranted) {
            showImagePickerDialog()
        } else {
            val deniedPermissions = permissions.filterValues { !it }.keys

            if (deniedPermissions.any { shouldShowRequestPermissionRationale(it) }) {
                showPermissionRationaleDialog()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Izin diperlukan untuk mengubah foto profil. Silakan aktifkan di Settings.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private val singlePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showImagePickerDialog()
        } else {
            showPermissionRationaleDialog()
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

        progressBar = ProgressBar(requireContext()).apply {
            visibility = View.GONE
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        tvChangePhoto.setOnClickListener {
            checkPermissionAndPickImage()
        }

        ivProfilePicture.setOnClickListener {
            checkPermissionAndPickImage()
        }

        btnSave.setOnClickListener {
            saveProfile()
        }
    }

    private fun loadUserProfile() {
        showLoading(true)

        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                showLoading(false)
                if (document.exists()) {
                    etName.setText(document.getString("name") ?: "")
                    etEmail.setText(document.getString("email") ?: "")
                    etPhone.setText(document.getString("phone") ?: "")

                    // Load profile picture dari kolom "profile"
                    currentProfileUrl = document.getString("profile")
                    Log.d("EditProfil", "Profile URL from Firestore: $currentProfileUrl")

                    if (!currentProfileUrl.isNullOrEmpty()) {
                        loadProfileImage(currentProfileUrl!!)
                    } else {
                        // Set placeholder jika tidak ada foto profil
                        ivProfilePicture.setImageResource(R.drawable.ic_profile_placeholder)
                    }
                }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Log.e("EditProfil", "Error loading profile: ${e.message}")
                Toast.makeText(requireContext(), "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadProfileImage(url: String) {
        try {
            Log.d("EditProfil", "Loading image from URL: $url")
            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .into(ivProfilePicture)
        } catch (e: Exception) {
            Log.e("EditProfil", "Error loading image: ${e.message}")
            ivProfilePicture.setImageResource(R.drawable.ic_profile_placeholder)
        }
    }

    private fun checkPermissionAndPickImage() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                checkAndRequestPermissions(
                    arrayOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.CAMERA
                    )
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                checkAndRequestPermissions(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                    )
                )
            }
            else -> {
                showImagePickerDialog()
            }
        }
    }

    private fun checkAndRequestPermissions(permissions: Array<String>) {
        val permissionsToRequest = mutableListOf<String>()

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission)
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission)
            }
        }

        when {
            permissionsToRequest.isEmpty() -> {
                showImagePickerDialog()
            }
            permissionsToRequest.size == 1 -> {
                singlePermissionLauncher.launch(permissionsToRequest[0])
            }
            else -> {
                multiplePermissionsLauncher.launch(permissionsToRequest.toTypedArray())
            }
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Izin Diperlukan")
            .setMessage("Aplikasi memerlukan izin akses galeri dan kamera untuk mengubah foto profil Anda.")
            .setPositiveButton("Coba Lagi") { _, _ ->
                checkPermissionAndPickImage()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Ambil dari Galeri", "Ambil Foto", "Batal")

        AlertDialog.Builder(requireContext())
            .setTitle("Pilih Foto Profil")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> openGallery()
                    1 -> openCamera()
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun openGallery() {
        try {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            galleryLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Gagal membuka galeri: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openCamera() {
        try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                cameraLauncher.launch(intent)
            } else {
                Toast.makeText(requireContext(), "Tidak ada aplikasi kamera tersedia", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Gagal membuka kamera: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displaySelectedImage(uri: Uri) {
        try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = android.graphics.ImageDecoder.createSource(requireContext().contentResolver, uri)
                android.graphics.ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            }
            ivProfilePicture.setImageBitmap(bitmap)
        } catch (e: IOException) {
            Log.e("EditProfil", "Error displaying image: ${e.message}")
            Toast.makeText(requireContext(), "Error menampilkan gambar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            requireContext().contentResolver,
            bitmap,
            "Profile_${System.currentTimeMillis()}",
            null
        )
        return Uri.parse(path)
    }

    private fun saveProfile() {
        val name = etName.text.toString().trim()
        val phone = etPhone.text.toString().trim()

        // Validation
        if (name.isEmpty()) {
            etName.error = "Nama tidak boleh kosong"
            etName.requestFocus()
            return
        }

        if (phone.isNotEmpty() && !isValidPhoneNumber(phone)) {
            etPhone.error = "Nomor telepon tidak valid"
            etPhone.requestFocus()
            return
        }

        showLoading(true)

        // Check if new image is selected
        if (selectedImageUri != null) {
            // Upload gambar baru ke ImgBB
            Toast.makeText(requireContext(), "Mengupload gambar...", Toast.LENGTH_SHORT).show()
            uploadImageToImgBB(selectedImageUri!!) { imageUrl ->
                if (imageUrl != null) {
                    // Upload berhasil, update profile dengan URL baru
                    updateProfileData(name, phone, imageUrl)
                } else {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Gagal upload gambar ke ImgBB", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            // Tidak ada gambar baru, update profile tanpa mengubah foto
            updateProfileData(name, phone, currentProfileUrl)
        }
    }

    private fun uploadImageToImgBB(imageUri: Uri, callback: (String?) -> Unit) {
        fragmentScope.launch(Dispatchers.IO) {
            try {
                Log.d("ImgBB", "Starting upload process...")

                // Convert image to bitmap
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = android.graphics.ImageDecoder.createSource(requireContext().contentResolver, imageUri)
                    android.graphics.ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
                }

                Log.d("ImgBB", "Bitmap loaded: ${bitmap.width}x${bitmap.height}")

                // Resize bitmap jika terlalu besar (max 32MB untuk ImgBB)
                val resizedBitmap = resizeBitmap(bitmap, 1920, 1920)
                val base64Image = bitmapToBase64(resizedBitmap)

                Log.d("ImgBB", "Base64 size: ${base64Image.length} characters")
                Log.d("ImgBB", "Uploading to ImgBB...")

                // Upload to ImgBB
                val url = URL(IMGBB_UPLOAD_URL)
                val connection = url.openConnection() as HttpURLConnection

                connection.apply {
                    requestMethod = "POST"
                    doOutput = true
                    connectTimeout = 30000
                    readTimeout = 30000
                    setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                }

                // Create POST data dengan URL encoding
                val postData = "key=$IMGBB_API_KEY&image=${java.net.URLEncoder.encode(base64Image, "UTF-8")}"

                connection.outputStream.use { os ->
                    os.write(postData.toByteArray())
                    os.flush()
                }

                // Read response
                val responseCode = connection.responseCode
                Log.d("ImgBB", "Response code: $responseCode")

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("ImgBB", "Response: $response")

                    val jsonResponse = JSONObject(response)

                    if (jsonResponse.getBoolean("success")) {
                        val imageUrl = jsonResponse.getJSONObject("data").getString("url")
                        Log.d("ImgBB", "Upload success! URL: $imageUrl")

                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Upload berhasil!", Toast.LENGTH_SHORT).show()
                            callback(imageUrl)
                        }
                    } else {
                        val errorMsg = jsonResponse.optJSONObject("error")?.optString("message") ?: "Unknown error"
                        Log.e("ImgBB", "Upload failed: $errorMsg")

                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "ImgBB Error: $errorMsg", Toast.LENGTH_LONG).show()
                            callback(null)
                        }
                    }
                } else {
                    val errorResponse = try {
                        connection.errorStream?.bufferedReader()?.use { it.readText() }
                    } catch (e: Exception) {
                        "No error details"
                    }

                    Log.e("ImgBB", "Upload failed with code: $responseCode")
                    Log.e("ImgBB", "Error response: $errorResponse")

                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Upload gagal: HTTP $responseCode", Toast.LENGTH_LONG).show()
                        callback(null)
                    }
                }

                connection.disconnect()

            } catch (e: Exception) {
                Log.e("ImgBB", "Upload exception: ${e.message}", e)
                e.printStackTrace()

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    callback(null)
                }
            }
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }

        val ratio = minOf(maxWidth.toFloat() / width, maxHeight.toFloat() / height)
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun updateProfileData(name: String, phone: String, profilePictureUrl: String?) {
        val profileData = hashMapOf<String, Any>(
            "name" to name,
            "phone" to phone,
            "updatedAt" to System.currentTimeMillis()
        )

        // Simpan URL gambar ke kolom "profile"
        profilePictureUrl?.let {
            profileData["profile"] = it
            Log.d("EditProfil", "Saving profile URL: $it")
        }

        firestore.collection("users")
            .document(userId)
            .update(profileData)
            .addOnSuccessListener {
                showLoading(false)
                Log.d("EditProfil", "Profile updated successfully")
                Toast.makeText(requireContext(), "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()

                // Update currentProfileUrl
                currentProfileUrl = profilePictureUrl

                // Reset selected image
                selectedImageUri = null

                // Kembali ke halaman sebelumnya
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Log.e("EditProfil", "Error updating profile: ${e.message}")
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        val phoneRegex = "^(\\+62|62|0)[0-9]{9,12}$".toRegex()
        return phoneRegex.matches(phone)
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            btnSave.isEnabled = false
            btnSave.text = "Menyimpan..."
            btnSave.alpha = 0.5f
        } else {
            btnSave.isEnabled = true
            btnSave.text = "Simpan Perubahan"
            btnSave.alpha = 1f
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentScope.cancel()
    }
}