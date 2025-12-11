package com.example.sawit.IsiDashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.sawit.R
import com.example.sawit.model.KebunData

class KebunAdapter(
    private val onItemClick: (KebunData) -> Unit,
    private val onDeleteClick: (KebunData) -> Unit
) : RecyclerView.Adapter<KebunAdapter.KebunViewHolder>() {

    private var kebunList: List<KebunData> = emptyList()

    fun submitList(list: List<KebunData>) {
        kebunList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KebunViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_kebun, parent, false)
        return KebunViewHolder(view)
    }

    override fun onBindViewHolder(holder: KebunViewHolder, position: Int) {
        holder.bind(kebunList[position])
    }

    override fun getItemCount(): Int = kebunList.size

    inner class KebunViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivKebunImage: ImageView = itemView.findViewById(R.id.ivKebunImage)
        private val tvNamaKebun: TextView = itemView.findViewById(R.id.tvNamaKebun)
        private val tvLokasiKebun: TextView = itemView.findViewById(R.id.tvLokasiKebun)
        private val btnLihatInformasi: View = itemView.findViewById(R.id.btnLihatInformasi)
        private val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)

        fun bind(kebunData: KebunData) {
            tvNamaKebun.text = kebunData.namaKebun
            tvLokasiKebun.text = kebunData.lokasiKebun

            // Set placeholder image (nanti bisa diganti dengan load image dari URL)
            ivKebunImage.setImageResource(R.drawable.ic_kebun_placeholder)

            btnLihatInformasi.setOnClickListener {
                onItemClick(kebunData)
            }

            btnDelete.setOnClickListener {
                onDeleteClick(kebunData)
            }
        }
    }
}