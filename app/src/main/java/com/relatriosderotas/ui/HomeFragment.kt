package com.relatriosderotas.ui

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.relatriosderotas.R
import com.relatriosderotas.adapter.RotasAdapter
import com.relatriosderotas.databinding.FragmentHomeBinding
import com.relatriosderotas.helper.DatabaseHelper
import com.relatriosderotas.helper.RotaData
import com.relatriosderotas.helper.Users

class HomeFragment : Fragment(), ConnectivityReceiver.OnConnectivityChangeListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var recyclerView: RecyclerView
    private lateinit var connectivityReceiver: ConnectivityReceiver
    private lateinit var progressBar: ProgressBar
    private lateinit var calendarView: MaterialCalendarView

    private lateinit var auth: FirebaseAuth
    private var database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val databaseRef: DatabaseReference = database.reference
    private lateinit var userRef: DatabaseReference
    private lateinit var valueEventListenerUser: ValueEventListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when {
            _binding != null -> {
                Log.d("HomeFragment", "Inside when block")
                // Inicializar a propriedade progressBar
                progressBar = binding.progressBar
                val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
                connectivityReceiver = ConnectivityReceiver(this)
                requireContext().registerReceiver(connectivityReceiver, filter)
                initFirebaseAuth()
                initToolbarAndNavigation()
                configCalendarView()
            }
        }
    }

    private fun initFirebaseAuth() {
        // Inicializar Firebase Auth
        auth = Firebase.auth
        val userId = auth.currentUser?.uid
        if (userId != null) {
            // Referência para o banco de dados "users" no Firebase Realtime Database
            userRef =
                databaseRef.child("meus_apps")
                    .child("relatorio_de_rotas")
                    .child("usuarios")
                    .child(userId)
                    .child("user")
        }
    }

    override fun onConnectivityChange(isConnected: Boolean) {
        onNetworkConnectionChanged(isConnected)
    }

    private fun onNetworkConnectionChanged(isConnected: Boolean) {
        if (isConnected) {
            binding.recyclerView.visibility = View.VISIBLE
            binding.noInternetTextView.visibility = View.GONE
            setupRecyclerView()
        } else {
            binding.recyclerView.visibility = View.GONE
            binding.noInternetTextView.visibility = View.VISIBLE
        }
    }

    private fun initToolbarAndNavigation() {
        drawerLayout = requireView().findViewById(R.id.drawerLayout)
        // Configurar a Toolbar
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_drawer_menu) // Substitua pelo ícone do Drawer desejado
            title = "Home"
            binding.toolbar.setTitleTextAppearance(
                requireContext(),
                R.style.ToolbarTitleStyle
            )
            setHasOptionsMenu(true)
            binding.toolbar.inflateMenu(R.menu.menu)
            binding.toolbar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_search -> {
                        // Navigate to settings screen.
                        Toast.makeText(
                            requireContext(),
                            "Search clicked toolbar.",
                            Toast.LENGTH_SHORT
                        ).show()
                        true
                    }

                    else -> false
                }
            }
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

                R.id.nav_datas -> {
                    val navController = findNavController()
                    navController.popBackStack(
                        R.id.homeFragment,
                        false
                    ) // Remove o fragmento anterior da pilha
                    navController.navigate(R.id.action_homeFragment_to_personalDataFragment)
                    drawerLayout.closeDrawer(GravityCompat.START) // Fechar o Drawer após o clique
                    true // Indicar que o clique foi tratado com sucesso
                }

                R.id.nav_rota -> {
                    val navController = findNavController()
                    navController.popBackStack(
                        R.id.homeFragment,
                        false
                    ) // Remove o fragmento anterior da pilha
                    navController.navigate(R.id.action_homeFragment_to_rotasFragment)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true// Indicar que o clique foi tratado com sucesso
                }
                // Outros itens do menu aqui, se houver
                else -> false // Indicar que o clique não foi tratado
            }
        }

    }

    private fun initUser() {
        valueEventListenerUser = userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding != null && isAdded) { // Verifique se o fragmento está vinculado à atividade
                    val user = snapshot.getValue(Users::class.java)
                    if (user != null) {
                        val userName = user.userName
                        val userEmail = user.email
                        Log.d("UserDetails", "User Name: $userName")
                        Log.d("UserDetails", "User Email: $userEmail")
                        // Atualizar o texto nas TextViews no nav_header_main
                        val headerView = binding.navigationView.getHeaderView(0)
                        headerView.findViewById<TextView>(R.id.textViewNome).text = userName
                        headerView.findViewById<TextView>(R.id.textViewEmail).text = userEmail
                        val textName = binding.textName
                        textName.text = "Olá, ${userName}"
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (isAdded) { // Verifique se o fragmento está vinculado à atividade
                    Toast.makeText(
                        requireContext(),
                        "Erro ao recuperar os dados do usuário. $error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    private fun setupRecyclerView() {
        recyclerView = requireView().findViewById(R.id.recyclerView)

        // Mostra o ProgressBar enquanto carrega os dados
        progressBar.visibility = View.VISIBLE

        DatabaseHelper.getRotasFromDatabase { rotasList ->
            val layoutManager = LinearLayoutManager(requireContext())
            recyclerView.layoutManager = layoutManager
            // Ordena a lista pelo campo "data" decrescentemente sortedByDescending
            // Ordena a lista pelo campo "data" decrescentemente sortedBy
            val sortedRotasList = rotasList.sortedByDescending { it.data }

            val rotasAdapter = RotasAdapter(requireContext(), sortedRotasList) { rota, position ->
                openEditForm(rota, position) // Passar a posição também
            }
            recyclerView.adapter = rotasAdapter
            // Usar um ViewTreeObserver para esperar a renderização da UI e, em seguida, esconder a progressBar
            recyclerView.viewTreeObserver.addOnGlobalLayoutListener {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun configCalendarView() {
        Log.d("HomeFragment", "configCalendarView called")

        calendarView = binding.calendarView
        calendarView.setOnMonthChangedListener { widget, date ->
            val selectedMonth = date.month
            val selectedYear = date.year
            Log.d("data", "calendarView: ${date.month} ${date.year}")
        }
    }

    private fun openEditForm(rota: RotaData, position: Int) {
        Log.d("Debug", "Opening edit form for rota: $rota at position: $position")

        val fragmentRotas = RotasFragment()
        val args = Bundle()
        args.putParcelable("rota", rota)
        args.putInt("position", position)
        fragmentRotas.arguments = args
        fragmentRotas.isEditMode = true

        // Abra o FragmentRotas
        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.nav_host_fragment, fragmentRotas)
        fragmentTransaction.addToBackStack(null) // Para adicionar à pilha de fragmentos
        fragmentTransaction.commit()

        // Limpar a pilha de backstack
        val fragmentCount = fragmentManager.backStackEntryCount
        for (i in 0 until fragmentCount) {
            fragmentManager.popBackStackImmediate()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu, menu)
    }

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
        initUser()
        Log.i("evento", "onStart: listener iniciado")
        // Verificar se o usuário está logado
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // Caso o usuário não esteja logado, navegue para o LoginFragment
            findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
        }
    }

    override fun onStop() {
        super.onStop()
        userRef.removeEventListener(valueEventListenerUser)
        Log.d("evento", "onStop: listener removido")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        requireContext().unregisterReceiver(connectivityReceiver)
    }
}