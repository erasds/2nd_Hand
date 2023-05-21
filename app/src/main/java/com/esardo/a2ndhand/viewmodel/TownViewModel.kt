package com.esardo.a2ndhand.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TownViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    //Para obtener los nombres de las ciudades
    suspend fun getTowns(): List<String> {
        val townCol = db.collection("Town").orderBy("Name").get().await()
        val towns = mutableListOf<String>()
        for (document in townCol.documents) {
            val town = document.getString("Name")
            town?.let {
                towns.add(it)
            }
        }
        return towns
    }
}