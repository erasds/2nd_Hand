package com.esardo.a2ndhand

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.esardo.a2ndhand.databinding.ActivityUploadProductBinding

class UploadProduct : AppCompatActivity() {
    private lateinit var binding: ActivityUploadProductBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ActivityUploadProductBinding.inflate(layoutInflater).also { binding = it }.root)

        binding.btnUpload.setOnClickListener {
            //Insertar datos
            finish()
        }
    }
}