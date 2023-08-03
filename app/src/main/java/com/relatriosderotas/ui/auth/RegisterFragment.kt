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
import com.relatriosderotas.databinding.FragmentRegisterBinding
import com.relatriosderotas.helper.KeyboardUtils
import com.relatriosderotas.helper.UserInformation

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private var database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val databaseRef: DatabaseReference = database.reference
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

        // Inicialize a instância do Firebase Auth
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
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()
        val userName = binding.editTextUserName.text.toString().trim() // Novo campo: Nome de Usuário

        if (email.isEmpty()) {
            binding.editTextEmail.error = "Informe seu e-mail"
            return
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextEmail.error = "E-mail inválido"
            return
        }

        if (password.isEmpty()) {
            binding.editTextPassword.error = "Informe sua senha"
            return
        } else if (password.length < 8) {
            binding.editTextPassword.error = "A senha deve conter pelo menos 8 caracteres"
            return
        } else if (!password.any { it.isUpperCase() }) {
            binding.editTextPassword.error = "A senha deve conter pelo menos 1 letra maiúscula"
            return
        }

        if (userName.isEmpty()) { // Novo campo: Nome de Usuário
            binding.editTextUserName.error = "Informe seu Nome de Usuário" // Novo campo: Nome de Usuário
            return
        }

        binding.progressBar.isVisible = true
        registerUser(email, password, userName) // Passa o campo "Nome de Usuário" para o método registerUser
    }

    private fun registerUser(email: String, password: String, userName: String) {
        if (userName.isEmpty()) {
            binding.editTextUserName.error = "Informe seu Nome de Usuário"
            return
        }

        binding.progressBar.isVisible = true
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                binding.progressBar.isVisible = false
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid

                    // Salvar os dados do usuário no nó "users"
                    userId?.let {
                        val userInformation = UserInformation(userId, email, userName) // Adiciona o campo "Nome de Usuário"
                        val userRef = databaseRef.child("users").child(userId)
                        userRef.setValue(userInformation)
                            .addOnSuccessListener {
                                // Sucesso ao salvar os dados do usuário
                                // Redirecionar para a tela HomeFragment com o ID do usuário
                                val action = RegisterFragmentDirections.actionRegisterFragmentToHomeFragment(userId)
                                findNavController().navigate(action)
                            }
                            .addOnFailureListener {
                                // Falha ao salvar os dados do usuário
                                Toast.makeText(
                                    requireContext(),
                                    "Erro ao salvar os dados do usuário.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                } else {
                    // Se a criação da conta falhar, exibir mensagem de erro
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
