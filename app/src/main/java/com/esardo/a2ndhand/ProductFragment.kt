package com.esardo.a2ndhand

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.esardo.a2ndhand.adapter.RatingAdapter
import com.esardo.a2ndhand.databinding.FragmentProductBinding
import com.esardo.a2ndhand.model.Product
import com.esardo.a2ndhand.model.Rating
import com.esardo.a2ndhand.model.User
import com.esardo.a2ndhand.viewmodel.ChatViewModel
import com.esardo.a2ndhand.viewmodel.ProductViewModel
import com.esardo.a2ndhand.viewmodel.UserViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

const val UPDATE_PRODUCT_REQUEST_CODE = 1
class ProductFragment : Fragment() {
    private lateinit var _binding: FragmentProductBinding
    private val binding get() = _binding

    private lateinit var ratingAdapter: RatingAdapter
    private lateinit var userViewModel: UserViewModel
    private lateinit var viewModel:ChatViewModel
    private lateinit var productViewModel: ProductViewModel

    private val images =  mutableListOf<String>()
    private val ratingList = mutableListOf<Rating>()

    private lateinit var productRef : Product
    private lateinit var userId: String
    private lateinit var viewPager2: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var tvProdName: TextView
    private lateinit var tvPrice: TextView
    private lateinit var tvCategory: TextView
    private lateinit var tvPublishDate: TextView
    private lateinit var tvDescription: TextView

    val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductBinding.inflate(inflater, container, false)
        //Recibimos la referencia del usuario
        val userRef = activity?.intent?.getSerializableExtra("object") as? User
        //Recibimos el objeto Product de los argumentos
        productRef = requireArguments().getSerializable("objeto") as Product

        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        productViewModel = ViewModelProvider(this)[ProductViewModel::class.java]

        //Obtenemos el objeto NavController
        val navController = findNavController()

        //Guardamos el UserId de la referencia
        val userID = userRef?.id
        if (userID != null) {
            userId = userID
        }

        tvProdName = binding.tvProdName
        tvPrice = binding.tvPrice
        tvCategory = binding.tvCategory
        tvPublishDate = binding.tvPublishDate
        tvDescription = binding.tvDescription

        //Para mostrar las imágenes
        viewPager2 = binding.viewPager2
        tabLayout = binding.tabLayout

        val picture = productRef.Picture
        images.clear()
        if (picture != null) {
            if(picture.Pic1 != "") images.add(picture.Pic1)
            if(picture.Pic2 != "") images.add(picture.Pic2)
            if(picture.Pic3 != "") images.add(picture.Pic3)
            if(picture.Pic4 != "") images.add(picture.Pic4)
            if(picture.Pic5 != "") images.add(picture.Pic5)
        }
        //Configura el ViewPager2 para mostrar las imágenes
        viewPager2.adapter = ProductImageAdapter(images)

        //Vinculamos el tabLayout para que salgan posiciones y podamos indicar que se puede deslizar
        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            //Establece el texto del tab como el número de posición (empezando por 1)
            tab.text = (position + 1).toString()
        }.attach()

        //Vamos añadiendo los datos del producto a los elementos
        tvProdName.text = productRef.Name
        //Formatea el precio eliminando los decimales si su valor es 0
        tvPrice.text = productRef.Price.toString().replace(".0", "") + " €"
        tvDescription.text = productRef.Description

        //Obtiene el nombre de la Categoría pasando su Id
        val categoryId = productRef.CategoryId
        val categoryDoc = db.collection("Category").document(categoryId)
        categoryDoc.get().addOnSuccessListener { category ->
            if (category != null) {
                tvCategory.text = "Categoría: " + category.getString("Name")
            }
        }

        //Le da formato al campo PublishDate
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateString = dateFormat.format(productRef.PublishDate)
        tvPublishDate.text = "Fecha de publicación: " + dateString

        val ivProfilePicture = binding.ivProfilePicture
        val tvUserName = binding.tvUserName
        val tvUbication = binding.tvUbication
        val tvRating = binding.tvRating
        var totalPoints = 0

        //Obtiene los datos del usuario pasando su Id
        val productUserId = productRef.UserId
        val userDoc = db.collection("User").document(productUserId)
        userDoc.get().addOnSuccessListener { user ->
            if (user != null) {
                val profPicture = user.getString("Picture")
                if(profPicture != ""){
                    Picasso.get().load(profPicture).placeholder(R.drawable.profile).error(R.drawable.profile).into(ivProfilePicture)
                }
                tvUserName.text = user.getString("User")
                //Obtiene el nombre de la Ciudad pasando su Id
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
            binding.llProdIcons.visibility = GONE
        } else {
            binding.btnSendMessage.visibility = GONE
        }

        //Al pulsar en la tarjeta con la información del usuario navegamos a su perfil
        binding.cvUser.setOnClickListener {
            if (userRef != null) {
                if (productUserId != userRef.id){
                    val bundle = Bundle()
                    bundle.putString("user", productUserId)
                    //Navegamos al ProfileFragment y se pasa el productUserId como argumento
                    view?.let { Navigation.findNavController(it) }
                        ?.navigate(R.id.action_productFragment_to_profileFragment, bundle)
                } else{
                    //Navegamos al perfil del usuario que ha iniciado sesión
                    navController.navigate(R.id.action_productFragment_to_profileFragment)
                }
            }
        }

        //Cuando pulsamos a Ver valoraciones se crea un diálogo con la información de todos los votos que ha recibido el usuario
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
            //Observamos el ratingList del UserViewModel y cargamos los datos en el recyclerview
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

        //Al pulsar el icono de editar producto
        binding.btnEditProduct.setOnClickListener {
            val productId = productRef.id
            //Lanza la actividad UploadProduct y envía el id del producto
            val intent = Intent(activity, UploadProduct::class.java)
            intent.putExtra("productId", productId)
            startActivityForResult(intent, UPDATE_PRODUCT_REQUEST_CODE)
        }

        //Al pulsar en Enviar mensaje se llama a la función que crea un nuevo chat en la base de datos
        binding.btnSendMessage.setOnClickListener {
            viewModel.createNewChat(userId, productUserId).observe(viewLifecycleOwner) { chat ->
                if (chat != null) {
                    val bundle = Bundle()
                    bundle.putSerializable("objeto", chat)
                    //Navegamos al messagesFragment pasando el chat en el bundle
                    view?.let { Navigation.findNavController(it) }
                        ?.navigate(R.id.action_productFragment_to_messagesFragment, bundle)
                }
            }
        }

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == UPDATE_PRODUCT_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val updatedProduct = data.getSerializableExtra("updatedProduct") as? Product
            if (updatedProduct != null) {
                //productRef = updatedProduct
                updateProductData(updatedProduct)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    //Actualiza los elementos con los nuevos datos del producto
    private fun updateProductData(updatedProduct: Product) {
        tvProdName.text = updatedProduct.Name
        tvPrice.text = updatedProduct.Price.toString().replace(".0", "") + " €"
        val categoryId = updatedProduct.CategoryId
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateString = dateFormat.format(updatedProduct.PublishDate)
        tvPublishDate.text = "Fecha de publicación: " + dateString
        tvDescription.text = updatedProduct.Description
        val categoryDoc = db.collection("Category").document(categoryId)
        categoryDoc.get().addOnSuccessListener { category ->
            if (category != null) {
                tvCategory.text = "Categoría: " + category.getString("Name")

                //Ahora comprobamos si tiene imágenes
                if(updatedProduct.Picture == null) {
                    //Si no tiene, comprobamos si Product sí que tiene
                    images.clear()
                } else {
                    //Si tiene limpiamos la lista y la volvemos a llenar con los datos nuevos
                    if(updatedProduct.Picture!!.Pic1 != "") images.add(updatedProduct.Picture!!.Pic1)
                    if(updatedProduct.Picture!!.Pic2 != "") images.add(updatedProduct.Picture!!.Pic2)
                    if(updatedProduct.Picture!!.Pic3 != "") images.add(updatedProduct.Picture!!.Pic3)
                    if(updatedProduct.Picture!!.Pic4 != "") images.add(updatedProduct.Picture!!.Pic4)
                    if(updatedProduct.Picture!!.Pic5 != "") images.add(updatedProduct.Picture!!.Pic5)

                    viewPager2.adapter = ProductImageAdapter(images)

                    TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
                        tab.text = (position + 1).toString()
                    }.attach()
                }
            }
        }
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
                .placeholder(R.drawable.logo)
                .error(R.drawable.logo)
                .into(holder.imageView) // Utiliza Picasso para cargar la imagen en el ImageView
        }

        override fun getItemCount() = imageUrls.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(R.id.imageView)
        }
    }
}

