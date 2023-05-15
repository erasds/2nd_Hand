package com.esardo.a2ndhand

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.esardo.a2ndhand.adapter.ProductAdapter
import com.esardo.a2ndhand.databinding.FragmentFavsBinding
import com.esardo.a2ndhand.model.Product
import com.esardo.a2ndhand.model.User
import com.esardo.a2ndhand.viewmodel.ProductViewModel

class FavsFragment : Fragment() {
    private lateinit var _binding: FragmentFavsBinding
    private val binding get() = _binding

    lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapter
    private lateinit var viewModel: ProductViewModel

    private val productList = mutableListOf<Product>()
    private lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavsBinding.inflate(inflater, container, false)
        //Recibimos la referencia del usuario que ha iniciado sesión
        val userRef = activity?.intent?.getSerializableExtra("object") as? User

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

        //Guardamos el UserId de la referencia
        val userID = userRef?.id
        if (userID != null) {
            userId = userID
        }

        //Llamamos a la función getMyFavorites para que se llene la lista de productos
        viewModel.getMyFavorites(userId)
        initRecyclerView()
        return binding.root
    }

    //Setups RecyclerView
    private fun initRecyclerView() {
        adapter = ProductAdapter(viewModel, userId, productList) { product -> loadProduct(product) }
        recyclerView = binding.rvFavs
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
    }

    //Carga el detalle del producto cuando se pulsa en una de las tarjetas
    private fun loadProduct(product: Product) {
        val bundle = Bundle()
        bundle.putSerializable("objeto", product)
        //Navega al ProductFragment y le pasa el bundle como argumento
        view?.let { Navigation.findNavController(it) }
            ?.navigate(R.id.action_favsFragment_to_productFragment, bundle)
    }
}