package com.relatriosderotas.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.relatriosderotas.R
import com.relatriosderotas.databinding.FragmentPersonalBinding
import com.relatriosderotas.helper.KeyboardUtils
import com.relatriosderotas.helper.UserDetails

class PersonalFragment : Fragment() {

    private var _binding: FragmentPersonalBinding? = null
    private val binding get() = _binding!!
    private var isEditMode = false
    private var database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val databaseRef: DatabaseReference = database.reference
    private lateinit var auth: FirebaseAuth
    private lateinit var userRef: DatabaseReference
    private lateinit var valueEventListenerUser: ValueEventListener
    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Certifique-se de que _binding não seja nulo antes de usá-lo
        when {
            _binding != null -> {
                val firebaseAuth = FirebaseAuth.getInstance()
                val currentUser: FirebaseUser? = firebaseAuth.currentUser
                database = FirebaseDatabase.getInstance()
                auth = Firebase.auth
                // Verifique se o usuário está autenticado
                if (currentUser != null) {
                    // Obtenha o ID do usuário autenticado
                    userId = currentUser.uid
                    // Obtenha o ID do usuário autenticado
                    userId?.let { uid ->
                        // Obtenha a referência para o nó "userDetails" no banco de dados para o usuário atual
                        userRef = databaseRef
                            .child("meus_apps")
                            .child("relatorio_de_rotas")
                            .child("usuarios")
                            .child(userId!!)
                            .child("userDetails")
                    }
                }
                setupViews()
                configToolbar()
            }
        }
    }

    private fun initUser() {
        // Adicione um listener para ouvir as alterações nos dados do usuário
        valueEventListenerUser = userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("PersonalDataFragment", "onDataChange: Snapshot: $snapshot")

                // Obtenha o objeto UserData do snapshot
                val userData = snapshot.getValue(UserDetails::class.java)
                Log.d("PersonalDataFragment", "onDataChange: UserData: $userData")

                // Se o objeto UserData não for nulo, atualize os campos de edição com os dados do usuário
                userData?.let { data ->
                    binding.editTextNomeCompleto.setText(data.nomeCompleto)
                    binding.editTextCpf.setText(data.cpf)
                    binding.editTextVeiculo.setText(data.veiculo)
                    binding.editTextPlaca.setText(data.placa)
                    binding.editTextTitular.setText(data.titular)
                    binding.editTextCnpj.setText(data.cnpj)
                    binding.editTextBanco.setText(data.banco)
                    binding.editTextAgencia.setText(data.agencia)
                    binding.editTextConta.setText(data.conta)
                    binding.editTextPix.setText(data.pix)
                    // Atualize outros campos aqui, se houver
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Tratar erros, se necessário
                displayMessageError("Erro ao salvar dados. $error")
            }
        })
    }

    private fun configToolbar() {
        // Configurar a Toolbar
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back_arrow_white) // Defina o ícone personalizado aqui
            title = "Dados do Usuário"
            binding.toolbar.setTitleTextAppearance(requireContext(), R.style.ToolbarTitleStyle)
        }
        // Configurar o clique na seta de voltar
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun setupViews() {
        // Obtenha referências das views através do binding
        val btnSave = binding.buttonSalvar
        /*val editTextNomeCompleto = binding.editTextNomeCompleto
        val editTextCpf = binding.editTextCpf
        val editTextVeiculo = binding.editTextVeiculo
        val editTextPlaca = binding.editTextPlaca
        val editTextTitular = binding.editTextTitular
        val editTextCnpj = binding.editTextCnpj
        val editTextBanco = binding.editTextBanco
        val editTextAgencia = binding.editTextAgencia
        val editTextConta = binding.editTextConta
        val editTextPix = binding.editTextPix*/

        // Configure os cliques dos botões
        btnSave.setOnClickListener {
            KeyboardUtils.hideKeyboard(requireContext(), it)
            if (isEditMode) {
                if (validateForm()) {
                    // Salve os dados no banco de dados
                    saveDataToDatabase()
                    displayMessage("Os dados foram atualizados com sucesso.")
                }
            } else {
                setEditMode(true)
            }
        }
        // Define o modo de exibição inicial
        setEditMode(false)
    }

    private fun setEditMode(enabled: Boolean) {
        isEditMode = enabled
        val btnSave = binding.buttonSalvar

        if (enabled) {
            btnSave.text = "Salvar"
            btnSave.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.color_destaque
                )
            ) // Cor para o modo de edição
        } else {
            btnSave.text = "Editar"
            btnSave.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.color_default
                )
            ) // Cor para o modo de visualização
        }
        setFormFieldsEnabled(enabled)
    }

    private fun setFormFieldsEnabled(enabled: Boolean) {
        // Define a propriedade isEnabled dos campos de edição através do binding
        binding.editTextNomeCompleto.isEnabled = enabled
        binding.editTextCpf.isEnabled = enabled
        binding.editTextVeiculo.isEnabled = enabled
        binding.editTextPlaca.isEnabled = enabled
        binding.editTextTitular.isEnabled = enabled
        binding.editTextCnpj.isEnabled = enabled
        binding.editTextBanco.isEnabled = enabled
        binding.editTextAgencia.isEnabled = enabled
        binding.editTextConta.isEnabled = enabled
        binding.editTextPix.isEnabled = enabled
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // Valide os campos individualmente aqui e, se necessário, exiba mensagens de erro usando setError() nos TextInputLayouts
        // Campo "Nome Completo"
        if (binding.editTextNomeCompleto.text.isNullOrBlank()) {
            binding.textInputLayoutNomeCompleto.error = "Informe o nome completo"
            isValid = false
        } else {
            binding.textInputLayoutNomeCompleto.error = null
        }
        // Campo "CPF"
        val cpf = binding.editTextCpf.text.toString()
        if (cpf.isBlank()) {
            binding.textInputLayoutCpf.error = "Informe o CPF"
            isValid = false
        } else if (cpf.length < 11) {
            binding.textInputLayoutCpf.error = "CPF inválido"
            isValid = false
        } else {
            binding.textInputLayoutCpf.error = null
        }

        // Campo "Veículo"
        if (binding.editTextVeiculo.text.isNullOrBlank()) {
            binding.textInputLayoutVeiculo.error = "Informe o veículo"
            isValid = false
        } else {
            binding.textInputLayoutVeiculo.error = null
        }

        // Campo "Placa"
        if (binding.editTextPlaca.text.isNullOrBlank()) {
            binding.textInputLayoutPlaca.error = "Informe a placa"
            isValid = false
        } else {
            binding.textInputLayoutPlaca.error = null
        }

        // Campo "Titular"
        if (binding.editTextTitular.text.isNullOrBlank()) {
            binding.textInputLayoutTitular.error = "Informe o titular"
            isValid = false
        } else {
            binding.textInputLayoutTitular.error = null
        }

        // Campo "CNPJ"
        val cnpj = binding.editTextCnpj.text.toString()
        if (cnpj.isBlank()) {
            binding.textInputLayoutCnpj.error = "Informe o CNPJ"
            isValid = false
        } else if (cnpj.length < 14) {
            binding.textInputLayoutCnpj.error = "CNPJ inválido"
            isValid = false
        } else {
            binding.textInputLayoutCnpj.error = null
        }

        // Campo "Banco"
        if (binding.editTextBanco.text.isNullOrBlank()) {
            binding.textInputLayoutBanco.error = "Informe o banco"
            isValid = false
        } else {
            binding.textInputLayoutBanco.error = null
        }

        // Campo "Agência"
        if (binding.editTextAgencia.text.isNullOrBlank()) {
            binding.textInputLayoutAgencia.error = "Informe a agência"
            isValid = false
        } else {
            binding.textInputLayoutAgencia.error = null
        }

        // Campo "Conta"
        if (binding.editTextConta.text.isNullOrBlank()) {
            binding.textInputLayoutConta.error = "Informe a conta"
            isValid = false
        } else {
            binding.textInputLayoutConta.error = null
        }

        // Campo "PIX"
        if (binding.editTextPix.text.isNullOrBlank()) {
            binding.textInputLayoutPix.error = "Informe o PIX"
            isValid = false
        } else {
            binding.textInputLayoutPix.error = null
        }
        return isValid
    }

    private fun saveDataToDatabase() {
        // Obtém os valores dos campos
        val nomeCompleto = binding.editTextNomeCompleto.text.toString().trim()
        val cpf = binding.editTextCpf.text.toString().trim()
        val veiculo = binding.editTextVeiculo.text.toString().trim()
        val placa = binding.editTextPlaca.text.toString().trim()
        val titular = binding.editTextTitular.text.toString().trim()
        val cnpj = binding.editTextCnpj.text.toString().trim()
        val banco = binding.editTextBanco.text.toString().trim()
        val agencia = binding.editTextAgencia.text.toString().trim()
        val conta = binding.editTextConta.text.toString().trim()
        val pix = binding.editTextPix.text.toString().trim()

        // Verifique se os campos estão preenchidos corretamente
        if (validateForm() && userId != null) {
            // Crie um mapa com os dados do usuário
            val userDataMap = hashMapOf(
                "nomeCompleto" to nomeCompleto,
                "cpf" to cpf,
                "veiculo" to veiculo,
                "placa" to placa,
                "titular" to titular,
                "cnpj" to cnpj,
                "banco" to banco,
                "agencia" to agencia,
                "conta" to conta,
                "pix" to pix
                // Adicione outros campos aqui, se houver
            )

            // Crie ou atualize os dados do usuário no banco de dados
            val userRef = databaseRef
                .child("meus_apps")
                .child("relatorio_de_rotas")
                .child("usuarios")
                .child(userId!!)
                .child("userDetails")

            userRef.updateChildren(userDataMap as Map<String, Any>)
                .addOnSuccessListener {
                    // Sucesso ao salvar os dados
                    // Alterar para o modo de visualização após salvar os dados
                    setEditMode(false)

                    val fragmentManager = requireActivity().supportFragmentManager
                    fragmentManager.popBackStack() // Voltar ao fragment anterior
                    // Faça algo aqui, como exibir uma mensagem de sucesso
                }
                .addOnFailureListener {
                    // Falha ao salvar os dados
                    // Faça algo aqui, como exibir uma mensagem de erro
                }
        }
    }

    private fun displayMessage(message: String) {
        val context = requireContext()
        val duration = Toast.LENGTH_SHORT
        val toast = Toast.makeText(context, message, duration)
        toast.show()
    } // ok

    private fun displayMessageError(error: String) {
        // Exibir a mensagem de erro na interface do usuário, por exemplo:
        val context = requireContext()
        val duration = Toast.LENGTH_SHORT
        val toast = Toast.makeText(context, "Error: $error", duration)
        toast.show()
    }  //ok

    override fun onStart() {
        super.onStart()
        initUser()
        Log.i("evento", "onStartPersonal: listener iniciado")
    }

    override fun onStop() {
        super.onStop()
        userRef.removeEventListener(valueEventListenerUser)
        Log.d("evento", "onStopPersonal: listener removido")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
