package com.relatriosderotas.adapter

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.relatriosderotas.helper.RotaData

object DatabaseHelper {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Obtém o UID do usuário autenticado
    private val userId: String? = auth.currentUser?.uid

    fun getRotasFromDatabase(callback: (List<RotaData>) -> Unit) {
        userId?.let { uid ->
            val rotasRef: DatabaseReference = database.reference.child("meus_apps").child("relatorio_de_rotas")
                .child("users").child(uid).child("rotas")

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

    // Add methods for saving data to the database if needed
}
