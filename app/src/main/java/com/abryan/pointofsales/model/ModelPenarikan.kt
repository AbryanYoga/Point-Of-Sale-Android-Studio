package com.abryan.pointofsales.model

import java.io.Serializable

data class ModelPenarikan(
    val id: String = "",
    val tanggal: String = "",
    val waktu: String = "",
    val nominal: Long = 0,
    val metode: String = "",
    val nomorTujuan: String = "",
    val status: String = ""
) : Serializable
