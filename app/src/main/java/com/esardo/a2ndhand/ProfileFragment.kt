package com.esardo.a2ndhand

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.esardo.a2ndhand.adapter.ProductAdapter
import com.esardo.a2ndhand.databinding.FragmentProfileBinding
import com.esardo.a2ndhand.model.Product
import com.esardo.a2ndhand.model.User
import com.esardo.a2ndhand.viewmodel.ProductViewModel
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {
    private lateinit var _binding: FragmentProfileBinding
    private val binding get() = _binding!!

    lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapter
    private lateinit var viewModel: ProductViewModel

    private val productList = mutableListOf<Product>()
    var userId : String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val userRef = activity?.intent?.getSerializableExtra("object") as? User

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
            userId = arguments.getString("user") as String
        } else {
            if (userRef != null) {
                userId = userRef.id
            }
        }

        //Load user data
        val points = 0
        val picture = ""
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

        viewModel.getMyProducts(userId)

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