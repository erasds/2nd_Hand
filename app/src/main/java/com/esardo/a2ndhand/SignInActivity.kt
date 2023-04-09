package com.esardo.a2ndhand

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.esardo.a2ndhand.databinding.ActivitySignInBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

private const val PICK_IMAGE_REQUEST_CODE = 1
class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding

    val storage = FirebaseStorage.getInstance()
    val db = FirebaseFirestore.getInstance()
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ActivitySignInBinding.inflate(layoutInflater).also { binding = it }.root)

        binding.ibUpUserPhoto.setOnClickListener {
            selectImage()
        }

        binding.btnSignIn.setOnClickListener {
            registerUser()
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }
    //To save the image path into a variable
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
        }
    }

    private fun registerUser() {
        var picture = ""
        val userName = binding.etUser.text.toString()
        val password = binding.etPassword.text.toString()
        val name = binding.etName.text.toString()
        val surname = binding.etSurname.text.toString()
        val mail = binding.etMail.text.toString()
        val phone = binding.etPhone.text.toString()
        val town = binding.etTown.text.toString()
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
                    lifecycleScope.launch {
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
                                //User registration completed
                                finish()
                            }
                            .addOnFailureListener {
                                //Error
                                showMessage("Error al intentar crear el Usuario")
                            }
                    }
                }
            }
            uploadTask.addOnFailureListener {
                // Manejar error en caso de falla en la subida de imagen
            }
        }

    }

    private fun showMessage(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }
}