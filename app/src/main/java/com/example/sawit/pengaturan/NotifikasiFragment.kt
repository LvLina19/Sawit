package com.example.sawit.pengaturan

import android.Manifest
import android.app.AlarmManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sawit.R
import com.example.sawit.adapter.NotificationScheduleAdapter
import com.example.sawit.model.NotificationSchedule
import com.example.sawit.model.UserSettings
import com.example.sawit.utils.AlarmScheduler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class NotifikasiFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var alarmScheduler: AlarmScheduler
    private lateinit var adapter: NotificationScheduleAdapter

    private lateinit var btnBack: ImageView
    private lateinit var switchNotifications: SwitchCompat
    private lateinit var switchSound: SwitchCompat
    private lateinit var switchVibration: SwitchCompat
    private lateinit var btnAddSchedule: Button
    private lateinit var rvSchedules: RecyclerView
    private lateinit var emptyState: LinearLayout

    private val userId by lazy { auth.currentUser?.uid ?: "" }
    private val notificationSchedules = mutableListOf<NotificationSchedule>()

    companion object {
        private const val NOTIFICATION_PERMISSION_CODE = 100
        private const val TAG = "NotifikasiFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notifikasi, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        alarmScheduler = AlarmScheduler(requireContext())

        initViews(view)
        setupUI()
        setupRecyclerView()
        loadUserSettings()
        loadNotificationSchedules()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Request notification permission untuk Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission()
        }

        // Check exact alarm permission untuk Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkExactAlarmPermission()
        }
    }

    private fun initViews(view: View) {
        btnBack = view.findViewById(R.id.btnBack)
        switchNotifications = view.findViewById(R.id.switchNotifications)
        switchSound = view.findViewById(R.id.switchSound)
        switchVibration = view.findViewById(R.id.switchVibration)
        btnAddSchedule = view.findViewById(R.id.btnAddSchedule)
        rvSchedules = view.findViewById(R.id.rvSchedules)
        emptyState = view.findViewById(R.id.emptyState)
    }

    private fun setupUI() {
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnAddSchedule.setOnClickListener {
            showAddScheduleDialog()
        }

        // Switch listeners
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            updateUserSettings("notificationsEnabled", isChecked)
        }

        switchSound.setOnCheckedChangeListener { _, isChecked ->
            updateUserSettings("soundEnabled", isChecked)
        }

        switchVibration.setOnCheckedChangeListener { _, isChecked ->
            updateUserSettings("vibrationEnabled", isChecked)
        }
    }

    private fun setupRecyclerView() {
        adapter = NotificationScheduleAdapter(
            schedules = notificationSchedules,
            onToggle = { schedule, isEnabled ->
                toggleSchedule(schedule.id, isEnabled)
            },
            onDelete = { schedule ->
                deleteSchedule(schedule)
            },
            onEdit = { schedule ->
                editSchedule(schedule)
            }
        )

        rvSchedules.layoutManager = LinearLayoutManager(requireContext())
        rvSchedules.adapter = adapter
    }

    private fun loadUserSettings() {
        firestore.collection("user_settings")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    document.toObject(UserSettings::class.java)?.let { settings ->
                        switchNotifications.isChecked = settings.notificationsEnabled
                        switchSound.isChecked = settings.soundEnabled
                        switchVibration.isChecked = settings.vibrationEnabled
                    }
                } else {
                    // Buat default settings jika belum ada
                    val defaultSettings = UserSettings(
                        userId = userId,
                        notificationsEnabled = true,
                        soundEnabled = true,
                        vibrationEnabled = true
                    )
                    firestore.collection("user_settings")
                        .document(userId)
                        .set(defaultSettings)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error loading user settings", e)
            }
    }

    private fun loadNotificationSchedules() {
        firestore.collection("notification_schedules")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e(TAG, "Error loading schedules", error)
                    Toast.makeText(
                        requireContext(),
                        "Error memuat jadwal: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addSnapshotListener
                }

                notificationSchedules.clear()

                snapshots?.documents?.forEach { doc ->
                    try {
                        doc.toObject(NotificationSchedule::class.java)?.let { schedule ->
                            notificationSchedules.add(schedule)
                            Log.d(TAG, "Loaded schedule: ${schedule.hour}:${schedule.minute}, type: ${schedule.type}, days: ${schedule.repeatDays}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing schedule document: ${doc.id}", e)
                    }
                }

                // Sort berdasarkan jam dan menit
                notificationSchedules.sortWith(compareBy({ it.hour }, { it.minute }))

                // Update adapter
                adapter.notifyDataSetChanged()
                updateEmptyState()

                Log.d(TAG, "Total schedules loaded: ${notificationSchedules.size}")
            }
    }

    private fun updateEmptyState() {
        if (notificationSchedules.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            rvSchedules.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            rvSchedules.visibility = View.VISIBLE
        }
    }

    private fun showAddScheduleDialog() {
        val calendar = Calendar.getInstance()

        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                showScheduleTypeDialog(hourOfDay, minute)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun showScheduleTypeDialog(hour: Int, minute: Int) {
        val types = arrayOf(
            "Pengingat Minum Air 💧",
            "Pengingat Olahraga 💪",
            "Pengingat Tidur 😴",
            "Pengingat Cek Kebun 🌴",
            "Pengingat Custom ✏️"
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Pilih Jenis Pengingat")
            .setItems(types) { _, which ->
                val type = when (which) {
                    0 -> "water"
                    1 -> "exercise"
                    2 -> "sleep"
                    3 -> "check_farm"
                    else -> "custom"
                }

                if (type == "custom") {
                    showCustomMessageDialog(hour, minute)
                } else {
                    showRepeatDaysDialog(hour, minute, type, "")
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showCustomMessageDialog(hour: Int, minute: Int) {
        val input = EditText(requireContext()).apply {
            hint = "Masukkan pesan pengingat"
            setPadding(50, 30, 50, 30)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Pesan Custom")
            .setView(input)
            .setPositiveButton("Lanjut") { _, _ ->
                val message = input.text.toString().trim()
                if (message.isNotEmpty()) {
                    showRepeatDaysDialog(hour, minute, "custom", message)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Pesan tidak boleh kosong",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showRepeatDaysDialog(hour: Int, minute: Int, type: String, customMessage: String) {
        val days = arrayOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
        val dayCodes = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val selectedDays = mutableListOf<String>()
        val checkedItems = BooleanArray(7) { true }

        // Semua hari aktif secara default
        selectedDays.addAll(dayCodes)

        AlertDialog.Builder(requireContext())
            .setTitle("Ulangi Pada Hari")
            .setMultiChoiceItems(days, checkedItems) { _, which, isChecked ->
                if (isChecked) {
                    if (!selectedDays.contains(dayCodes[which])) {
                        selectedDays.add(dayCodes[which])
                    }
                } else {
                    selectedDays.remove(dayCodes[which])
                }
            }
            .setPositiveButton("Simpan") { _, _ ->
                if (selectedDays.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "Pilih minimal 1 hari",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    saveNotificationSchedule(hour, minute, type, customMessage, selectedDays)
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun saveNotificationSchedule(
        hour: Int,
        minute: Int,
        type: String,
        customMessage: String,
        repeatDays: List<String>
    ) {
        val schedule = NotificationSchedule(
            id = UUID.randomUUID().toString(),
            userId = userId,
            hour = hour,
            minute = minute,
            type = type,
            customMessage = customMessage,
            isEnabled = true,
            repeatDays = repeatDays,
            createdAt = System.currentTimeMillis()
        )

        Log.d(TAG, "Saving schedule: ${schedule.hour}:${schedule.minute}, type: ${schedule.type}, days: ${schedule.repeatDays}")

        firestore.collection("notification_schedules")
            .document(schedule.id)
            .set(schedule)
            .addOnSuccessListener {
                Log.d(TAG, "Schedule saved successfully")
                Toast.makeText(
                    requireContext(),
                    "Jadwal berhasil ditambahkan",
                    Toast.LENGTH_SHORT
                ).show()

                // Schedule alarm
                alarmScheduler.scheduleAlarm(schedule)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error saving schedule", e)
                Toast.makeText(
                    requireContext(),
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun deleteSchedule(schedule: NotificationSchedule) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Jadwal")
            .setMessage("Apakah Anda yakin ingin menghapus jadwal ini?")
            .setPositiveButton("Ya") { _, _ ->
                firestore.collection("notification_schedules")
                    .document(schedule.id)
                    .delete()
                    .addOnSuccessListener {
                        Log.d(TAG, "Schedule deleted: ${schedule.id}")
                        Toast.makeText(
                            requireContext(),
                            "Jadwal berhasil dihapus",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Cancel alarm
                        alarmScheduler.cancelAlarm(schedule.id)
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error deleting schedule", e)
                        Toast.makeText(
                            requireContext(),
                            "Error: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun editSchedule(schedule: NotificationSchedule) {
        // TODO: Implement edit functionality
        Toast.makeText(
            requireContext(),
            "Fitur edit akan segera hadir",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun toggleSchedule(scheduleId: String, isEnabled: Boolean) {
        Log.d(TAG, "Toggle schedule: $scheduleId, enabled: $isEnabled")

        firestore.collection("notification_schedules")
            .document(scheduleId)
            .update("isEnabled", isEnabled)
            .addOnSuccessListener {
                val schedule = notificationSchedules.find { it.id == scheduleId }
                schedule?.let {
                    if (isEnabled) {
                        // Schedule ulang alarm
                        alarmScheduler.scheduleAlarm(it.copy(isEnabled = true))
                        Log.d(TAG, "Alarm rescheduled for: ${it.hour}:${it.minute}")
                    } else {
                        // Cancel alarm
                        alarmScheduler.cancelAlarm(scheduleId)
                        Log.d(TAG, "Alarm cancelled")
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error toggling schedule", e)
                Toast.makeText(
                    requireContext(),
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun updateUserSettings(field: String, value: Any) {
        firestore.collection("user_settings")
            .document(userId)
            .update(field, value)
            .addOnSuccessListener {
                Log.d(TAG, "User settings updated: $field = $value")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error updating user settings", e)
            }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
            }
        }
    }

    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (!alarmManager.canScheduleExactAlarms()) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Izin Diperlukan")
                    .setMessage("Aplikasi memerlukan izin untuk mengatur alarm tepat waktu. Silakan aktifkan di pengaturan.")
                    .setPositiveButton("Buka Pengaturan") { _, _ ->
                        try {
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                            startActivity(intent)
                        } catch (e: Exception) {
                            Log.e(TAG, "Cannot open exact alarm settings", e)
                            Toast.makeText(
                                requireContext(),
                                "Tidak dapat membuka pengaturan",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    .setNegativeButton("Nanti", null)
                    .show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            NOTIFICATION_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Notification permission granted")
                    Toast.makeText(
                        requireContext(),
                        "Izin notifikasi diberikan",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Log.d(TAG, "Notification permission denied")
                    Toast.makeText(
                        requireContext(),
                        "Izin notifikasi ditolak. Fitur pengingat mungkin tidak berfungsi.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}