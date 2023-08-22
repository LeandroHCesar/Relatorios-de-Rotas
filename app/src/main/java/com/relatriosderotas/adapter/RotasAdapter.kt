package com.relatriosderotas.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.relatriosderotas.R
import com.relatriosderotas.helper.RotaData

class RotasAdapter(
    private val context: Context,
    private val rotasList: List<RotaData>,
    var onItemClick: ((RotaData) -> Unit)? = null
) : RecyclerView.Adapter<RotasViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RotasViewHolder {
        val view = inflater.inflate(R.layout.layout_rota_item, parent, false)
        return RotasViewHolder(view)
    }

    override fun getItemCount(): Int {
        return rotasList.size
    }

    override fun onBindViewHolder(holder: RotasViewHolder, position: Int) {
        val rota = rotasList[position]
        holder.bind(rota)
        holder.editIcon.setOnClickListener {
            onItemClick?.let { it1 -> it1(rota) }
        }
    }
}

class RotasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val textWeekDay: TextView = itemView.findViewById(R.id.textWeekDay)
    private val textDate: TextView = itemView.findViewById(R.id.textData)
    private val textDescCity: TextView = itemView.findViewById(R.id.textCity)
    private val textIdRota: TextView = itemView.findViewById(R.id.textId)
    private val textCodRota: TextView = itemView.findViewById(R.id.textCodRod)
    private val textParadas: TextView = itemView.findViewById(R.id.textParadas)
    private val textPacotes: TextView = itemView.findViewById(R.id.textPacotes)
    private val textAdicional: TextView = itemView.findViewById(R.id.textAdicional)
    private val textDiaria: TextView = itemView.findViewById(R.id.textDiaria)
    private val textCombustivel: TextView = itemView.findViewById(R.id.textCombustivel)
    private val textPedagio: TextView = itemView.findViewById(R.id.textPedagio)
    private val textkm: TextView = itemView.findViewById(R.id.textKm)

    val editIcon: ImageView = itemView.findViewById(R.id.editIcon)

    fun bind(rota: RotaData) {
        val cidades = "Cidades:   ${rota.descricaoCidades}"
        val idRota = "Id da rota: ${rota.numRota}"
        val codRota = "Cod. da rota: ${rota.codRota}"
        val numParadas = "Paradas: ${rota.paradas}"
        val numPacotes = "Pacotes: ${rota.pacotes}"
        val formattedAdicionalValue  = "Adicional: ${formatValorEmReais(rota.adicionalCentavos)}"
        val formattedDiarialValue = "Diária: ${formatValorEmReais(rota.diariaCentavos)}"
        val formattedComblValue = "Combustível: ${formatValorEmReais(rota.combustivelCentavos)}"
        val formattedPedagiolValue = "Pedágio: ${formatValorEmReais(rota.pedagioCentavos)}"
        val numKM = "KM: ${rota.km}"

        textWeekDay.text = rota.diaDaSemana
        textDate.text = rota.data
        textDescCity.text = cidades
        textIdRota.text = idRota
        textCodRota.text = codRota
        textParadas.text = numParadas
        textPacotes.text = numPacotes
        textAdicional.text = formattedAdicionalValue
        textDiaria.text = formattedDiarialValue
        textCombustivel.text = formattedComblValue
        textPedagio.text = formattedPedagiolValue
        textkm.text = numKM

    }

    private fun formatValorEmReais(valorCentavos: Int): String {
        val valorReais = valorCentavos / 100.0
        return String.format("R$ %.2f", valorReais)
    }
}


