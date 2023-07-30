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
import com.google.firebase.ktx.Firebase
import com.relatriosderotas.R
import com.relatriosderotas.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

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
        auth = Firebase.auth
        initClicks()
    }

    private fun initClicks() {
        binding.buttonRegister.setOnClickListener { validateData() }
    }

    private fun validateData() {
        val name = binding.editTextNome.text.toString().trim()
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()

        if (name.isEmpty()) {
            binding.editTextNome.error = "Informe seu nome"
        } else if (email.isEmpty()) {
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
                binding.progressBar.isVisible = false
                if (task.isSuccessful) {
                    findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
                } else {
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
