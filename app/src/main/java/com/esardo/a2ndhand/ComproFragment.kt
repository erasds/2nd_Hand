package com.esardo.a2ndhand

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.esardo.a2ndhand.adapter.ItemAdapter
import com.esardo.a2ndhand.adapter.ProductAdapter
import com.esardo.a2ndhand.databinding.FragmentComproBinding
import com.esardo.a2ndhand.model.Product
import com.esardo.a2ndhand.model.User
import com.esardo.a2ndhand.viewmodel.ProductViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ComproFragment : Fragment(), SearchView.OnQueryTextListener {
    private lateinit var _binding: FragmentComproBinding
    private val binding get() = _binding

    lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapter
    private lateinit var viewModel: ProductViewModel

    private val productList = mutableListOf<Product>()
    private lateinit var userId: String
    private var isSell: Boolean = false

    val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentComproBinding.inflate(inflater, container, false)
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

        val userID = userRef?.id
        if (userID != null) {
            userId = userID
        }
        viewModel.getAllProducts(isSell, userId)

        //this will get the text of the searchView and set it as the query variable
        binding.svProduct.setOnQueryTextListener(this)
        //When SearchView is closed all products load again
        binding.svProduct.setOnCloseListener {
            if (userId != null) {
                viewModel.getAllProducts(isSell, userId)
            }
            true
        }

        binding.btnFilter.setOnClickListener {
            showDialog()
        }

        binding.btnNewProduct.setOnClickListener {
            //Start UploadProduct, send userId and set isSell as true
            val intent = Intent(activity, UploadProduct::class.java)
            intent.putExtra("compro_fragment", userRef)
            startActivity(intent)
        }
        initRecyclerView()
        return binding.root
    }

    private fun showDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_selection)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        val recyclerView = dialog.findViewById<RecyclerView>(R.id.rvList)
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager

        val viewModel = ViewModelProvider(this)[ProductViewModel::class.java]
        lifecycleScope.launch {
            val categories = viewModel.getCategories()
            val adapter = ItemAdapter(categories)
            recyclerView.adapter = adapter
        }

        recyclerView.addOnItemClickListener { position, _, _, _ ->
            val category = (recyclerView.adapter as ItemAdapter).items[position]
            viewModel.getProductsByFilter(category, userId, isSell)
            dialog.dismiss()
        }

        dialog.show()

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window?.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window?.attributes = lp
    }

    private fun RecyclerView.addOnItemClickListener(onClickListener: (position: Int, view: View, any: Any?, any2: Any?) -> Unit) {
        this.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                view.setOnClickListener {
                    val holder = getChildViewHolder(view)
                    onClickListener(holder.adapterPosition, view, null, null)
                }
            }
            override fun onChildViewDetachedFromWindow(view: View) {
                view.setOnClickListener(null)
            }
        })
    }

    //Setups the RecyclerView
    private fun initRecyclerView() {
        adapter = ProductAdapter(viewModel, userId, productList) { product -> loadProduct(product) }
        recyclerView = binding.rvCompro
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
    }

    //Load the product Fragment
    private fun loadProduct(product: Product) {
        val bundle = Bundle()
        bundle.putSerializable("objeto", product)
        // Navigates to ProductFragment and pass the bundle as an argument
        view?.let { Navigation.findNavController(it) }
            ?.navigate(R.id.action_comproFragment_to_productFragment, bundle)
    }

    //It controls when the text of the SearchView changes
    override fun onQueryTextChange(newText:String?):Boolean {
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if(!query.isNullOrEmpty()){
            viewModel.getProductsByName(query)
        }
        return true
    }
}