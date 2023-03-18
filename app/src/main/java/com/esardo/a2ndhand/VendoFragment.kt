package com.esardo.a2ndhand

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.esardo.a2ndhand.databinding.FragmentVendoBinding

class VendoFragment : Fragment() {
    private lateinit var _binding: FragmentVendoBinding
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVendoBinding.inflate(inflater, container, false)
        binding.btnNewProduct.setOnClickListener {
            //Enviar que es Venta
            val intent = Intent(activity, UploadProduct::class.java)
            startActivity(intent)
        }
        return binding.root
    }
}