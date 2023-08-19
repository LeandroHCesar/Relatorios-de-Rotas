package com.relatriosderotas.helper

import android.os.Parcel
import android.os.Parcelable

data class RotaData(
    val diaDaSemana: String = "", val data: String = "", val id: String = "", val descricaoCidades: String = "",
    val codRota: String = "", val km: String = "", val paradas: String = "", val pacotes: String = "",
    val pedagioCentavos: Int = 0, val combustivelCentavos: Int = 0, val diariaCentavos: Int = 0, val adicionalCentavos: Int = 0
): Parcelable  {
    // Construtor sem argumentos (necessário para o Firebase Database)
    constructor() : this("", "", "", "", "", "",
        "", "", 0, 0, 0, 0)

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(diaDaSemana)
        parcel.writeString(data)
        parcel.writeString(id)
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

    constructor(parcel: Parcel) : this(
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

    companion object CREATOR : Parcelable.Creator<RotaData> {
        override fun createFromParcel(parcel: Parcel): RotaData {

            return RotaData(parcel.toString())

        }

        override fun newArray(size: Int): Array<RotaData?> {
            return arrayOfNulls(size)
        }
    }
}


