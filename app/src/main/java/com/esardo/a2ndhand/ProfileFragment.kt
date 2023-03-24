package com.esardo.a2ndhand

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.esardo.a2ndhand.databinding.FragmentProfileBinding
import com.esardo.a2ndhand.model.User
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {
    private lateinit var _binding: FragmentProfileBinding
    private val binding get() = _binding!!

    private lateinit var userRef: User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val userRef = activity?.intent?.getSerializableExtra("object") as? User
        val userId = userRef?.id
        //Load user data
        val points = 0
        val picture = ""
        if (userId != null) {
            val userDoc = FirebaseFirestore.getInstance().collection("User").document(userId)
            userDoc.get().addOnSuccessListener { user ->
                if (user != null) {
                    val userName = user.getString("User")
                    binding.tvUserName.text = userName
                    val townId = user.getString("TownId")
                    if (townId != null){
                        val townDoc = FirebaseFirestore.getInstance().collection("Town").document(townId)
                        townDoc.get().addOnSuccessListener { town ->
                            if (town != null) {
                                val townName = town.getString("Name")
                                binding.tvUbication.text = townName
                            }
                        }
                    }
                }
            }
        }

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