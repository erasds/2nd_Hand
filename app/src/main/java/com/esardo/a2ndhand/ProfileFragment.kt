package com.esardo.a2ndhand

import android.animation.Animator
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import androidx.constraintlayout.widget.ConstraintSet.VISIBLE
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.esardo.a2ndhand.adapter.ProductAdapter
import com.esardo.a2ndhand.adapter.RatingAdapter
import com.esardo.a2ndhand.databinding.FragmentProfileBinding
import com.esardo.a2ndhand.model.Product
import com.esardo.a2ndhand.model.Rating
import com.esardo.a2ndhand.model.User
import com.esardo.a2ndhand.viewmodel.ProductViewModel
import com.esardo.a2ndhand.viewmodel.UserViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {
    private lateinit var _binding: FragmentProfileBinding
    private val binding get() = _binding

    lateinit var recyclerView: RecyclerView
    private lateinit var ratingAdapter: RatingAdapter
    private lateinit var adapter: ProductAdapter
    private lateinit var viewModel: ProductViewModel
    private lateinit var userViewModel: UserViewModel

    private val ratingList = mutableListOf<Rating>()
    private val productList = mutableListOf<Product>()
    var userId : String = ""

    val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        //Recibimos la referencia del usuario
        val userRef = activity?.intent?.getSerializableExtra("object") as? User

        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        viewModel = ViewModelProvider(this)[ProductViewModel::class.java]
        viewModel.getAllProductObserver()
        //Observamos el productList del ProductViewModel y cargamos los datos en el recyclerview
        viewModel.productLiveData.observe(viewLifecycleOwner){
            productList.clear()
            if (it != null) {
                productList.addAll(it)
            }
            adapter.notifyDataSetChanged()
        }

        //Guardamos el UserId de la referencia,
        //en este caso puede ser del usuario que ha iniciado sesión
        //o del usuario del perfil que estemos visualizando
        val arguments = arguments
        if(arguments != null){
            //Si es el perfil de otro usuario
            userId = arguments.getString("user") as String
            binding.btnEditProfile.visibility = INVISIBLE
        } else {
            //Si es nuestro perfil
            binding.btnVote.visibility = GONE
            if (userRef != null) {
                userId = userRef.id
            }
        }

        //Cargamos los datos del usuario
        var totalPoints = 0
        val userDoc = db.collection("User").document(userId)
        userDoc.addSnapshotListener { user, exception ->
            if(exception != null) {
                Log.w("TAG", "Listen failed.", exception)
                return@addSnapshotListener
            } else {
                if (user != null) {
                    val userName = user.getString("User")
                    binding.tvUserName.text = userName
                    val picture = user.getString("Picture")
                    if(picture != "") {
                        Picasso.get().load(picture).placeholder(R.drawable.profile).error(R.drawable.profile).into(binding.ivProfilePic)
                    }

                    //Ahora obtenemos el nombre de la ciudad
                    val townId = user.getString("TownId")
                    if (townId != null){
                        val townDoc = FirebaseFirestore.getInstance().collection("Town").document(townId)
                        townDoc.get().addOnSuccessListener { town ->
                            if (town != null) {
                                val townName = town.getString("Name")
                                binding.tvUbication.text = townName
                            }

                            //Ahora cargamos los puntos del usuario
                            val ratingCol = userDoc.collection("Rating")
                            ratingCol.get().addOnSuccessListener { querySnapshot ->
                                for(document in querySnapshot.documents) {
                                    val points = document.getLong("Points")?.toInt() ?: 0
                                    totalPoints += points
                                }
                                val totalPtsStr = totalPoints.toString()
                                binding.tvRating.text = "$totalPtsStr puntos de usuario"
                                if(totalPoints == 0) binding.tvSeeVotes.visibility = INVISIBLE
                            }
                        }
                    }
                }
            }
        }

        //Llamamos a la función getMyProducts para que se llene la lista de productos
        viewModel.getMyProducts(userId)

        //Al pulsar el icono de Editar perfil
        binding.btnEditProfile.setOnClickListener {
            //Carga SignInActivity y envía el UserId
            val intent = Intent(requireContext(), SignInActivity::class.java).apply {
                putExtra("object", userRef)
            }
            startActivity(intent)
        }

        //Al pulsar en ver valoraciones se abre un diálogo con los comentarios de otros usuarios
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
            userViewModel.getVotes(userId)

            dialog.show()
        }

        //Al pulsar en el botón de Valorar usuario se abre un diálogo para poder asignarle puntos y un comentario
        binding.btnVote.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            val view = layoutInflater.inflate(R.layout.dialog_add_vote, null)

            builder.setView(view)

            val dialog = builder.create()

            dialog.show()

            val btnSendVote = view.findViewById<Button>(R.id.btnSendVote)

            //Al enviar la valoración
            btnSendVote.setOnClickListener {
                //Cojo el valor del rating bar
                val ratingBar = view.findViewById<RatingBar>(R.id.rbHands)
                val rating = ratingBar.rating.toInt()

                //Cojo el valor del comentario
                val etComment = view.findViewById<EditText>(R.id.etComment)
                val comment = etComment.text.toString()

                //Lo inserto en la base de datos y cierro el diálogo
                userViewModel.voteUser(userId, rating, comment)
                    .observe(viewLifecycleOwner) { isRated ->
                    if (isRated) {
                        dialog.cancel()
                        showAnimation(binding.ivAnimation, R.raw.success)
                    }
                }
            }

        }

        initRecyclerView()
        return binding.root
    }

    //Setups RecyclerView
    private fun initRecyclerView() {
        adapter = ProductAdapter(viewModel, userId, productList) { product -> loadProduct(product) }
        recyclerView = binding.rvMyProd
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
    }

    //Carga el detalle del producto cuando se pulsa en una de las tarjetas
    private fun loadProduct(product: Product) {
        val bundle = Bundle()
        bundle.putSerializable("objeto", product)
        //Navega al ProductFragment y le pasa el bundle como argumento
        view?.let { Navigation.findNavController(it) }
            ?.navigate(R.id.action_profileFragment_to_productFragment, bundle)
    }

    //Lanza la animación y cierra la actividad una vez termina
    private fun showAnimation(
        imageView: LottieAnimationView,
        animation: Int,
    ) {
        imageView.visibility = View.VISIBLE
        imageView.setAnimation(animation)
        imageView.playAnimation()
        imageView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                // No se necesita implementación aquí
            }
            override fun onAnimationEnd(animation: Animator) {
                imageView.visibility = GONE
            }
            override fun onAnimationCancel(animation: Animator) {
                // No se necesita implementación aquí
            }
            override fun onAnimationRepeat(animation: Animator) {
                // No se necesita implementación aquí
            }
        })
    }
}