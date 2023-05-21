package com.esardo.a2ndhand.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CategoryViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    //Función que obtiene los nombres de las categorías
    suspend fun getCategories(): List<String> {
        val categoryCol = db.collection("Category").orderBy("Name").get().await()
        val categories = mutableListOf<String>()
        for (document in categoryCol.documents) {
            val category = document.getString("Name")
            category?.let {
                categories.add(it)
            }
        }
        return categories
    }
}