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
import kotlinx.coroutines.runBlocking
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

        // Autenticar o usuário diretamente com o Firebase
        binding.progressBar.isVisible = true
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                binding.progressBar.isVisible = false
                if (task.isSuccessful) {
                    // Sucesso ao autenticar, redirecionar para a HomeFragment
                    val action = LoginFragmentDirections.actionLoginFragmentToHomeFragment()
                    findNavController().navigate(action)
                } else {
                    // Falha na autenticação, exibir mensagem de erro
                    Toast.makeText(
                        requireContext(),
                        "Falha no login. Verifique o e-mail e a senha.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    /*private fun validateData() {
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

        // Verificar se o usuário já está cadastrado no banco de dados
        binding.progressBar.isVisible = true
        lifecycleScope.launch(Dispatchers.IO) {
            val userExists = checkIfUserExistsInDatabase(email)
            withContext(Dispatchers.Main) {
                binding.progressBar.isVisible = false

                if (userExists) {
                    // Usuário cadastrado, realizar o login
                    loginUser(email, password)
                } else {
                    // Usuário não cadastrado, redirecionar para a tela de registro com os dados preenchidos
                    val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment(email, password)
                    findNavController().navigate(action)
                }
            }
        }
    }
    */

    /*private suspend fun checkIfUserExistsInDatabase(email: String): Boolean {
        val usersRef = Firebase.database.reference.child("users")

        return try {
            val dataSnapshot = usersRef.orderByChild("email").equalTo(email).limitToFirst(1).get().await()
            dataSnapshot.exists()
        } catch (e: Exception) {
            // Lidar com erros de consulta, se necessário
            false
        }
    }*/

    /*private fun navigateToRegister(email: String, password: String) {
        val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment(email, password)
        findNavController().navigate(action)
    }*/

    /*private fun loginUser(email: String, password: String) {
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
*/

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
