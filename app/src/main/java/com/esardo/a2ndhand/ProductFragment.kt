package com.esardo.a2ndhand

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.esardo.a2ndhand.databinding.FragmentProductBinding
import com.esardo.a2ndhand.model.Chat
import com.esardo.a2ndhand.model.Message
import com.esardo.a2ndhand.model.Product
import com.esardo.a2ndhand.model.User
import com.esardo.a2ndhand.viewmodel.ChatViewModel
import com.esardo.a2ndhand.viewmodel.UserViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.squareup.picasso.Picasso

class ProductFragment : Fragment() {
    private lateinit var _binding: FragmentProductBinding
    private val binding get() = _binding!!

    private lateinit var product : Product
    private lateinit var userId: String

    private lateinit var viewModel:ChatViewModel

    val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProductBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        // Obtain NavController Object
        val navController = findNavController()
        //Obtain our own User reference
        val userRef = activity?.intent?.getSerializableExtra("object") as? User

        val userID = userRef?.id
        if (userID != null) {
            userId = userID
        }
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
            val categoryDoc = db.collection("Category").document(categoryId)
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
        val productUserId = product.UserId
        if (productUserId != null){
            val userDoc = db.collection("User").document(productUserId)
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
                        val townDoc = db.collection("Town").document(townId)
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
                if (productUserId != userRef.id){
                    val bundle = Bundle()
                    bundle.putString("user", productUserId)
                    // Navigates to ProfileFragment and pass the productUserId as an argument
                    view?.let { Navigation.findNavController(it) }
                        ?.navigate(R.id.action_productFragment_to_profileFragment, bundle)
                } else{
                    // Navigates to our own Profile
                    navController.navigate(R.id.action_productFragment_to_profileFragment)
                }
            }
        }

        binding.btnSendMessage.setOnClickListener {
            viewModel.createNewChat(userId, productUserId).observe(viewLifecycleOwner) { chat ->
                if (chat != null) {
                    val bundle = Bundle()
                    bundle.putSerializable("objeto", chat)

                    view?.let { Navigation.findNavController(it) }
                        ?.navigate(R.id.action_productFragment_to_messagesFragment, bundle)
                }
            }
            //val chat: Chat? = viewModel.createNewChat(userId, productUserId)

            /*var chat: Chat? = null
            val chatCol = db.collection("User").document(userId).collection("Chat")
            chatCol.whereEqualTo("OtherUser", productUserId)
                .get().addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        for(document in documents) {
                            val id = document.id
                            val data = document.toObject<Chat>()
                            val otherUser = data.OtherUser
                            chat = Chat(id, otherUser, Message("", "", "", "", null))
                        }

                        //Si el chat ya existe navegamos al fragmento de los mensajes y le pasamos el chat
                        val bundle = Bundle()
                        bundle.putSerializable("objeto", chat)

                        view?.let { Navigation.findNavController(it) }
                            ?.navigate(R.id.action_productFragment_to_messagesFragment, bundle)
                    } else {
                        //Si no existe primero lo creamos para el usuario logeado
                        val chat1 = hashMapOf(
                            "OtherUser" to productUserId
                        )
                        db.collection("User").document(userId)
                            .collection("Chat").add(chat1)
                            .addOnSuccessListener { documentReference ->
                                //Now I can obtain it's data
                                val documentId = documentReference.id
                                chatCol.document(documentId).get()
                                    .addOnSuccessListener { documentSnapshot ->
                                        val chat = documentSnapshot.toObject(Chat::class.java)

                                        //Lo preparamos para luego enviarlo como argumento
                                        val bundle = Bundle()
                                        bundle.putSerializable("objeto", chat)

                                        //Chat creation completed
                                        Log.d(ContentValues.TAG, "Chat creado para el usuario logeado")

                                        //Lo creamos también para el otro usuario
                                        val chat2 = hashMapOf(
                                            "OtherUser" to userId
                                        )
                                        db.collection("User").document(productUserId)
                                            .collection("Chat").add(chat2)
                                            .addOnSuccessListener {
                                                //Chat creation completed
                                                Log.d(ContentValues.TAG, "Chat creado para el otro usuario")

                                                //Después navegamos al fragmento de los mensajes y le pasamos el chat de nuestro usuario como argumento
                                                view?.let { Navigation.findNavController(it) }
                                                    ?.navigate(R.id.action_productFragment_to_messagesFragment, bundle)
                                            }
                                    }
                            }
                            .addOnFailureListener { exception ->
                                //Error
                                Log.w(ContentValues.TAG, "Error al intentar crear un chat: ", exception)
                            }
                    }
                }*/

        }
        return binding.root
    }
}