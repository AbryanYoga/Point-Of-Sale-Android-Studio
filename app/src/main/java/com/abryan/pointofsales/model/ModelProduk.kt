package com.abryan.pointofsales.model

import java.io.Serializable

data class ModelProduk(
    val id: String = "",
    val nama: String = "",
    val harga: Long = 0,
    val jenis: String = "",
    val stok: Int = 0,
    val cabang: String = "",
    val status: String = ""
) : Serializable