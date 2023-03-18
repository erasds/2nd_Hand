package com.esardo.a2ndhand

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.esardo.a2ndhand.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_2ndHand)
        setContentView(ActivityMainBinding.inflate(layoutInflater).also { binding = it }.root)

        binding.btnLogIn.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        binding.tvSignIn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }
}