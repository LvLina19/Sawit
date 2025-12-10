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
        private val cardView: CardView = itemView.findViewById(R.id.cardKebun)
        private val tvNamaKebun: TextView = itemView.findViewById(R.id.tvNamaKebun)
        private val tvLuasLahan: TextView = itemView.findViewById(R.id.tvLuasLahan)
        private val tvLokasiKebun: TextView = itemView.findViewById(R.id.tvLokasiKebun)
        private val tvJenisBibit: TextView = itemView.findViewById(R.id.tvJenisBibit)
        private val tvJumlahTanaman: TextView = itemView.findViewById(R.id.tvJumlahTanaman)
        private val tvTahunTanam: TextView = itemView.findViewById(R.id.tvTahunTanam)
        private val tvJenisTanah: TextView = itemView.findViewById(R.id.tvJenisTanah)
        private val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)

        fun bind(kebunData: KebunData) {
            tvNamaKebun.text = kebunData.namaKebun
            tvLuasLahan.text = "Luas: ${kebunData.luasLahan} ha"
            tvLokasiKebun.text = "Lokasi: ${kebunData.lokasiKebun}"
            tvJenisBibit.text = "Bibit: ${kebunData.jenisBibit}"

            if (kebunData.jumlahTanaman > 0) {
                tvJumlahTanaman.visibility = View.VISIBLE
                tvJumlahTanaman.text = "Jumlah: ${kebunData.jumlahTanaman} tanaman"
            } else {
                tvJumlahTanaman.visibility = View.GONE
            }

            tvTahunTanam.text = "Tahun Tanam: ${kebunData.tahunTanam}"
            tvJenisTanah.text = "Tanah: ${kebunData.jenisTanah}"

            cardView.setOnClickListener {
                onItemClick(kebunData)
            }

            btnDelete.setOnClickListener {
                onDeleteClick(kebunData)
            }
        }
    }
}