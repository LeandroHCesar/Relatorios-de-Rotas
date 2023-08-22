package com.relatriosderotas

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        // Desativar o cache do Firebase
        FirebaseDatabase.getInstance().setPersistenceEnabled(false)
    }
}