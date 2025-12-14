package com.example.sawit.pengaturan
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sawit.R
import com.example.sawit.model.UserSettings
import com.example.sawit.model.NotificationSchedule
import com.example.sawit.adapter.NotificationScheduleAdapter
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
                document.toObject(UserSettings::class.java)?.let { settings ->
                    switchNotifications.isChecked = settings.notificationsEnabled
                    switchSound.isChecked = settings.soundEnabled
                    switchVibration.isChecked = settings.vibrationEnabled
                }
            }
    }

    private fun loadNotificationSchedules() {
        firestore.collection("notification_schedules")
            .whereEqualTo("userId", userId)
            .orderBy("hour")
            .orderBy("minute")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                notificationSchedules.clear()
                snapshots?.documents?.forEach { doc ->
                    doc.toObject(NotificationSchedule::class.java)?.let {
                        notificationSchedules.add(it)
                    }
                }

                adapter.updateSchedules(notificationSchedules)
                updateEmptyState()
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
                val message = input.text.toString()
                if (message.isNotEmpty()) {
                    showRepeatDaysDialog(hour, minute, "custom", message)
                } else {
                    Toast.makeText(requireContext(), "Pesan tidak boleh kosong", Toast.LENGTH_SHORT).show()
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

        // Add all days by default
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
                    Toast.makeText(requireContext(), "Pilih minimal 1 hari", Toast.LENGTH_SHORT).show()
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

        firestore.collection("notification_schedules")
            .document(schedule.id)
            .set(schedule)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Jadwal berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                alarmScheduler.scheduleAlarm(schedule)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(requireContext(), "Jadwal berhasil dihapus", Toast.LENGTH_SHORT).show()
                        alarmScheduler.cancelAlarm(schedule.id)
                        adapter.removeSchedule(schedule)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun editSchedule(schedule: NotificationSchedule) {
        Toast.makeText(requireContext(), "Fitur edit akan segera hadir", Toast.LENGTH_SHORT).show()
    }

    private fun toggleSchedule(scheduleId: String, isEnabled: Boolean) {
        firestore.collection("notification_schedules")
            .document(scheduleId)
            .update("isEnabled", isEnabled)
            .addOnSuccessListener {
                val schedule = notificationSchedules.find { it.id == scheduleId }
                schedule?.let {
                    if (isEnabled) {
                        alarmScheduler.scheduleAlarm(it.copy(isEnabled = true))
                    } else {
                        alarmScheduler.cancelAlarm(scheduleId)
                    }
                }
            }
    }

    private fun updateUserSettings(field: String, value: Any) {
        firestore.collection("user_settings")
            .document(userId)
            .update(field, value)
            .addOnSuccessListener {
                // Settings updated
            }
    }
}