package com.relatriosderotas.ui.auth
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.relatriosderotas.R
import com.relatriosderotas.databinding.FragmentRecoverAccountBinding
import com.relatriosderotas.helper.KeyboardUtils

class RecoverAccountFragment : Fragment() {

    private var _binding: FragmentRecoverAccountBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecoverAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        initClicks()
    }

    private fun initClicks() {
        binding.buttonRecover.setOnClickListener {
            validateData()
            KeyboardUtils.hideKeyboard(requireContext(), it)
        }
    }

    private fun validateData() {
        val email = binding.editTextEmail.text.toString().trim()

        if (email.isEmpty()) {
            binding.editTextEmail.error = "Informe seu e-mail"
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextEmail.error = "E-mail inválido"
        } else {
            binding.progressBar.isVisible = true
            recoverAccountUser(email)
        }
    }

    private fun recoverAccountUser(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener(requireActivity()) { task ->
                binding.progressBar.isVisible = false
                if (task.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Enviamos um link para seu e-mail",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigate(R.id.action_recoverAccountFragment_to_loginFragment)
                } else {
                    handleRecoverAccountError(task.exception)
                }
            }
    }

    private fun handleRecoverAccountError(exception: Exception?) {
        when (exception) {
            is FirebaseAuthInvalidUserException -> {
                Toast.makeText(
                    requireContext(),
                    "Usuário não encontrado",
                    Toast.LENGTH_SHORT
                ).show()
            }
            is FirebaseAuthInvalidCredentialsException -> {
                Toast.makeText(
                    requireContext(),
                    "Credenciais inválidas",
                    Toast.LENGTH_SHORT
                ).show()
            }
            is FirebaseAuthRecentLoginRequiredException -> {
                Toast.makeText(
                    requireContext(),
                    "É necessário fazer login novamente",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                Toast.makeText(
                    requireContext(),
                    "Erro ao recuperar a conta",
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
