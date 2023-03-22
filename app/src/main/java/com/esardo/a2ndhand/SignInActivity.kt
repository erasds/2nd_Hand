package com.esardo.a2ndhand

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.esardo.a2ndhand.databinding.ActivitySignInBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ActivitySignInBinding.inflate(layoutInflater).also { binding = it }.root)

        binding.btnSignIn.setOnClickListener {
            val userName = binding.etUser.text.toString()
            val password = binding.etPassword.text.toString()
            val name = binding.etName.text.toString()
            val surname = binding.etSurname.text.toString()
            val mail = binding.etMail.text.toString()
            val phone = binding.etPhone.text.toString()
            //Check if User and Password exists in database
            var townId = "";
            val townCol = FirebaseFirestore.getInstance().collection("Town")
            val query = townCol.whereEqualTo("Name", binding.etTown.text.toString())
            query.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val towns = task.result
                    if (!towns.isEmpty) {
                        val town = towns.first()
                        townId = town.id
                    } else {
                        showMessage("Ciudad incorrecta")
                    }
                } else {
                    showMessage("Error conexión")
                }
            }

            //townId
            val registerDate = Timestamp(Date())

            //Create new User with the data obtained
            val db = FirebaseFirestore.getInstance()
            val user = hashMapOf(
                "User" to userName,
                "Password" to password,
                "Name" to name,
                "Surname" to surname,
                "Mail" to mail,
                "Phone" to phone,
                "TownId" to townId,
                "RegisterDate" to registerDate
            )

            db.collection("User").add(user)
                .addOnSuccessListener { documentReference ->
                    //User registration completed
                    finish()
                }
                .addOnFailureListener { exception ->
                    //Error
                    showMessage("Error al intentar crear el Usuario")
                }
        }
    }

    private fun showMessage(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }
}