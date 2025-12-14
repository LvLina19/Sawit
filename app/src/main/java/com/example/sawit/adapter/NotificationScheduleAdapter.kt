package com.example.sawit.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.sawit.R
import com.example.sawit.model.NotificationSchedule

class NotificationScheduleAdapter(
    private var schedules: MutableList<NotificationSchedule>,
    private val onToggle: (NotificationSchedule, Boolean) -> Unit,
    private val onDelete: (NotificationSchedule) -> Unit,
    private val onEdit: (NotificationSchedule) -> Unit
) : RecyclerView.Adapter<NotificationScheduleAdapter.ScheduleViewHolder>() {

    inner class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvType: TextView = itemView.findViewById(R.id.tvType)
        val tvDays: TextView = itemView.findViewById(R.id.tvDays)
        val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)
        val switchEnabled: SwitchCompat = itemView.findViewById(R.id.switchEnabled)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
        val btnEdit: ImageView = itemView.findViewById(R.id.btnEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification_schedule, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val schedule = schedules[position]

        // Format time
        val timeString = String.format("%02d:%02d", schedule.hour, schedule.minute)
        holder.tvTime.text = timeString

        // Set type and icon
        val (typeName, iconRes) = when (schedule.type) {
            "water" -> "Minum Air" to R.drawable.ic_water_drop
            "exercise" -> "Olahraga" to R.drawable.ic_exercise
            "sleep" -> "Tidur" to R.drawable.ic_sleep
            "check_farm" -> "Cek Kebun" to R.drawable.ic_farm
            "custom" -> schedule.customMessage to R.drawable.ic_bell
            else -> "Pengingat" to R.drawable.ic_bell
        }

        holder.tvType.text = typeName
        holder.ivIcon.setImageResource(iconRes)

        // Format repeat days
        val daysText = if (schedule.repeatDays.size == 7) {
            "Setiap Hari"
        } else {
            val dayMap = mapOf(
                "Mon" to "Sen",
                "Tue" to "Sel",
                "Wed" to "Rab",
                "Thu" to "Kam",
                "Fri" to "Jum",
                "Sat" to "Sab",
                "Sun" to "Min"
            )
            schedule.repeatDays.joinToString(", ") { dayMap[it] ?: it }
        }
        holder.tvDays.text = daysText

        // Set switch state
        holder.switchEnabled.isChecked = schedule.isEnabled
        holder.switchEnabled.setOnCheckedChangeListener { _, isChecked ->
            onToggle(schedule, isChecked)
        }

        // Delete button
        holder.btnDelete.setOnClickListener {
            onDelete(schedule)
        }

        // Edit button
        holder.btnEdit.setOnClickListener {
            onEdit(schedule)
        }
    }

    override fun getItemCount(): Int = schedules.size

    fun updateSchedules(newSchedules: List<NotificationSchedule>) {
        schedules.clear()
        schedules.addAll(newSchedules)
        notifyDataSetChanged()
    }

    fun removeSchedule(schedule: NotificationSchedule) {
        val position = schedules.indexOf(schedule)
        if (position != -1) {
            schedules.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}