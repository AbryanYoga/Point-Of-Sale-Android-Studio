package com.abryan.pointofsales.model

import java.io.Serializable

data class ModelItemTransaksi(
    var idProduk: String = "",
    var namaProduk: String = "",
    var harga: Long = 0,
    var jumlah: Int = 0,
    var subtotal: Long = 0,
    var imageUrl: String = ""
) : Serializable
