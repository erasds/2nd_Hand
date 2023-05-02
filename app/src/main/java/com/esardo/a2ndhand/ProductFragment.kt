package com.esardo.a2ndhand

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.esardo.a2ndhand.adapter.RatingAdapter
import com.esardo.a2ndhand.databinding.FragmentProductBinding
import com.esardo.a2ndhand.model.Picture
import com.esardo.a2ndhand.model.Product
import com.esardo.a2ndhand.model.Rating
import com.esardo.a2ndhand.model.User
import com.esardo.a2ndhand.viewmodel.ChatViewModel
import com.esardo.a2ndhand.viewmodel.UserViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

val REQUEST_CODE = 1 // Código de solicitud para startActivityForResult()
class ProductFragment : Fragment() {
    private lateinit var _binding: FragmentProductBinding
    private val binding get() = _binding

    private lateinit var product : Product
    private lateinit var userId: String

    private lateinit var viewModel:ChatViewModel

    private lateinit var userViewModel: UserViewModel
    private val ratingList = mutableListOf<Rating>()
    private lateinit var ratingAdapter: RatingAdapter

    private lateinit var viewPager2: ViewPager2
    private lateinit var tabLayout: TabLayout
    private val images =  mutableListOf<String>()

    val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProductBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

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
        val tvProdName = binding.tvProdName
        val tvPrice = binding.tvPrice
        val tvCategory = binding.tvCategory
        val tvPublishDate = binding.tvPublishDate
        val tvDescription = binding.tvDescription

        //Para mostrar las imágenes
        viewPager2 = binding.viewPager2
        tabLayout = binding.tabLayout

        val picture = product.Picture
        images.clear()
        if(picture.Pic1 != "") images.add(picture.Pic1)
        if(picture.Pic2 != "") images.add(picture.Pic2)
        if(picture.Pic3 != "") images.add(picture.Pic3)
        if(picture.Pic4 != "") images.add(picture.Pic4)
        if(picture.Pic5 != "") images.add(picture.Pic5)
        viewPager2.adapter = ProductImageAdapter(images) // configura el ViewPager2 para mostrar las imágenes

        //Vinculamos el tabLayout para que salgan puntitos y podamos indicar que se puede deslizar
        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            // establece el texto del tab como el número de posición (empezando por 1)
            tab.text = (position + 1).toString()
        }.attach()

        tvProdName.text = product.Name
        //Format price erasing decimals if it's value is 0
        tvPrice.text = product.Price.toString().replace(".0", "") + " €"
        tvDescription.text = product.Description

        //Get Category name by its Id
        val categoryId = product.CategoryId
        val categoryDoc = db.collection("Category").document(categoryId)
        categoryDoc.get().addOnSuccessListener { category ->
            if (category != null) {
                tvCategory.text = "Categoría: " + category.getString("Name")
            }
        }

        //Format PublishDate
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateString = dateFormat.format(product.PublishDate)
        tvPublishDate.text = "Fecha de publicación: " + dateString

        //User info
        val ivProfilePicture = binding.ivProfilePicture
        val tvUserName = binding.tvUserName
        val tvUbication = binding.tvUbication
        val tvRating = binding.tvRating
        var totalPoints = 0

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

                            //Ahora cargamos los puntos del usuario
                            val ratingCol = userDoc.collection("Rating")
                            ratingCol.get().addOnSuccessListener { querySnapshot ->
                                for(document in querySnapshot.documents) {
                                    val points = document.getLong("Points")?.toInt() ?: 0
                                    totalPoints += points
                                }
                                val totalPtsStr = totalPoints.toString()
                                tvRating.text = "$totalPtsStr puntos de usuario"
                            }
                        }
                    }
                }
            }

            //Dependiendo de si el producto pertenece al usuario logeado o no cambiaremos el aspecto
            if(productUserId != userId) {
                binding.btnEditProduct.visibility = INVISIBLE
            } else {
                binding.btnSendMessage.visibility = GONE
            }
        }

        binding.llSeller.setOnClickListener {
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

        binding.tvSeeVotes.setOnClickListener {
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.dialog_show_votes)

            ratingAdapter = RatingAdapter(ratingList)
            val recyclerView = dialog.findViewById<RecyclerView>(R.id.rvVotes)
            val layoutManager = LinearLayoutManager(requireContext())
            recyclerView.layoutManager = layoutManager
            recyclerView.setHasFixedSize(true)
            recyclerView.adapter = ratingAdapter

            userViewModel.getAllVotesObserver()
            //This will observe the ratingList of the UserViewModel class and load the necessary data into the recyclerview
            //everytime that the dialog is loaded
            userViewModel.ratingLiveData.observe(viewLifecycleOwner){
                ratingList.clear()
                if (it != null) {
                    ratingList.addAll(it)
                }
                ratingAdapter.notifyDataSetChanged()
            }
            userViewModel.getVotes(productUserId)

            dialog.show()
        }

        binding.btnEditProduct.setOnClickListener {
            val productId = product.id
            //Start UploadProduct, send product
            val intent = Intent(activity, UploadProduct::class.java)
            intent.putExtra("productId", productId)
            startActivity(intent)
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
        }
        return binding.root
    }


    // Clase interna para el adaptador del ViewPager2
    private inner class ProductImageAdapter(val imageUrls: List<String>) : RecyclerView.Adapter<ProductImageAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product_image, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val imageUrl = imageUrls[position]
            Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.prueba)
                .error(R.drawable.prueba)
                .into(holder.imageView) // Utiliza Picasso para cargar la imagen en el ImageView
        }

        override fun getItemCount() = imageUrls.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(R.id.imageView)
        }
    }
}

