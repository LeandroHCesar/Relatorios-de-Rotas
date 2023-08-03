package com.relatriosderotas.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.relatriosderotas.R
import com.relatriosderotas.databinding.FragmentRegisterBinding
import com.relatriosderotas.helper.KeyboardUtils
import com.relatriosderotas.helper.User

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        database = FirebaseDatabase.getInstance()
        auth = Firebase.auth
        initClicks()
    }

    private fun initClicks() {
        binding.buttonRegister.setOnClickListener {
            validateData()
            KeyboardUtils.hideKeyboard(requireContext(), it)
        }
    }

    private fun validateData() {
        //val name = binding.editTextNome.text.toString().trim()
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()

        /*if (name.isEmpty()) {
            binding.editTextNome.error = "Informe seu nome"
        } else */if (email.isEmpty()) {
            binding.editTextEmail.error = "Informe seu e-mail"
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextEmail.error = "E-mail inválido"
        } else if (password.isEmpty()) {
            binding.editTextPassword.error = "Informe sua senha"
        } else if (password.length < 8) {
            binding.editTextPassword.error = "A senha deve conter pelo menos 8 caracteres"
        } else if (!password.any { it.isUpperCase() }) {
            binding.editTextPassword.error = "A senha deve conter pelo menos 1 letra maiúscula"
        } else {
            binding.progressBar.isVisible = true
            registerUser(email, password)
        }
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Obter o ID do usuário recém-criado
                    val userId = auth.currentUser?.uid

                    // Criar um objeto User com os dados do usuário
                    val user = User(userId!!, email)

                    // Referência para o banco de dados "users" no Firebase Realtime Database
                    val userRef: DatabaseReference = database.getReference("users")

                    // Salvando o objeto User no banco de dados com o ID do usuário como chave
                    userRef.child(userId).setValue(user)
                        .addOnCompleteListener { saveTask ->
                            if (saveTask.isSuccessful) {
                                // Sucesso ao salvar os dados do usuário no banco de dados
                                // Agora, redirecione para a tela HomeFragment
                                findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
                            } else {
                                // Falha ao salvar os dados do usuário no banco de dados
                                binding.progressBar.isVisible = false
                                Toast.makeText(requireContext(), "Erro ao salvar os dados do usuário.", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    // Se a criação da conta falhar, exibir mensagem de erro
                    binding.progressBar.isVisible = false
                    handleRegisterError(task.exception) // Chama a função handleRegisterError com a exceção recebida
                }
            }
    }

    private fun handleRegisterError(exception: Exception?) {
        when (exception) {
            is FirebaseAuthInvalidCredentialsException -> {
                Toast.makeText(
                    requireContext(),
                    "Credenciais inválidas",
                    Toast.LENGTH_SHORT
                ).show()
            }
            is FirebaseAuthUserCollisionException -> {
                Toast.makeText(
                    requireContext(),
                    "Usuário já registrado com este e-mail",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                Toast.makeText(
                    requireContext(),
                    "Erro ao registrar usuário",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
