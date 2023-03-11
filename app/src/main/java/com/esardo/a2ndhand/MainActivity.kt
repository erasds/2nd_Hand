package com.esardo.a2ndhand

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.esardo.a2ndhand.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ActivityMainBinding.inflate(layoutInflater).also { binding = it }.root)

        //When the button is clicked it will load the DrawerActivity
        binding.btnLogIn.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }
}