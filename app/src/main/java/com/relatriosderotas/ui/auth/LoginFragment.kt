package com.relatriosderotas.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.relatriosderotas.R
import com.relatriosderotas.databinding.FragmentLoginBinding
import com.relatriosderotas.helper.KeyboardUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

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
        auth = Firebase.auth
        initClicks()
    }

    private fun initClicks() {
        binding.buttonLogin.setOnClickListener {
            validateData()
            // Esconder o teclado após o clique do botão
            KeyboardUtils.hideKeyboard(requireContext(), it)
        }

        binding.textViewCreateNewAccount.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.textViewForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_recoverAccountFragment)
        }
    }

    private fun validateData() {
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()

        val emailLayout = binding.textInputLayoutEmail
        val passwordLayout = binding.textInputLayoutPassword

        if (email.isEmpty()) {
            emailLayout.error = "Informe seu e-mail"
            return
        } else {
            emailLayout.error = null
        }

        if (!isValidEmail(email)) {
            emailLayout.error = "E-mail inválido"
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

        // Executar a função checkIfUserExistsInDatabase de forma assíncrona
        // dentro da CoroutineScope do Fragment.
        // Aqui usamos o launch para executar a função de forma assíncrona.
        // Utilizamos o contexto Dispatchers.IO para realizar a consulta ao banco de dados
        // de forma não bloqueante.
        // O resultado é obtido através da função withContext.
        // Com o resultado em mãos, podemos realizar o login ou redirecionar para a tela de cadastro.
        requireActivity().lifecycleScope.launch(Dispatchers.IO) {
            val userExists = checkIfUserExistsInDatabase(email)
            withContext(Dispatchers.Main) {
                if (userExists) {
                    loginUser(email, password)
                } else {
                    binding.progressBar.isVisible = false
                    Toast.makeText(
                        requireContext(),
                        "Usuário não cadastrado. Faça o cadastro.",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun loginUser(email: String, password: String) {
        binding.progressBar.isVisible = true
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                binding.progressBar.isVisible = false
                if (task.isSuccessful) {
                    // Redirecionar para a HomeFragment
                    val action = LoginFragmentDirections.actionLoginFragmentToHomeFragment()
                    findNavController().navigate(action)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Falha no login. Verifique o e-mail e a senha.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private suspend fun checkIfUserExistsInDatabase(email: String): Boolean {
        val usersRef = Firebase.database.reference.child("users")

        return try {
            val dataSnapshot = usersRef.orderByChild("email").equalTo(email).get().await()
            dataSnapshot.exists()
        } catch (e: Exception) {
            // Lidar com erros de consulta, se necessário
            false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
