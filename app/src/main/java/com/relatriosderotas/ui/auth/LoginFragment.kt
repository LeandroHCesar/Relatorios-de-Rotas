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
import com.relatriosderotas.R
import com.relatriosderotas.databinding.FragmentLoginBinding
import com.relatriosderotas.helper.KeyboardUtils

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.buttonLogin.setOnClickListener {
            validateAndLogin()
            KeyboardUtils.hideKeyboard(requireContext(), it)
        }

        binding.textViewCreateNewAccount.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.textViewForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_recoverAccountFragment)
        }
    }

    private fun validateAndLogin() {
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()

        val emailLayout = binding.textInputLayoutEmail
        val passwordLayout = binding.textInputLayoutPassword

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
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                binding.progressBar.isVisible = false
                if (task.isSuccessful) {
                    navigateToHome()
                } else {
                    showLoginError()
                }
            }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun navigateToHome() {
        val action = LoginFragmentDirections.actionLoginFragmentToHomeFragment()
        findNavController().navigate(action)
    }

    private fun showLoginError() {
        Toast.makeText(
            requireContext(),
            "Falha no login. Verifique o e-mail e a senha.",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
