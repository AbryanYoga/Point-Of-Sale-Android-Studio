package com.abryan.pointofsales.model

import android.os.Parcel
import android.os.Parcelable

data class ModelCabang(
    var idCabang: String? = null,
    var namaCabang: String? = null,
    var kodeCabang: String? = null,
    var penanggungJawab: String? = null,
    var nomorCabang: String? = null,
    var alamatCabang: String? = null,
    var statusCabang: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        idCabang = parcel.readString(),
        namaCabang = parcel.readString(),
        kodeCabang = parcel.readString(),
        penanggungJawab = parcel.readString(),
        nomorCabang = parcel.readString(),
        alamatCabang = parcel.readString(),
        statusCabang = parcel.readString()
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idCabang)
        parcel.writeString(namaCabang)
        parcel.writeString(kodeCabang)
        parcel.writeString(penanggungJawab)
        parcel.writeString(nomorCabang)
        parcel.writeString(alamatCabang)
        parcel.writeString(statusCabang)
    }

    companion object CREATOR : Parcelable.Creator<ModelCabang> {
        override fun createFromParcel(parcel: Parcel): ModelCabang = ModelCabang(parcel)
        override fun newArray(size: Int): Array<ModelCabang?> = arrayOfNulls(size)
    }
}
