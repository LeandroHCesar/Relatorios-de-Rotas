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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.relatriosderotas.databinding.FragmentRegisterBinding
import com.relatriosderotas.helper.KeyboardUtils
import com.relatriosderotas.helper.UserDetails

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
        auth = FirebaseAuth.getInstance()
        setupClickListeners()
        fillFieldsFromArguments()
    }

    private fun setupClickListeners() {
        binding.buttonRegister.setOnClickListener {
            validateAndRegister()
            KeyboardUtils.hideKeyboard(requireContext(), it)
        }
    }

    private fun fillFieldsFromArguments() {
        val args = arguments
        if (args != null) {
            val email = args.getString("email")
            val password = args.getString("password")
            if (!email.isNullOrEmpty() && !password.isNullOrEmpty()) {
                binding.editTextEmail.setText(email)
                binding.editTextPassword.setText(password)
            }
        }
    }

    private fun validateAndRegister() {
        val name = binding.editTextUserName.text.toString().trim()
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()

        val nameLayout = binding.textInputLayoutUserName
        val emailLayout = binding.textInputLayoutEmail
        val passwordLayout = binding.textInputLayoutPassword

        if (name.isEmpty()) {
            nameLayout.error = "Informe seu nome"
            return
        } else {
            nameLayout.error = null
        }

        if (email.isEmpty() || !isValidEmail(email)) {
            emailLayout.error = "Informe um e-mail válido"
            return
        } else {
            emailLayout.error = null
        }

        if (password.isEmpty()) {
            passwordLayout.error = "Informe sua senha"
            return
        } else {
            passwordLayout.error = null
        }

        binding.progressBar.isVisible = true
        registerUser(email, password, name)
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun registerUser(email: String, password: String, name: String) {

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->

                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid

                    val userDetailsRef: DatabaseReference = database.reference
                        .child("meus_apps")
                        .child("relatorio_de_rotas")
                        .child("userDetails")
                        .child(userId.toString())

                    val userDetails = UserDetails(userId!!, name, email, /* outros campos */)

                    userDetailsRef.setValue(userDetails)
                        .addOnCompleteListener { userDetailsSaveTask ->
                            if (userDetailsSaveTask.isSuccessful) {
                                navigateToHome()
                            } else {
                                handleRegisterError(userDetailsSaveTask.exception)
                            }
                        }
                } else {
                    handleRegisterError(task.exception)
                }
            }
    }

    private fun navigateToHome() {
        val action = RegisterFragmentDirections.actionRegisterFragmentToHomeFragment()
        findNavController().navigate(action)
    }

    private fun handleRegisterError(exception: Exception?) {
        val errorMessage = when (exception) {
            is FirebaseAuthInvalidCredentialsException -> "Credenciais inválidas"
            is FirebaseAuthUserCollisionException -> "Usuário já registrado com este e-mail"
            else -> "Erro ao registrar usuário"
        }
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
