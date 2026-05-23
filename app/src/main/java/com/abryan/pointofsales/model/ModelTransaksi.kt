package com.abryan.pointofsales.model

import java.io.Serializable

data class ModelTransaksi(
    var idTransaksi: String = "",
    var tanggal: String = "",
    var waktu: String = "",
    var totalHarga: Long = 0,
    var totalItem: Int = 0,
    var cabang: String = "",
    var listItem: List<ModelItemTransaksi> = listOf(),
    var statusTransaksi: String = ""
) : Serializable
