package com.abryan.pointofsales.model

import android.os.Parcel
import android.os.Parcelable

data class ModelPegawai(
    var idPegawai: String? = null,
    var namaPegawai: String? = null,
    var jenisKelamin: String? = null,
    var alamatPegawai: String? = null,
    var cabangPegawai: String? = null,
    var nomorHp: String? = null,
    var statusPegawai: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        idPegawai = parcel.readString(),
        namaPegawai = parcel.readString(),
        jenisKelamin = parcel.readString(),
        alamatPegawai = parcel.readString(),
        cabangPegawai = parcel.readString(),
        nomorHp = parcel.readString(),
        statusPegawai = parcel.readString()
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idPegawai)
        parcel.writeString(namaPegawai)
        parcel.writeString(jenisKelamin)
        parcel.writeString(alamatPegawai)
        parcel.writeString(cabangPegawai)
        parcel.writeString(nomorHp)
        parcel.writeString(statusPegawai)
    }

    companion object CREATOR : Parcelable.Creator<ModelPegawai> {
        override fun createFromParcel(parcel: Parcel): ModelPegawai = ModelPegawai(parcel)
        override fun newArray(size: Int): Array<ModelPegawai?> = arrayOfNulls(size)
    }
}
