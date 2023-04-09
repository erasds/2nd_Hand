package com.esardo.a2ndhand

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.esardo.a2ndhand.databinding.FragmentProductBinding
import com.esardo.a2ndhand.model.Product
import com.esardo.a2ndhand.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class ProductFragment : Fragment() {
    private lateinit var _binding: FragmentProductBinding
    private val binding get() = _binding!!

    private lateinit var product : Product

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProductBinding.inflate(inflater, container, false)
        // Obtain NavController Object
        val navController = findNavController()
        //Obtain our own User reference
        val userRef = activity?.intent?.getSerializableExtra("object") as? User

        // Obtains object Product from the arguments
        product = requireArguments().getSerializable("objeto") as Product
        //Product info
        val ivPictures = binding.ivPictures
        val tvProdName = binding.tvProdName
        val tvPrice = binding.tvPrice
        val tvCategory = binding.tvCategory
        val tvDescription = binding.tvDescription

        //***ESTO TENGO QUE ADAPTARLO PARA QUE MUESTRE MAS DE UNA FOTO, O SEA, QUE SE PUEDA NAVEGAR ENTRE ELLAS***
        val productPic = product.Picture.Pic1
        if(productPic != "") {
            Picasso.get().load(productPic).placeholder(R.drawable.prueba).error(R.drawable.prueba).into(ivPictures)
        }
        tvProdName.text = product.Name
        //Format price erasing decimals if it's value is 0
        tvPrice.text = product.Price.toString().replace(".0", "") + " €"
        tvDescription.text = product.Description
        //***METER LA FECHA DE PUBLICACIÓN POR ALGÚN LADO***

        //Get Category name by its Id
        val categoryId = product.CategoryId
        if (categoryId != null){
            val categoryDoc = FirebaseFirestore.getInstance().collection("Category").document(categoryId)
            categoryDoc.get().addOnSuccessListener { category ->
                if (category != null) {
                    tvCategory.text = category.getString("Name")
                }
            }
        }

        //User info
        val ivProfilePicture = binding.ivProfilePicture
        val tvUserName = binding.tvUserName
        val tvUbication = binding.tvUbication
        //tvRanking??

        //Get User by its Id
        val userId = product.UserId
        if (userId != null){
            val userDoc = FirebaseFirestore.getInstance().collection("User").document(userId)
            userDoc.get().addOnSuccessListener { user ->
                if (user != null) {
                    val profPicture = user.getString("Picture")
                    if(profPicture != ""){
                        Picasso.get().load(profPicture).placeholder(R.drawable.prueba).error(R.drawable.prueba).into(ivProfilePicture)
                    }
                    tvUserName.text = user.getString("User")
                    //Get Town name by its Id
                    val townId = user.getString("TownId")
                    if (townId != null){
                        val townDoc = FirebaseFirestore.getInstance().collection("Town").document(townId)
                        townDoc.get().addOnSuccessListener { town ->
                            if (town != null) {
                                tvUbication.text = town.getString("Name")
                            }
                        }
                    }
                    //tvRanking
                }
            }
        }
        binding.llSeller.setOnClickListener {
            //***PUEDE QUE ESTE IF FINALMENTE NO SEA NECESARIO***
            if (userRef != null) {
                if (userId != userRef.id){
                    val bundle = Bundle()
                    bundle.putString("user", userId)
                    // Navigates to ProfileFragment and pass the userId as an argument
                    view?.let { Navigation.findNavController(it) }
                        ?.navigate(R.id.action_productFragment_to_profileFragment, bundle)
                } else{
                    // Navigates to our own Profile
                    navController.navigate(R.id.action_productFragment_to_profileFragment)
                }
            }
        }

        binding.btnSendMessage.setOnClickListener {

        }
        return binding.root
    }
}