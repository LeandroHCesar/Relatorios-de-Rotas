package com.relatriosderotas.helper

data class RotaData(
    val diaDaSemana: String = "", val data: String = "", val id: String = "", val descricaoCidades: String = "",
    val codRota: String = "", val km: String = "", val paradas: String = "", val pacotes: String = "",
    val pedagioCentavos: Int = 0, val combustivelCentavos: Int = 0, val diariaCentavos: Int = 0, val adicionalCentavos: Int = 0
) {
    // Construtor sem argumentos (necessário para o Firebase Database)
    constructor() : this("", "", "", "", "", "",
        "", "", 0, 0, 0, 0)
}


