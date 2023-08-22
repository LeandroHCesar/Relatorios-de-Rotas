package com.relatriosderotas.helper

import android.os.Parcel
import android.os.Parcelable

data class RotaData(
    var idRota: String = "",
    val diaDaSemana: String = "",
    var data: String = "",
    var numRota: String = "",
    var descricaoCidades: String = "",
    var codRota: String = "",
    var km: String = "",
    var paradas: String = "",
    var pacotes: String = "",
    var pedagioCentavos: Int = 0,
    var combustivelCentavos: Int = 0,
    val diariaCentavos: Int = 0,
    var adicionalCentavos: Int = 0
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idRota)
        parcel.writeString(diaDaSemana)
        parcel.writeString(data)
        parcel.writeString(numRota)
        parcel.writeString(descricaoCidades)
        parcel.writeString(codRota)
        parcel.writeString(km)
        parcel.writeString(paradas)
        parcel.writeString(pacotes)
        parcel.writeInt(pedagioCentavos)
        parcel.writeInt(combustivelCentavos)
        parcel.writeInt(diariaCentavos)
        parcel.writeInt(adicionalCentavos)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RotaData> {
        override fun createFromParcel(parcel: Parcel): RotaData {
            return RotaData(parcel)
        }

        override fun newArray(size: Int): Array<RotaData?> {
            return arrayOfNulls(size)
        }
    }
}
