package com.esardo.a2ndhand.viewmodel

import android.content.ContentValues
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class UserViewModel: ViewModel() {

    private val storage = FirebaseStorage.getInstance()
    val db = FirebaseFirestore.getInstance()
    private val mAuth = FirebaseAuth.getInstance()

    fun registerUser(userName: String,
                     password: String,
                     encryptedPassword: String,
                     name: String,
                     surname: String,
                     mail: String,
                     phone: String,
                     town: String,
                     imageUri: Uri?): LiveData<Boolean>
    {
        val isUserRegisteredSuccessfully = MutableLiveData<Boolean>()
        var picture = ""
        val registerDate = Timestamp(Date())
        val isOnline = false
        val lastOnline = Timestamp(Date())
        val usersRef = db.collection("User")
        val storageRef = storage.reference.child("images/${UUID.randomUUID()}")
        if (imageUri != null) {
            val uploadTask = storageRef.putFile(imageUri)
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
                            "Password" to encryptedPassword,
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
                                mAuth.createUserWithEmailAndPassword(mail, password)
                                    .addOnCompleteListener { task ->
                                        viewModelScope.launch {
                                            if (task.isSuccessful) {
                                                isUserRegisteredSuccessfully.postValue(true)
                                                Log.d(ContentValues.TAG, "Usuario registrado correctamente")
                                            } else {
                                                isUserRegisteredSuccessfully.postValue(false)
                                                Log.d(ContentValues.TAG, "Se ha producido un error al intentar registrar al usuario")
                                            }
                                        }
                                    }
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
                    for (town in towns) {
                        townId = town.id
                    }

                    //Create new User with the data obtained
                    val user = hashMapOf(
                        "User" to userName,
                        "Password" to encryptedPassword,
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

                    usersRef.add(user)
                        .addOnSuccessListener {
                            mAuth.createUserWithEmailAndPassword(mail, password)
                                .addOnCompleteListener { task ->
                                    viewModelScope.launch {
                                        if (task.isSuccessful) {
                                            isUserRegisteredSuccessfully.postValue(true)
                                            Log.d(ContentValues.TAG, "Usuario registrado correctamente")
                                        } else {
                                            isUserRegisteredSuccessfully.postValue(false)
                                            Log.d(
                                                ContentValues.TAG,
                                                "Se ha producido un error al intentar registrar al usuario"
                                            )
                                        }
                                    }
                                }
                        }.addOnFailureListener { exception ->
                            Log.d(ContentValues.TAG, "Se ha producido un error al intentar registrar al usuario",exception)
                        }
                }
        }
        return isUserRegisteredSuccessfully
    }

    fun updateUser(
        userId: String,
        userName: String,
        name: String,
        surname: String,
        phone: String,
        town: String,
        imageUri: Uri?): LiveData<Boolean>
    {
        val isUserUpdatedSuccessfully = MutableLiveData<Boolean>()
        //Obtenemos el townId
        var townId = ""
        var picture = ""
        val townCol = db.collection("Town")
            townCol.whereEqualTo("Name", town).get().addOnSuccessListener { towns ->
                for (town in towns) {
                    townId = town.id
                }
                //Subimos la foto y obtenemos su referencia
                val storageRef = storage.reference.child("images/${UUID.randomUUID()}")
                if (imageUri != null) {
                    val uploadTask = storageRef.putFile(imageUri)
                    uploadTask.addOnSuccessListener { taskSnapshot ->
                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                            picture = uri.toString()

                            //Actualizamos los datos del usuario
                            val userCol = db.collection("User")
                            userCol.document(userId).set(
                                hashMapOf(
                                    "User" to userName,
                                    "Name" to name,
                                    "Surname" to surname,
                                    "Phone" to phone,
                                    "TownId" to townId,
                                    "Picture" to picture
                                ), SetOptions.merge())
                                .addOnSuccessListener {
                                    isUserUpdatedSuccessfully.postValue(true)
                                    Log.d(ContentValues.TAG, "Usuario actualizado con éxtio")
                                }.addOnFailureListener { e ->
                                    isUserUpdatedSuccessfully.postValue(false)
                                    Log.d(ContentValues.TAG, "Se ha producido un error al intentar actualizar el campo",e)
                                }
                        }
                    }
                } else {
                    //Actualizamos los datos del usuario sin la foto
                    val userCol = db.collection("User")
                    userCol.document(userId).set(
                        hashMapOf(
                            "User" to userName,
                            "Name" to name,
                            "Surname" to surname,
                            "Phone" to phone,
                            "TownId" to townId
                        ), SetOptions.merge())
                        .addOnSuccessListener {
                            isUserUpdatedSuccessfully.postValue(true)
                            Log.d(ContentValues.TAG, "Usuario actualizado con éxtio")
                        }.addOnFailureListener { e ->
                            isUserUpdatedSuccessfully.postValue(false)
                            Log.d(ContentValues.TAG, "Se ha producido un error al intentar actualizar el campo",e)
                        }
                }

            }
        return isUserUpdatedSuccessfully
    }

    fun isMailAlreadyRegistered(email: String, callback: (Boolean) -> Unit) {
        var registered = false
        val userCol = db.collection("User")
        userCol.whereEqualTo("Mail", email).get()
            .addOnSuccessListener { users ->
                for (user in users) {
                    registered = true
                    break
                }
                callback(registered)
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error getting documents: ", exception)
                callback(false)
            }
    }

    fun logIn(email: String, callback: (String) -> Unit) {
        //Update user field IsOnline to true and LastOnline with actual date
        val userCol = db.collection("User")
        userCol.whereEqualTo("Mail", email).get()
            .addOnSuccessListener { users ->
                var userId = ""
                for (user in users) {
                    userId = user.id
                }
                userCol.document(userId).set(
                    hashMapOf(
                        "IsOnline" to true,
                        "LastOnline" to Timestamp(Date())
                    ), SetOptions.merge()
                ).addOnSuccessListener {
                    callback(userId)
                }.addOnFailureListener { e ->
                    Log.d(ContentValues.TAG, "Se ha producido un error al intentar actualizar el campo",e)
                }
            }.addOnFailureListener { e ->
                Log.d(ContentValues.TAG, "Se ha producido un error al intentar obtener el usuario",e)
            }
    }


}