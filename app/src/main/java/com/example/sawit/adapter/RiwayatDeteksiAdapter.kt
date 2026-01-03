package com.example.sawit.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.sawit.R
import com.example.sawit.data.model.RiwayatDeteksiModel
import java.text.SimpleDateFormat
import java.util.*

class RiwayatDeteksiAdapter(
    private val onDeleteClick: (RiwayatDeteksiModel) -> Unit
) : ListAdapter<RiwayatDeteksiModel, RiwayatDeteksiAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_riwayat, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivDeteksi: ImageView = itemView.findViewById(R.id.ivDeteksi)
        private val tvJenisBuah: TextView = itemView.findViewById(R.id.tvJenisBuah)
        private val tvKepercayaan: TextView = itemView.findViewById(R.id.tvKepercayaan)
        private val tvLokasi: TextView = itemView.findViewById(R.id.tvLokasi)
        private val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(item: RiwayatDeteksiModel) {
            // Set text
            tvJenisBuah.text = item.jenisBuah
            tvKepercayaan.text = "Kepercayaan: ${item.kepercayaan}%"
            tvLokasi.text = item.lokasi

            // Format tanggal
            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            tvTanggal.text = item.tanggal
                ?.toDate()
                ?.let { dateFormat.format(it) }
                ?: "-"

            // Set warna berdasarkan jenis buah
            val textColor = when (item.jenisBuah) {
                "Mentah" -> android.graphics.Color.parseColor("#E53935")
                "Matang" -> android.graphics.Color.parseColor("#43A047")
                "Kelewat Matang" -> android.graphics.Color.parseColor("#FB8C00")
                else -> android.graphics.Color.parseColor("#757575")
            }
            tvJenisBuah.setTextColor(textColor)

            // Load image dari ImgBB menggunakan Glide
            Glide.with(itemView.context)
                .load(item.imageUrl?.trim())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_error)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(ivDeteksi)


            // Delete button click
            btnDelete.setOnClickListener {
                onDeleteClick(item)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<RiwayatDeteksiModel>() {
        override fun areItemsTheSame(
            oldItem: RiwayatDeteksiModel,
            newItem: RiwayatDeteksiModel
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: RiwayatDeteksiModel,
            newItem: RiwayatDeteksiModel
        ): Boolean {
            return oldItem == newItem
        }
    }
}