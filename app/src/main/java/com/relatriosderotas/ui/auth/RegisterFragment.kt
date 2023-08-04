package com.relatriosderotas.ui.auth

import android.os.Bundle
import android.util.Log
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

        // Verificar se existem argumentos recebidos da LoginFragment
        val args = arguments
        if (args != null) {
            val email = args.getString("email")
            val password = args.getString("password")
            if (!email.isNullOrEmpty() && !password.isNullOrEmpty()) {
                // Preencher os campos com o email e senha recebidos
                binding.editTextEmail.setText(email)
                binding.editTextPassword.setText(password)
            }
        }
    }

    private fun initClicks() {
        binding.buttonRegister.setOnClickListener {
            validateData()
            KeyboardUtils.hideKeyboard(requireContext(), it)
        }
    }

    private fun validateData() {
        val name = binding.editTextUserName.text.toString().trim()
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()

        // Verificar se todos os campos estão preenchidos
        if (name.isEmpty()) {
            binding.editTextUserName.error = "Informe seu nome"
            return
        }
        if (email.isEmpty()) {
            binding.editTextEmail.error = "Informe seu e-mail"
            return
        }
        if (!isValidEmail(email)) {
            binding.editTextEmail.error = "E-mail inválido"
            return
        }
        if (password.isEmpty()) {
            binding.editTextPassword.error = "Informe sua senha"
            return
        }
        binding.progressBar.isVisible = true
        registerUser(email, password, name)
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun registerUser(email: String, password: String, name: String) {
        Log.d("RegisterFragment", "Registering user with email: $email, password: $password")
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                binding.progressBar.isVisible = false
                if (task.isSuccessful) {
                    // Sucesso ao registrar o usuário, redirecionar para a HomeFragment
                    val userId = auth.currentUser?.uid

                    // Criar um objeto User com os dados do usuário
                    val user = User(userId!!, name, email)

                    // Referência para o banco de dados "users" no Firebase Realtime Database
                    val userRef: DatabaseReference = database.getReference("users")

                    // Salvando o objeto User no banco de dados com o ID do usuário como chave
                    userRef.child(userId).setValue(user)
                        .addOnCompleteListener { saveTask ->
                            if (saveTask.isSuccessful) {
                                // Sucesso ao registrar o usuário, redirecionar para a HomeFragment
                                Log.d("RegisterFragment", "User registered successfully.")
                                findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
                            } else {
                                // Tratar falhas ao registrar o usuário
                                Log.e("RegisterFragment", "Error registering user:", task.exception)
                                handleRegisterError(task.exception)
                            }
                        }
                } else {
                    // Tratar falhas ao registrar o usuário
                    handleRegisterError(task.exception)
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
