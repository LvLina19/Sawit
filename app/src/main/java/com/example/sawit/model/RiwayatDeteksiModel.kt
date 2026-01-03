package com.example.sawit.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class RiwayatDeteksiModel(
    @DocumentId
    val id: String = "",
    val imageUrl: String = "",
    val localImagePath: String = "", // Path lokal untuk fallback
    val jenisBuah: String = "",
    val lokasi: String = "",
    val tanggal: Timestamp? = null,
    val kepercayaan: Int = 0,
    val area: Double = 0.0,
    val userId: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null
) {
    // Constructor tanpa parameter untuk Firestore
    constructor() : this(
        id = "",
        imageUrl = "",
        localImagePath = "",
        jenisBuah = "",
        lokasi = "",
        tanggal = null,
        kepercayaan = 0,
        area = 0.0,
        userId = "",
        createdAt = null
    )
}