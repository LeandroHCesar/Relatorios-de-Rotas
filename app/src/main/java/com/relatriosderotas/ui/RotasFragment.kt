package com.relatriosderotas.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.relatriosderotas.R
import com.relatriosderotas.databinding.FragmentRotasBinding
import com.relatriosderotas.helper.MaxLengthInputFilter
import com.relatriosderotas.helper.RotaData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RotasFragment : Fragment() {

    private var _binding: FragmentRotasBinding? = null
    private val binding get() = _binding!!

    private var isCalculating = true

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

                // Inicialize o Firebase Database
                val database = FirebaseDatabase.getInstance()
                val databaseRef = database.reference

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

                // Evento de clique no botão "Adicionar Nova Rota"
                binding.buttonAdicionarRota.setOnClickListener { calcularDiariaOrSalvar() }

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

                // Filtro de edit text
                filterEditText()

                // mostrar o spinner
                //configSpinner()
                setupDiariaField()

            }
        }
    }

    private fun saveRotaDataToDatabase() {
        val rotaData = createRotaData() // Crie o objeto RotaData com os dados

        // Verifique se o usuário está autenticado
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            // Obtenha a referência do banco de dados
            val databaseRef = FirebaseDatabase.getInstance().reference

            // Crie um nó "rotas" no banco de dados com o UID do usuário como parte da estrutura
            val rotasRef = databaseRef.child("meus_apps").child("relatorio_de_rotas")
                .child("users").child(userId).child("rotas")

            // Crie uma nova chave única para a rota
            val newRotaRef = rotasRef.push()

            // Salve os dados usando a chave única
            newRotaRef.setValue(rotaData)
                .addOnSuccessListener {
                    // Sucesso ao salvar os dados da rota
                    // Faça algo aqui, como exibir uma mensagem de sucesso
                }
                .addOnFailureListener {
                    // Falha ao salvar os dados da rota
                    // Faça algo aqui, como exibir uma mensagem de erro
                }
        }
    }

    private fun showSaveEditDialog() {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Salvar ou Editar")
            .setMessage("Deseja salvar ou editar os dados?")
            .setPositiveButton("Salvar") { _, _ ->
                saveRotaDataToDatabase()
                resetButtonAndFields()
                // Remove a RotaFragment da pilha e volta para a home
                val action = RotasFragmentDirections.actionRotasFragmentToHomeFragment()
                findNavController().navigate(action)

            }
            .setNegativeButton("Editar") { _, _ ->
                setFormFieldsEnabled(true)
                binding.buttonAdicionarRota.text = "Salvar"
                binding.buttonAdicionarRota.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.color_destaque))
                isCalculating = false
                // Recalcula a diária ao editar
                calcularDiaria()
            }
            .create()

        alertDialog.show()
    }

    private fun calcularDiariaOrSalvar() {
        val btnSave = binding.buttonAdicionarRota

        if (isCalculating) {
            // ... (código existente para calcular a diária)

            calcularDiaria()
            btnSave.text = "Salvar"
            btnSave.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.color_destaque))
            isCalculating = false
            setFormFieldsEnabled(false)
        } else {
            showSaveEditDialog()
        }
    }

    private fun setFormFieldsEnabled(enabled: Boolean) {
        // Define a propriedade isEnabled dos campos de edição através do binding
        binding.editTextData.isEnabled = enabled
        binding.editTextId.isEnabled = enabled
        binding.editTextDescricaoCidades.isEnabled = enabled
        binding.editTextCodRota.isEnabled = enabled
        binding.editTextKm.isEnabled = enabled
        binding.editTextParadas.isEnabled = enabled
        binding.editTextPacotes.isEnabled = enabled
        binding.editTextPedagio.isEnabled = enabled
        binding.editTextCombustivel.isEnabled = enabled
        binding.switchDomingosFeriados.isEnabled = enabled

        // Define a cor do texto dos campos de edição quando eles estiverem desabilitados
        val disabledTextColor = if (enabled) android.R.color.black else R.color.disabled_text_color
        binding.editTextData.setTextColor(ContextCompat.getColor(requireContext(), disabledTextColor))
        binding.editTextId.setTextColor(ContextCompat.getColor(requireContext(), disabledTextColor))
        binding.editTextDescricaoCidades.setTextColor(ContextCompat.getColor(requireContext(), disabledTextColor))
        binding.editTextCodRota.setTextColor(ContextCompat.getColor(requireContext(), disabledTextColor))
        binding.editTextKm.setTextColor(ContextCompat.getColor(requireContext(), disabledTextColor))
        binding.editTextParadas.setTextColor(ContextCompat.getColor(requireContext(), disabledTextColor))
        binding.editTextPacotes.setTextColor(ContextCompat.getColor(requireContext(), disabledTextColor))
        binding.editTextPedagio.setTextColor(ContextCompat.getColor(requireContext(), disabledTextColor))
        binding.editTextCombustivel.setTextColor(ContextCompat.getColor(requireContext(), disabledTextColor))

    }

    private fun createRotaData(): RotaData {
    val id = binding.editTextId.text.toString().trim()
    val descricaoCidades = binding.editTextDescricaoCidades.text.toString().trim()
    val codRota = binding.editTextCodRota.text.toString().trim()
    val km = binding.editTextKm.text.toString().trim()
    val paradas = binding.editTextParadas.text.toString().trim()
    val pacotes = binding.editTextPacotes.text.toString().trim()
    val pedagio = binding.editTextPedagio.text.toString().trim()
    val combustivel = binding.editTextCombustivel.text.toString().trim()
    val diaria = binding.editTextDiaria.text.toString().trim()
    val data = binding.editTextData.text.toString().trim()
    val adicional = binding.editTextAdicional.text.toString().trim()

    // Obter o valor em centavos a partir dos valores em formato de texto
    val pedagioCentavos = (pedagio.replace("R$", "").replace(",", "").toDouble() * 100).toInt()
    val combustivelCentavos = (combustivel.replace("R$", "").replace(",", "").toDouble() * 100).toInt()
    val diariaCentavos = (diaria.replace("R$", "").replace(",", "").toDouble() * 100).toInt()
    val adicionalCentavos = (adicional.replace("R$", "").replace(",", "").toDouble() * 100).toInt()

    // Obter o dia da semana
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val date = dateFormat.parse(data) // Converte a string de data para um objeto Date
    calendar.time = date
    val diaDaSemana = when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.SUNDAY -> "Domingo"
        Calendar.MONDAY -> "Segunda-feira"
        Calendar.TUESDAY -> "Terça-feira"
        Calendar.WEDNESDAY -> "Quarta-feira"
        Calendar.THURSDAY -> "Quinta-feira"
        Calendar.FRIDAY -> "Sexta-feira"
        Calendar.SATURDAY -> "Sábado"
        else -> ""
    }

    return RotaData(
        id = id,
        diaDaSemana = diaDaSemana,
        descricaoCidades = descricaoCidades,
        codRota = codRota,
        km = km,
        paradas = paradas,
        pacotes = pacotes,
        pedagioCentavos = pedagioCentavos,
        combustivelCentavos = combustivelCentavos,
        diariaCentavos = diariaCentavos,
        data = data,
        adicionalCentavos = adicionalCentavos
    )
}

    private fun calcularDiaria() {
        val valorSelecionadoString = binding.editTextDiaria.text.toString().replace("R$", "").trim()

        if (valorSelecionadoString.isNotEmpty()) {

            val valorSelecionadoCentavos = valorSelecionadoString.replace(",", "").toInt()
            val valorSelecionadoReais = valorSelecionadoCentavos / 100.0

            var valorFinal = valorSelecionadoReais
            if (binding.switchDomingosFeriados.isChecked) {
                valorFinal += valorSelecionadoReais * 0.15
            }
            binding.editTextDiaria.setText(String.format("R$ %.2f", valorFinal))
        }
    }

    private fun resetButtonAndFields() {
        binding.buttonAdicionarRota.text = "Calcular Diária"
        binding.buttonAdicionarRota.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.color_default))
        isCalculating = true
        setFormFieldsEnabled(true)

        // Limpa os campos
        binding.editTextData.text = null
        binding.editTextDescricaoCidades.text = null
        binding.editTextId.text = null
        binding.editTextCodRota.text = null
        binding.editTextKm.text = null
        binding.editTextParadas.text = null
        binding.editTextPacotes.text = null
        binding.editTextPedagio.text = null
        binding.editTextCombustivel.text = null
        binding.editTextDiaria.text = null
        binding.editTextAdicional.text = null
    }

    private fun filterEditText() {
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

    private fun setupDiariaField() {
        val cidadesArray = resources.getStringArray(R.array.valores_cidades_array)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, cidadesArray)
        val autoCompleteTextView = binding.editTextDiaria
        autoCompleteTextView.setAdapter(adapter)

        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selectedDiaria = cidadesArray[position]
            autoCompleteTextView.setText(selectedDiaria)
            autoCompleteTextView.clearFocus() // Para fechar o dropdown após a seleção
        }

        // Lidar com o clique no campo
        autoCompleteTextView.setOnClickListener {
            autoCompleteTextView.showDropDown()
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