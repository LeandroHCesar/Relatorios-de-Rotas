package com.relatriosderotas.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.relatriosderotas.R
import com.relatriosderotas.databinding.FragmentPersonalBinding
import com.relatriosderotas.databinding.FragmentRotasBinding
import com.relatriosderotas.helper.MaxLengthInputFilter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class RotasFragment : Fragment() {

    private var _binding: FragmentRotasBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRotasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Certifique-se de que _binding não seja nulo antes de usá-lo
        when {
            _binding != null -> {
                // Configurar a Toolbar
                (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
                (activity as AppCompatActivity).supportActionBar?.apply {
                    setDisplayHomeAsUpEnabled(true)
                    setHomeAsUpIndicator(R.drawable.ic_back_arrow_white) // Defina o ícone personalizado aqui
                    title = "Dados de Rota"
                    binding.toolbar.setTitleTextAppearance(requireContext(), R.style.ToolbarTitleStyle)
                }

                // Configurar o clique na seta de voltar
                binding.toolbar.setNavigationOnClickListener {
                    requireActivity().onBackPressed()
                }

                val spinner = binding.spinnerValoresCidades
                val cidadesArray = resources.getStringArray(R.array.valores_cidades_array)
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, cidadesArray)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter

                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        val selectedCity = cidadesArray[position]
                        // Faça algo com o valor selecionado
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // Nenhuma seleção foi feita
                    }
                }

                // Evento de clique no botão "Adicionar Nova Rota"
                binding.buttonAdicionarRota.setOnClickListener {
                    val valoresCidadesArray = resources.getStringArray(R.array.valores_cidades_array)
                    val valorSelecionadoString = valoresCidadesArray[binding.spinnerValoresCidades.selectedItemPosition]

                    // Remover símbolo "R$" e vírgula do valor selecionado
                    val valorSelecionadoCentavos = valorSelecionadoString.replace("R$", "").replace(",", "").trim().toInt()

                    val valorSelecionadoReais = valorSelecionadoCentavos / 100.0

                    var valorFinal = valorSelecionadoReais
                    if (binding.switchDomingosFeriados.isChecked) {
                        valorFinal += valorSelecionadoReais * 0.15
                    }

                    //val valorParadas = binding.editTextParadas.text.toString().toDoubleOrNull() ?: 0.0
                    //valorFinal += valorParadas

                    binding.editTextDiaria.setText(String.format("R$ %.2f", valorFinal))
                }

                binding.editTextParadas.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        // Quando o texto for alterado no campo de Paradas

                        val paradasText = s.toString().toIntOrNull() // Obtém o valor digitado ou null se não for um número válido

                        // Realiza o cálculo com base nas faixas de paradas
                        val adicionalValue = paradasText?.let { paradas ->
                            when {
                                paradas in 1..60 -> paradas * 0.3
                                paradas in 61..90 -> 60 * 0.3 + (paradas - 60) * 1.46
                                paradas > 90 -> 60 * 0.3 + 30 * 1.46 + (paradas - 90) * 0.6
                                else -> 0.0
                            }
                        } ?: 0.0 // Valor padrão se paradasText for nulo

                        // Formata o valor para exibição em R$ e atualiza o campo de Adicional
                        val formattedAdicionalValue = String.format("R$ %.2f", adicionalValue)
                        binding.editTextAdicional.setText(formattedAdicionalValue)

                        // Atualize outros campos ou realize outras ações com base no valor calculado, se necessário
                    }
                    override fun afterTextChanged(s: Editable?) {}
                })

                // Configurar o clique no campo de data
                binding.editTextData.setOnClickListener {
                    showDatePicker()
                }

                // Exemplo de uso:
                val editTextId = binding.editTextId
                editTextId.filters = arrayOf(MaxLengthInputFilter(9)) // Limite de 9 caracteres

                val editTextDescricaoCidades = binding.editTextDescricaoCidades
                editTextDescricaoCidades.filters = arrayOf(MaxLengthInputFilter(3 * 20)) // Limite de 3 cidades com até 20 caracteres cada

                val editTextCodRota = binding.editTextCodRota
                editTextCodRota.filters = arrayOf(MaxLengthInputFilter(4)) // Limite de 5 caracteres

                val editTextKm = binding.editTextKm
                editTextKm.filters = arrayOf(MaxLengthInputFilter(3)) // Limite de 6 caracteres

                val editTextParadas = binding.editTextParadas
                editTextParadas.filters = arrayOf(MaxLengthInputFilter(3)) // Limite de 3 caracteres

                val editTextPacotes = binding.editTextPacotes
                editTextPacotes.filters = arrayOf(MaxLengthInputFilter(3)) // Limite de 3 caracteres

                val editTextPedagio = binding.editTextPedagio
                editTextPedagio.filters = arrayOf(MaxLengthInputFilter(8)) // Limite de 8 caracteres

                val editTextCombustivel = binding.editTextCombustivel
                editTextCombustivel.filters = arrayOf(MaxLengthInputFilter(8)) // Limite de 8 caracteres
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                binding.editTextData.setText(selectedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}