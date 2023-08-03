package com.relatriosderotas.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.relatriosderotas.R
import com.relatriosderotas.databinding.FragmentHomeBinding
import com.relatriosderotas.helper.User

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var userRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar a Toolbar
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        binding.toolbar.title = "Home" // Defina o título da Toolbar conforme desejado

        // Inicializar Firebase Auth
        auth = Firebase.auth
        val userId = auth.currentUser?.uid
        if (userId != null) {
            // Referência para o banco de dados "users" no Firebase Realtime Database
            val database = FirebaseDatabase.getInstance()
            userRef = database.getReference("users").child(userId)
        }

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding != null && isAdded) { // Verifique se o fragmento está vinculado à atividade
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        val userName = user.name
                        binding.textViewUserName.text = "Seja bem vindo, $userName!"
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (isAdded) { // Verifique se o fragmento está vinculado à atividade
                    Toast.makeText(requireContext(), "Erro ao recuperar os dados do usuário.", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                // Action goes here
                logoutUser()
                Toast.makeText(requireContext(), "sair", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun logoutUser(){
        Firebase.auth.signOut()
        findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
    }

    override fun onStart() {
        super.onStart()

        // Verificar se o usuário está logado
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // Caso o usuário não esteja logado, navegue para o LoginFragment
            findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
