package com.abryan.pointofsales.model

import java.io.Serializable

data class ModelTransaksi(
    val idTransaksi: String = "",
    val tanggal: String = "",
    val waktu: String = "",
    val totalHarga: Long = 0,
    val ppn: Long = 0,
    val totalSetelahPpn: Long = 0,
    val nominalBayar: Long = 0,
    val kembalian: Long = 0,
    val totalItem: Int = 0,
    val cabang: String = "",
    val alamatCabang: String = "",
    val namaKasir: String = "",
    val nomorKasir: String = "",
    val listItem: List<ModelItemTransaksi> = emptyList(),
    val statusTransaksi: String = ""
) : Serializable
