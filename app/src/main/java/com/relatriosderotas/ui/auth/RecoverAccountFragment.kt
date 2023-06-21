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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.relatriosderotas.R
import com.relatriosderotas.databinding.FragmentRecoverAccountBinding

class RecoverAccountFragment : Fragment() {

    private var _binding: FragmentRecoverAccountBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentRecoverAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Initialize Firebase Auth
        auth = Firebase.auth
        initClicks()
    }

    private fun initClicks() {
        binding.buttonRecover.setOnClickListener { validateData() }
    }

    private fun validateData() {
        val email = binding.editTextEmail.text.toString().trim()

        if (email.isNotEmpty()) {
            //binding.buttonRegister.isEnabled = true
            binding.progressBar.isVisible = true
            recoverAccountUser(email)
        } else {
            Toast.makeText(requireContext(), "informe seu e-mail", Toast.LENGTH_SHORT).show()
        }
    }

    private fun recoverAccountUser(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(
                        requireContext(),
                        "Enviamos um link para seu email",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.progressBar.isVisible = false
                    // Sign in success, update UI with the signed-in user's information
                    findNavController().navigate(R.id.action_recoverAccountFragment_to_loginFragment)
                } else {
                    // If sign in fails, display a message to the user.
                    binding.progressBar.isVisible = false
                    Toast.makeText(requireContext(), "error", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}