package com.esardo.a2ndhand

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.esardo.a2ndhand.adapter.ItemAdapter
import com.esardo.a2ndhand.adapter.ProductAdapter
import com.esardo.a2ndhand.adapter.RatingAdapter
import com.esardo.a2ndhand.databinding.FragmentProfileBinding
import com.esardo.a2ndhand.model.Product
import com.esardo.a2ndhand.model.Rating
import com.esardo.a2ndhand.model.User
import com.esardo.a2ndhand.viewmodel.ProductViewModel
import com.esardo.a2ndhand.viewmodel.TownViewModel
import com.esardo.a2ndhand.viewmodel.UserViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    private lateinit var _binding: FragmentProfileBinding
    private val binding get() = _binding!!

    lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapter
    private lateinit var viewModel: ProductViewModel
    private lateinit var userViewModel: UserViewModel

    private val productList = mutableListOf<Product>()
    var userId : String = ""

    private val ratingList = mutableListOf<Rating>()
    private lateinit var ratingAdapter: RatingAdapter

    val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val userRef = activity?.intent?.getSerializableExtra("object") as? User

        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        viewModel = ViewModelProvider(this)[ProductViewModel::class.java]
        viewModel.getAllProductObserver()
        //This will observe the productList of the ProductViewModel class and load the necessary data into the recyclerview
        //everytime that the fragment is loaded
        viewModel.productLiveData.observe(viewLifecycleOwner){
            productList.clear()
            if (it != null) {
                productList.addAll(it)
            }
            adapter.notifyDataSetChanged()
        }

        //Funciona pero puede que haya que cambiar alguna cosa
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

        //Load user data
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
                        Picasso.get().load(picture).placeholder(R.drawable.prueba).error(R.drawable.prueba).into(binding.ivProfilePic)
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
                            }
                        }
                    }
                }
            }
        }

        viewModel.getMyProducts(userId)

        binding.btnEditProfile.setOnClickListener {
            //Start SignInActivity and send UserId
            val intent = Intent(requireContext(), SignInActivity::class.java).apply {
                putExtra("object", userRef)
            }
            startActivity(intent)
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
            userViewModel.getVotes(userId)

            dialog.show()
        }

        binding.btnVote.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            val view = layoutInflater.inflate(R.layout.dialog_add_vote, null)

            builder.setView(view)

            val dialog = builder.create()

            dialog.show()

            val btnSendVote = view.findViewById<Button>(R.id.btnSendVote)

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
                    }
                }
            }

        }

        setHasOptionsMenu(true)
        initRecyclerView()
        return binding.root
    }

    //Setups the RecyclerView
    private fun initRecyclerView() {
        adapter = ProductAdapter(viewModel, userId, productList) { product -> loadProduct(product) }
        recyclerView = binding.rvMyProd
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
    }

    //Load the product Fragment
    private fun loadProduct(product: Product) {
        val bundle = Bundle()
        bundle.putSerializable("objeto", product)
        // Navigates to ProductFragment and pass the bundle as an argument
        view?.let { Navigation.findNavController(it) }
            ?.navigate(R.id.action_profileFragment_to_productFragment, bundle)
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