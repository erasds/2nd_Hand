package com.esardo.a2ndhand

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.esardo.a2ndhand.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

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
                            //***Pasarle un intent con el id del usuario???***
                            startActivity(Intent(this, HomeActivity::class.java))
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
            //Start SignInActivity
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }

    private fun showMessage(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }
}