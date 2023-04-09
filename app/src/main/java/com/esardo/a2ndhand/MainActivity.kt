package com.esardo.a2ndhand

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.esardo.a2ndhand.databinding.ActivityMainBinding
import com.esardo.a2ndhand.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var userRef: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_2ndHand)
        setContentView(ActivityMainBinding.inflate(layoutInflater).also { binding = it }.root)

        binding.btnLogIn.setOnClickListener {
            //Check if User and Password exists in database
            val userCol = FirebaseFirestore.getInstance().collection("User")
            val query = userCol.whereEqualTo("User", binding.etUserName.text.toString())

            query.get().addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    val users = task.result
                    if (!users.isEmpty) {
                        val user = users.first()
                        val password = user.getString("Password")
                        if (password == binding.etPass.text.toString()) {
                            //User exists and Password is correct
                            //Update IsOnline to true
                            val userId = user.id
                            userRef = User(userId)
                            userCol.document(userId).set(
                                hashMapOf(
                                    "IsOnline" to true
                                ), SetOptions.merge()
                            )
                            //Start HomeActivity and send UserId
                            val intent = Intent(this, HomeActivity::class.java).apply {
                                putExtra("object", userRef)
                            }
                            startActivity(intent)
                        } else {
                            //Wrong Password
                            showMessage("Contraseña incorrecta")
                        }
                    } else {
                        //User doesn't exists
                        showMessage("Usuario no registrado")
                    }
                } else {
                    //Error
                    showMessage("Error de conexión")
                }
            }
        }

        binding.tvSignIn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }

    private fun showMessage(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }
}