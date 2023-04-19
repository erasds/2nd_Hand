package com.esardo.a2ndhand.viewmodel

import android.content.ContentValues
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class UserViewModel: ViewModel() {

    val storage = FirebaseStorage.getInstance()
    val db = FirebaseFirestore.getInstance()

    fun registerUser(userName: String,
                     password: String,
                     name: String,
                     surname: String,
                     mail: String,
                     phone: String,
                     town: String,
                     imageUri: Uri?)
    {
        var picture = ""
        val registerDate = Timestamp(Date())
        val isOnline = false
        val lastOnline = Timestamp(Date())
        val storageRef = storage.reference.child("images/${UUID.randomUUID()}")
        if (imageUri != null) {
            val uploadTask = storageRef.putFile(imageUri!!)
            uploadTask.addOnSuccessListener { taskSnapshot ->
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    picture = uri.toString()

                    //Get TownId
                    val townCol = db.collection("Town")
                    val query = townCol.whereEqualTo("Name", town)
                    viewModelScope.launch {
                        val querySnapshot = query.get().await()
                        val docSnapshot = querySnapshot.documents[0]
                        val townId = docSnapshot.id

                        //Create new User with the data obtained
                        val user = hashMapOf(
                            "User" to userName,
                            "Password" to password,
                            "Name" to name,
                            "Surname" to surname,
                            "Mail" to mail,
                            "Phone" to phone,
                            "TownId" to townId,
                            "RegisterDate" to registerDate,
                            "IsOnline" to isOnline,
                            "LastOnline" to lastOnline,
                            "Picture" to picture
                        )

                        db.collection("User").add(user)
                            .addOnSuccessListener {
                                Log.d(ContentValues.TAG, "Usuario registrado correctamente")
                            }
                            .addOnFailureListener { exception ->
                                //Error
                                Log.d(
                                    ContentValues.TAG,
                                    "Se ha producido un error al intentar registrar al usuario",
                                    exception
                                )
                            }
                    }
                }
            }
            uploadTask.addOnFailureListener { exception ->
                // Manejar error en caso de falla en la subida de imagen
                Log.d(ContentValues.TAG, "No se ha podido subir la imagen", exception)
            }
        } else {
            //Get TownId
            val townCol = db.collection("Town")
            townCol.whereEqualTo("Name", town).get()
                .addOnSuccessListener { towns ->
                    var townId = ""
                    for(town in towns) {
                         townId = town.id
                    }

                    //Create new User with the data obtained
                    val user = hashMapOf(
                        "User" to userName,
                        "Password" to password,
                        "Name" to name,
                        "Surname" to surname,
                        "Mail" to mail,
                        "Phone" to phone,
                        "TownId" to townId,
                        "RegisterDate" to registerDate,
                        "IsOnline" to isOnline,
                        "LastOnline" to lastOnline,
                        "Picture" to picture
                    )

                    db.collection("User").add(user)
                        .addOnSuccessListener {
                            Log.d(ContentValues.TAG, "Usuario registrado correctamente")
                        }
                        .addOnFailureListener { exception ->
                            //Error
                            Log.d(
                                ContentValues.TAG,
                                "Se ha producido un error al intentar registrar al usuario",
                                exception
                            )
                        }
                }
        }

    }
}