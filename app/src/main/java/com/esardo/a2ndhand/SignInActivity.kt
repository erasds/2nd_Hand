package com.esardo.a2ndhand

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.esardo.a2ndhand.databinding.ActivitySignInBinding

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ActivitySignInBinding.inflate(layoutInflater).also { binding = it }.root)

        binding.btnSignIn.setOnClickListener {
            //Enviar datos
            finish()
        }
    }
}