package com.relatriosderotas.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.relatriosderotas.R
import com.relatriosderotas.databinding.FragmentRotasBinding
import com.relatriosderotas.helper.MaxLengthInputFilter
import com.relatriosderotas.helper.RotaData
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RotasFragment : Fragment() {

    private var _binding: FragmentRotasBinding? = null
    private val binding get() = _binding!!

    private var shouldShowDiariaDropdown = true
    private var calculatedDiariaValue: Int = 0

    private var isEditMode = false

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
                    binding.toolbar.setTitleTextAppearance(
                        requireContext(),
                        R.style.ToolbarTitleStyle
                    )
                }

                // Configurar o clique na seta de voltar
                binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }

                binding.editTextParadas.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        val paradasText = s.toString().toIntOrNull()
                        val adicionalValue = paradasText?.let { paradas ->
                            when {
                                paradas in 1..60 -> (paradas * 0.3 * 100).toInt()
                                paradas in 61..90 -> ((60 * 0.3 + (paradas - 60) * 1.46) * 100).toInt()
                                paradas > 90 -> ((60 * 0.3 + 30 * 1.46 + (paradas - 90) * 0.6) * 100).toInt()
                                else -> 0
                            }
                        } ?: 0
                        val formattedAdicionalValue = formatCurrencyValueCents(adicionalValue)
                        binding.editTextAdicional.setText(formattedAdicionalValue)
                    }

                    override fun afterTextChanged(s: Editable?) {}
                })

                // Configurar o clique no campo de data
                binding.editTextData.setOnClickListener { showDatePicker() }

                // Filtro de edit text
                filterEditText()

                // mostrar o spinner
                setupDiariaField()

                validaçãoInstantânea()

                setupEditMode()

                clicks()
            }
        }
    }

    private fun clicks() {
        if (isEditMode) {
            // Evento de clique no botão "Atualizar Rota":
            binding.buttonPrincipal.setOnClickListener {
                if (isFormValid()) {
                    val diaria = calcularDiaria()
                    setFormFieldsEnabled(false)
                    // Aqui você pode personalizar o texto do botão
                    binding.buttonPrincipal.text = "Atualizar Rota"
                    binding.buttonPrincipal.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.color_green_success
                        )
                    )
                    binding.buttonPrincipal.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.white
                        )
                    )
                    // Aqui você pode adicionar a lógica para atualizar a rota no banco de dados
                }
            }
        } else {
            // Evento de clique no botão "Calcular Rota":
            binding.buttonPrincipal.setOnClickListener {
                if (isFormValid()) {
                    val diaria = calcularDiaria()
                    setFormFieldsEnabled(false)
                    // Mostrar o layout de edição e esconde o botão "Calcular Rota"
                    binding.buttonPrincipal.visibility = View.GONE
                    binding.layoutBotoesSalvarEditar.visibility = View.VISIBLE
                    binding.buttonEditar.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.color_destaque
                        )
                    )
                    binding.buttonSalvar.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.color_green_success
                        )
                    )
                }
            }
        }

        // Evento de clique no botão "Editar":
        binding.buttonEditar.setOnClickListener {
            binding.autoCompleteTextViewDiaria.setText("") // Limpa o campo de diária
            setFormFieldsEnabled(true) // Habilita os campos
            binding.buttonPrincipal.visibility =
                View.VISIBLE // Mostra o botão "Calcular Rota"
            binding.layoutBotoesSalvarEditar.visibility =
                View.GONE // Esconde os botões "Salvar" e "Editar"
        }

        // Configurar o clique no botão "Salvar"
        binding.buttonSalvar.setOnClickListener {
            if (isFormValid()) {
                // Modo de salvamento
                saveRotaDataToDatabase()
                resetButtonAndFields()
                // Agendar o fechamento do fragmento após um breve atraso
                Handler(Looper.getMainLooper()).postDelayed({
                    val action = RotasFragmentDirections.actionRotasFragmentToHomeFragment()
                    findNavController().navigate(action)
                }, 1000) // Atraso de 1 segundo (1000 milissegundos)
                // Mostrar o botão "Calcular Rota" e esconder o layout de edição
                binding.buttonPrincipal.visibility = View.GONE
                binding.layoutBotoesSalvarEditar.visibility = View.VISIBLE
            }
        }
    }

    private fun setupEditMode() {
        val arguments = arguments
        if (arguments != null) {
            val rota: RotaData? = arguments.getParcelable("rota")
            if (rota != null) {
                isEditMode = true // Defina o modo de edição como verdadeiro
                binding.editTextData.setText(rota.data)
                binding.editTextId.setText(rota.id)
                binding.editTextDescricaoCidades.setText(rota.descricaoCidades)
                binding.editTextCodRota.setText(rota.codRota)
                binding.editTextKm.setText(rota.km)
                binding.editTextParadas.setText(rota.paradas)
                binding.editTextPacotes.setText(rota.pacotes)
                binding.editTextAdicional.setText(rota.adicionalCentavos.toString())
                binding.editTextPedagio.setText(rota.pedagioCentavos.toString())
                binding.editTextCombustivel.setText(rota.combustivelCentavos.toString())

                binding.buttonPrincipal.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.color_attention
                    )
                )
                binding.buttonPrincipal.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
            }
        }
    }

    private fun setupInstantValidationForAutoCompleteField(
        autoCompleteTextView: AutoCompleteTextView,
        textInputLayout: TextInputLayout
    ) {
        autoCompleteTextView.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                textInputLayout.error = null
            }
        }
    }

    private fun setupInstantValidationForField(
        textField: TextInputEditText,
        textInputLayout: TextInputLayout
    ) {
        textField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrBlank()) {
                    textInputLayout.error = null
                }
            }
        })
    }

    private fun formatCurrencyValueCents(valueCents: Int): String {
        val valueInReal = valueCents / 100.0
        return formatCurrency(valueInReal)
    }

    private fun convertCurrencyToCents(currency: String): Int {
        val cleanedCurrency = currency.replace("R$", "").replace(",", ".").trim()
        val valueInCents = (cleanedCurrency.toDouble() * 100).toInt()
        return valueInCents
    }

    private fun formatCurrencyField(input: String): String {
        val value = input.replace("R$", "").replace(",", "").toDoubleOrNull() ?: 0.0
        return formatCurrency(value)
    }

    private fun formatCurrency(value: Double): String {
        return String.format("R$ %.2f", value)
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
                    displayMessage("Dados da rota foram salvos com sucesso.")
                }
                .addOnFailureListener { exception ->
                    // Falha ao salvar os dados da rota
                    displayMessageError("Erro ao salvar os dados da rota: ${exception.message}")

                }
        }
    }

    private fun displayMessage(message: String) {
        val context = requireContext()
        val duration = Toast.LENGTH_SHORT
        val toast = Toast.makeText(context, message, duration)
        toast.show()
    }

    private fun displayMessageError(error: String) {
        // Exibir a mensagem de erro na interface do usuário, por exemplo:
        val context = requireContext()
        val duration = Toast.LENGTH_SHORT
        val toast = Toast.makeText(context, "Error: $error", duration)
        toast.show()
    }

    private fun isFormValid(): Boolean {
        var isValid = true
        // Validação dos campos
        if (binding.editTextData.text.isNullOrBlank()) {
            binding.textInputLayoutData.error = "Informe a data"
            isValid = false
        } else {
            binding.textInputLayoutData.error = null
        }

        if (binding.editTextId.text.isNullOrBlank()) {
            binding.textInputLayoutId.error = "Informe o ID da rota"
            isValid = false
        } else {
            binding.textInputLayoutId.error = null
        }

        if (binding.editTextDescricaoCidades.text.isNullOrBlank()) {
            binding.textInputLayoutDescricaoCidades.error = "Informe a descrição das cidades"
            isValid = false
        } else {
            binding.textInputLayoutDescricaoCidades.error = null
        }

        if (binding.editTextCodRota.text.isNullOrBlank()) {
            binding.textInputLayoutCodRota.error = "Informe o codigo da rota"
            isValid = false
        } else {
            binding.textInputLayoutCodRota.error = null
        }

        if (binding.editTextKm.text.isNullOrBlank()) {
            binding.textInputLayoutKm.error = "Informe o Km"
            isValid = false
        } else {
            binding.textInputLayoutKm.error = null
        }

        if (binding.editTextParadas.text.isNullOrBlank()) {
            binding.textInputLayoutParadas.error = "Informe quantas paradas."
            isValid = false
        } else {
            binding.textInputLayoutParadas.error = null
        }

        if (binding.editTextPacotes.text.isNullOrBlank()) {
            binding.textInputLayoutPacotes.error = "Informe quantos pacotes."
            isValid = false
        } else {
            binding.textInputLayoutPacotes.error = null
        }

        if (binding.autoCompleteTextViewDiaria.text.isNullOrBlank()) {
            binding.textInputLayoutDiaria.error = "Escolha uma opção."
            isValid = false
        } else {
            binding.textInputLayoutDiaria.error = null
        }

        if (binding.editTextPedagio.text.isNullOrBlank()) {
            binding.textInputLayoutPedagio.error = "Informe o valor dos pedagios ou 0"
            isValid = false
        } else {
            binding.textInputLayoutPedagio.error = null
        }

        if (binding.editTextCombustivel.text.isNullOrBlank()) {
            binding.textInputLayoutCombustivel.error = "Informe o valor dos combustíveis ou 0"
            isValid = false
        } else {
            binding.textInputLayoutCombustivel.error = null
        }

        return isValid
    }

    private fun calcularDiaria(): Int {
        // Formate os campos de pedágio e combustível
        binding.editTextPedagio.setText(formatCurrencyField(binding.editTextPedagio.text.toString()))
        binding.editTextCombustivel.setText(formatCurrencyField(binding.editTextCombustivel.text.toString()))

        val valorSelecionadoFormatted = binding.autoCompleteTextViewDiaria.text.toString()
            .replace("R$", "")
            .replace(".", "")
            .replace(",", "")
            .trim()

        val currencyFormat = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale("pt", "BR")))
        val valorSelecionado = currencyFormat.parse(valorSelecionadoFormatted)?.toDouble() ?: 0.0

        if (valorSelecionado != 0.0) {
            val valorSelecionadoCentavos = (valorSelecionado * 100).toInt()

            var valorFinal = valorSelecionadoCentavos
            if (binding.switchDomingosFeriados.isChecked) {
                valorFinal = (valorSelecionadoCentavos * 1.15).toInt()
            }

            val formattedValue = NumberFormat.getCurrencyInstance(
                Locale("pt", "BR")
            ).format((valorFinal / 100.0) / 100)
            binding.autoCompleteTextViewDiaria.setText(formattedValue)

            calculatedDiariaValue = valorFinal // Armazena o valor calculado
            return valorFinal
        }
        calculatedDiariaValue = 0 // Zera o valor calculado

        return 0
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
        binding.autoCompleteTextViewDiaria.isEnabled = enabled
        binding.switchDomingosFeriados.isEnabled = enabled

        // Define a cor do texto dos campos de edição quando eles estiverem desabilitados
        val disabledTextColor = if (enabled) android.R.color.black else R.color.disabled_text_color
        binding.editTextData.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                disabledTextColor
            )
        )
        binding.editTextId.setTextColor(ContextCompat.getColor(requireContext(), disabledTextColor))
        binding.editTextDescricaoCidades.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                disabledTextColor
            )
        )
        binding.editTextCodRota.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                disabledTextColor
            )
        )
        binding.editTextKm.setTextColor(ContextCompat.getColor(requireContext(), disabledTextColor))
        binding.editTextParadas.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                disabledTextColor
            )
        )
        binding.editTextPacotes.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                disabledTextColor
            )
        )
        binding.editTextPedagio.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                disabledTextColor
            )
        )
        binding.editTextCombustivel.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                disabledTextColor
            )
        )
        binding.autoCompleteTextViewDiaria.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                disabledTextColor
            )
        )

    }

    private fun createRotaData(): RotaData {
        val id = binding.editTextId.text.toString().trim()
        val descricaoCidades = binding.editTextDescricaoCidades.text.toString().trim()
        val codRota = binding.editTextCodRota.text.toString().trim()
        val km = binding.editTextKm.text.toString().trim()
        val paradas = binding.editTextParadas.text.toString().trim()
        val pacotes = binding.editTextPacotes.text.toString().trim()
        val diaria = binding.autoCompleteTextViewDiaria.text.toString().trim()
        val data = binding.editTextData.text.toString().trim()
        val adicional = binding.editTextAdicional.text.toString().trim()

        // Obter o valor em centavos a partir dos valores em formato de texto
        val pedagio = binding.editTextPedagio.text.toString().trim()
        val combustivel = binding.editTextCombustivel.text.toString().trim()

        val pedagioCentavos = convertCurrencyToCents(pedagio)
        val combustivelCentavos = convertCurrencyToCents(combustivel)

        Log.d("Debug", "Valor de diaria: $diaria")
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        val diariaValue = currencyFormat.parse(diaria)?.toDouble() ?: 0.0
        val diariaCentavos = (diariaValue * 100).toInt()

        // Remova o "R$" e substitua a vírgula por ponto
        val adicionalFormatted = adicional
            .replace("R$", "")
            .replace(".", "")
            .replace(",", ".")
            .trim()
        val adicionalValue = adicionalFormatted.toDoubleOrNull() ?: 0.0
        val adicionalCentavos = (adicionalValue * 100).toInt()

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

    private fun resetButtonAndFields() {

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
        binding.autoCompleteTextViewDiaria.text = null
        binding.editTextAdicional.text = null

        // Resetar a validação dos TextInputLayouts
        binding.textInputLayoutId.error = null
        binding.textInputLayoutDescricaoCidades.error = null
        binding.textInputLayoutData.error = null
        binding.textInputLayoutCodRota.error = null
        binding.textInputLayoutKm.error = null
        binding.textInputLayoutParadas.error = null
        binding.textInputLayoutPacotes.error = null
        binding.textInputLayoutPedagio.error = null
        binding.textInputLayoutCombustivel.error = null
        binding.textInputLayoutDiaria.error = null
        binding.editTextAdicional.error = null

    }

    private fun filterEditText() {
        val editTextId = binding.editTextId
        editTextId.filters = arrayOf(MaxLengthInputFilter(9)) // Limite de 9 caracteres

        val editTextDescricaoCidades = binding.editTextDescricaoCidades
        editTextDescricaoCidades.filters =
            arrayOf(MaxLengthInputFilter(3 * 20)) // Limite de 3 cidades com até 20 caracteres cada

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
        editTextCombustivel.filters = arrayOf(MaxLengthInputFilter(9)) // Limite de 9 caracteres
    }

    private fun setupDiariaField() {
        val cidadesArray = resources.getStringArray(R.array.valores_cidades_array)
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            cidadesArray
        )
        val autoCompleteTextView = binding.autoCompleteTextViewDiaria
        autoCompleteTextView.setAdapter(adapter)

        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selectedDiaria = cidadesArray[position]
            autoCompleteTextView.setText(selectedDiaria)
            autoCompleteTextView.clearFocus() // Para fechar o dropdown após a seleção
        }

        // Lidar com o clique no campo
        autoCompleteTextView.setOnClickListener {
            if (shouldShowDiariaDropdown) {
                autoCompleteTextView.showDropDown()
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
                val selectedDate =
                    String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                binding.editTextData.setText(selectedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun validaçãoInstantânea() {
        // Chamando a função para configurar validação instantânea para cada campo relevante
        setupInstantValidationForField(binding.editTextData, binding.textInputLayoutData)
        setupInstantValidationForField(binding.editTextId, binding.textInputLayoutId)
        setupInstantValidationForField(
            binding.editTextDescricaoCidades,
            binding.textInputLayoutDescricaoCidades
        )
        setupInstantValidationForField(
            binding.editTextCodRota,
            binding.textInputLayoutCodRota
        )
        setupInstantValidationForField(binding.editTextKm, binding.textInputLayoutKm)
        setupInstantValidationForField(
            binding.editTextParadas,
            binding.textInputLayoutParadas
        )
        setupInstantValidationForField(
            binding.editTextPacotes,
            binding.textInputLayoutPacotes
        )
        setupInstantValidationForAutoCompleteField(
            binding.autoCompleteTextViewDiaria,
            binding.textInputLayoutDiaria
        )
        setupInstantValidationForField(
            binding.editTextPedagio,
            binding.textInputLayoutPedagio
        )
        setupInstantValidationForField(
            binding.editTextCombustivel,
            binding.textInputLayoutCombustivel
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}