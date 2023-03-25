package com.esardo.a2ndhand

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.esardo.a2ndhand.adapter.ProductAdapter
import com.esardo.a2ndhand.databinding.FragmentVendoBinding
import com.esardo.a2ndhand.model.Product
import com.esardo.a2ndhand.model.User
import com.esardo.a2ndhand.viewmodel.ProductViewModel

class VendoFragment : Fragment() {
    private lateinit var _binding: FragmentVendoBinding
    private val binding get() = _binding!!

    lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapter
    private lateinit var viewModel: ProductViewModel

    private val productList = mutableListOf<Product>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVendoBinding.inflate(inflater, container, false)
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

        val isSell = true
        viewModel.getAllProducts(isSell)

        binding.btnNewProduct.setOnClickListener {
            //Start UploadProduct, send userId and set isSell as true
            val intent = Intent(activity, UploadProduct::class.java)
            intent.putExtra("vendo_fragment", userRef)
            startActivity(intent)
        }
        initRecyclerView()
        return binding.root
    }

    //Setups the RecyclerView
    private fun initRecyclerView() {
        adapter = ProductAdapter(productList) { product -> loadProduct(product) }
        recyclerView = binding.rvVendo
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
    }

    //Load the product Fragment
    private fun loadProduct(product: Product) {
        val bundle = Bundle()
        bundle.putSerializable("objeto", product)
        // Navigates to ProductFragment and pass the bundle as an argument
        view?.let { Navigation.findNavController(it) }
            ?.navigate(R.id.action_vendoFragment_to_productFragment, bundle)
    }
}