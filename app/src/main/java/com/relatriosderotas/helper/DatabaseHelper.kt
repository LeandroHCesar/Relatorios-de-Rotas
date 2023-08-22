package com.relatriosderotas.helper

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlin.collections.mutableListOf

object DatabaseHelper {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Referência para o nó "users" dentro da estrutura do banco de dados
    private val usersRef: DatabaseReference = database.reference
        .child("meus_apps")
        .child("relatorio_de_rotas")
        .child("users")

    // Função para buscar rotas no banco de dados
    fun getRotasFromDatabase(callback: (List<RotaData>) -> Unit) {
        val userId = auth.currentUser?.uid

        userId?.let { uid ->
            val rotasRef: DatabaseReference = database.reference.child("meus_apps")
                .child("relatorio_de_rotas").child("rotas").child(uid)

            rotasRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val rotasList = mutableListOf<RotaData>()

                    for (rotaSnapshot in snapshot.children) {
                        val rota = rotaSnapshot.getValue(RotaData::class.java)
                        rota?.let {
                            rotasList.add(it)
                        }
                    }

                    callback(rotasList)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error here
                }
            })
        }
    }

    /*// Função para fazer login
    fun loginUser(email: String, password: String, callback: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true) // Login bem-sucedido
                } else {
                    callback(false) // Falha no login
                }
            }
    }*/

    /*// Função para registrar um novo usuário
    fun registerUser(email: String, password: String, callback: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true) // Registro bem-sucedido
                } else {
                    callback(false) // Falha no registro
                }
            }
    }*/

    // Outras funções para salvar, atualizar e deletar rotas...
}
