package com.relatriosderotas.helper

class DateCustom {

    fun mesAnoSelect(retornoData: List<String>): String {
        val dia = retornoData[0]
        val mes = retornoData[1]
        val ano = retornoData[2]

        val mesAno = mes + ano
        return mesAno
    }
}

