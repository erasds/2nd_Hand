package com.esardo.a2ndhand

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.esardo.a2ndhand.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    private lateinit var _binding: FragmentProfileBinding
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.options_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // When the id of the option is...
        return when (item.itemId) {
            /*R.id.opcion1 -> {
                // Agrega aquí el código que deseas ejecutar cuando se seleccione la opción 1 del menú
                return true
            }
            R.id.opcion2 -> {
                // Agrega aquí el código que deseas ejecutar cuando se seleccione la opción 1 del menú
                return true
            }*/
            else -> return false
        }
    }
}