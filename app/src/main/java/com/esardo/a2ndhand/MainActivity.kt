package com.esardo.a2ndhand

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.esardo.a2ndhand.databinding.ActivityMainBinding
import com.esardo.a2ndhand.model.User
import com.esardo.a2ndhand.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var userRef: User
    var userId: String? = ""

    private val mAuth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    private lateinit var viewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_2ndHand)
        setContentView(ActivityMainBinding.inflate(layoutInflater).also { binding = it }.root)

        viewModel = ViewModelProvider(this)[UserViewModel::class.java]

        binding.btnLogIn.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPass.text.toString()
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if(task.isSuccessful) {
                        viewModel.logIn(email) { user ->
                            userId = user
                            userRef = User(user)
                            //Start HomeActivity and send UserId
                            val intent = Intent(this, HomeActivity::class.java).apply {
                                putExtra("object", userRef)
                            }
                            startActivity(intent)
                        }
                    } else {
                        showMessage("Email/Contraseña incorrectos")
                    }
                }
        }

        binding.tvSignIn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }

    /*override fun onDestroy() {
        // Actualizar campo IsOnline a false al destruir la actividad
        userId?.let {
            db.collection("User").document(it)
                .set(
                    hashMapOf(
                        "IsOnline" to false
                    ), SetOptions.merge()
                ).addOnSuccessListener {
                    Log.d(ContentValues.TAG, "Se ha actualizado el campo IsOnline")
                    super.onDestroy()
                }.addOnFailureListener { e ->
                    Log.d(ContentValues.TAG, "Se ha producido un error al intentar actualizar el campo",e)
                }
        }
    }*/

    private fun showMessage(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }
}