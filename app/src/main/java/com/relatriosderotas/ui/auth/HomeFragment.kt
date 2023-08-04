package com.relatriosderotas.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.navigation.NavigationView
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
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        drawerLayout = view.findViewById(R.id.drawerLayout)
        // Inicializar Firebase Auth
        auth = Firebase.auth
        val userId = auth.currentUser?.uid
        if (userId != null) {
            // Referência para o banco de dados "users" no Firebase Realtime Database
            val database = FirebaseDatabase.getInstance()
            userRef = database.getReference("users").child(userId)
        }
        // Configurar a Toolbar
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_drawer_menu) // Substitua pelo ícone do Drawer desejado
            title = "Home"
            binding.toolbar.setTitleTextAppearance(requireContext(), R.style.ToolbarTitleStyle)
        }

        // Configurar o clique no ícone do Drawer
        binding.toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Referência para o NavigationView
        navigationView = binding.navigationView // Já deve estar inicializado

        // Configurar o clique nos itens do Drawer
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    logoutUser()
                    drawerLayout.closeDrawer(GravityCompat.START) // Fechar o Drawer após o clique
                    true // Indicar que o clique foi tratado com sucesso
                }
                R.id.nav_user -> {
                    findNavController().navigate(R.id.action_homeFragment_to_personalDataFragment)
                    drawerLayout.closeDrawer(GravityCompat.START) // Fechar o Drawer após o clique
                    true // Indicar que o clique foi tratado com sucesso
                }
                // Outros itens do menu aqui, se houver
                else -> false // Indicar que o clique não foi tratado
            }
        }

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding != null && isAdded) { // Verifique se o fragmento está vinculado à atividade
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        val userName = user.userName
                        val userEmail = user.email
                        // Atualizar o texto nas TextViews no nav_header_main
                        val headerView = binding.navigationView.getHeaderView(0)
                        headerView.findViewById<TextView>(R.id.textViewNome).text = userName
                        headerView.findViewById<TextView>(R.id.textViewEmail).text = userEmail
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

    // Em seguida, implemente a função logoutUser() que mostra um AlertDialog de confirmação e realiza o logout:
    private fun logoutUser() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmação")
        builder.setMessage("Deseja sair da conta?")
        builder.setPositiveButton("Sim") { _, _ ->
            // O usuário clicou em "Sim", então faça o logout
            auth.signOut()

            // Redirecionar para a tela de login após o logout
            val action = HomeFragmentDirections.actionHomeFragmentToLoginFragment()
            findNavController().navigate(action)
        }
        builder.setNegativeButton("Não") { dialog, _ ->
            // O usuário clicou em "Não", então apenas feche o diálogo
            dialog.dismiss()
        }
        builder.create().show()
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