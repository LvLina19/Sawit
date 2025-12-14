package com.example.sawit.IsiDashboard

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sawit.R
import com.example.sawit.model.KebunData

class KebunAdapter(
    private val onItemClick: (KebunData) -> Unit,
    private val onEditClick: (KebunData) -> Unit,
    private val onDeleteClick: (KebunData) -> Unit
) : ListAdapter<KebunData, KebunAdapter.KebunViewHolder>(KebunDiffCallback()) {

    companion object {
        private const val TAG = "KebunAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KebunViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_kebun, parent, false)
        return KebunViewHolder(view)
    }

    override fun onBindViewHolder(holder: KebunViewHolder, position: Int) {
        val kebun = getItem(position)
        holder.bind(kebun)
    }

    inner class KebunViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardKebun: CardView = itemView.findViewById(R.id.cardKebun)
        private val ivKebunImage: ImageView = itemView.findViewById(R.id.ivKebunImage)
        private val tvNamaKebun: TextView = itemView.findViewById(R.id.tvNamaKebun)
        private val tvLokasiKebun: TextView = itemView.findViewById(R.id.tvLokasiKebun)
        private val btnEdit: ImageView = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
        private val btnLihatInformasi: LinearLayout = itemView.findViewById(R.id.btnLihatInformasi)

        fun bind(kebun: KebunData) {
            // Log untuk debugging
            Log.d(TAG, "Binding kebun: ID=${kebun.id}, Nama=${kebun.namaKebun}")

            // Validasi ID sebelum ditampilkan
            if (kebun.id == 0) {
                Log.e(TAG, "!!! WARNING: Kebun has ID = 0! Nama: ${kebun.namaKebun}")
            }

            tvNamaKebun.text = kebun.namaKebun
            tvLokasiKebun.text = kebun.lokasiKebun

            // Set click listeners dengan logging
            btnEdit.setOnClickListener {
                Log.d(TAG, ">>> Edit clicked: ID=${kebun.id}, Nama=${kebun.namaKebun}")
                onEditClick(kebun)
            }

            btnDelete.setOnClickListener {
                Log.d(TAG, ">>> Delete clicked: ID=${kebun.id}, Nama=${kebun.namaKebun}")
                onDeleteClick(kebun)
            }

            btnLihatInformasi.setOnClickListener {
                Log.d(TAG, ">>> Lihat Informasi clicked: ID=${kebun.id}, Nama=${kebun.namaKebun}")
                onItemClick(kebun)
            }

            cardKebun.setOnClickListener {
                Log.d(TAG, ">>> Card clicked: ID=${kebun.id}, Nama=${kebun.namaKebun}")
                onItemClick(kebun)
            }
        }
    }

    class KebunDiffCallback : DiffUtil.ItemCallback<KebunData>() {
        override fun areItemsTheSame(oldItem: KebunData, newItem: KebunData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: KebunData, newItem: KebunData): Boolean {
            return oldItem == newItem
        }
    }
}

//package com.example.sawit.IsiDashboard
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.LinearLayout
//import android.widget.TextView
//import androidx.cardview.widget.CardView
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.ListAdapter
//import androidx.recyclerview.widget.RecyclerView
//import com.example.sawit.R
//import com.example.sawit.model.KebunData
//
//class KebunAdapter(
//    private val onItemClick: (KebunData) -> Unit,
//    private val onEditClick: (KebunData) -> Unit,
//    private val onDeleteClick: (KebunData) -> Unit
//) : ListAdapter<KebunData, KebunAdapter.KebunViewHolder>(KebunDiffCallback()) {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KebunViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_kebun, parent, false)
//        return KebunViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: KebunViewHolder, position: Int) {
//        val kebun = getItem(position)
//        holder.bind(kebun)
//    }
//
//    inner class KebunViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        private val cardKebun: CardView = itemView.findViewById(R.id.cardKebun)
//        private val ivKebunImage: ImageView = itemView.findViewById(R.id.ivKebunImage)
//        private val tvNamaKebun: TextView = itemView.findViewById(R.id.tvNamaKebun)
//        private val tvLokasiKebun: TextView = itemView.findViewById(R.id.tvLokasiKebun)
//        private val btnEdit: ImageView = itemView.findViewById(R.id.btnEdit)
//        private val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
//        private val btnLihatInformasi: LinearLayout = itemView.findViewById(R.id.btnLihatInformasi)
//
//        fun bind(kebun: KebunData) {
//            tvNamaKebun.text = kebun.namaKebun
//            tvLokasiKebun.text = kebun.lokasiKebun
//
//            // Set click listeners
//            btnEdit.setOnClickListener {
//                android.util.Log.d("KebunAdapter", "Edit button clicked for: ${kebun.namaKebun}")
//                onEditClick(kebun)
//            }
//
//            btnDelete.setOnClickListener {
//                onDeleteClick(kebun)
//            }
//
//            btnLihatInformasi.setOnClickListener {
//                onItemClick(kebun)
//            }
//
//            // Optional: Click pada card juga bisa membuka detail
//            cardKebun.setOnClickListener {
//                onItemClick(kebun)
//            }
//        }
//    }
//
//    class KebunDiffCallback : DiffUtil.ItemCallback<KebunData>() {
//        override fun areItemsTheSame(oldItem: KebunData, newItem: KebunData): Boolean {
//            return oldItem.id == newItem.id
//        }
//
//        override fun areContentsTheSame(oldItem: KebunData, newItem: KebunData): Boolean {
//            return oldItem == newItem
//        }
//    }
//
//}