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
import com.esardo.a2ndhand.adapter.ProductAdapter
import com.esardo.a2ndhand.adapter.ItemAdapter
import com.esardo.a2ndhand.databinding.FragmentVendoBinding
import com.esardo.a2ndhand.model.Product
import com.esardo.a2ndhand.model.User
import com.esardo.a2ndhand.viewmodel.ProductViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class VendoFragment : Fragment(), SearchView.OnQueryTextListener {
    private lateinit var _binding: FragmentVendoBinding
    private val binding get() = _binding

    lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapter
    private lateinit var viewModel: ProductViewModel

    private val productList = mutableListOf<Product>()
    private lateinit var userId: String
    private var isSell: Boolean = true

    val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVendoBinding.inflate(inflater, container, false)
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

        //Llamamos a la función getAllProducts para que se llene la lista de productos
        viewModel.getAllProducts(isSell, userId)

        //Este método coge el texto del searchView y lo envía en la variable query
        binding.svProduct.setOnQueryTextListener(this)
        //Cuando se limpia el SearchView se vuelven a cargar todos los productos
        binding.svProduct.setOnCloseListener {
            viewModel.getAllProducts(isSell, userId)
            true
        }

        //Muestra el diálogo para seleccionar un filtro
        binding.btnFilter.setOnClickListener {
            showDialog()
        }

        //Inicia la actividad UploadProduct y le pasa el userId y la variable isSell
        binding.btnNewProduct.setOnClickListener {
            val intent = Intent(activity, UploadProduct::class.java)
            intent.putExtra("vendo_fragment", userRef)
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

    //Setups RecyclerView
    private fun initRecyclerView() {
        adapter = ProductAdapter(viewModel, userId, productList) { product -> loadProduct(product) }
        recyclerView = binding.rvVendo
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
    }

    //Carga el detalle del producto cuando se pulsa en una de las tarjetas
    private fun loadProduct(product: Product) {
        val bundle = Bundle()
        bundle.putSerializable("objeto", product)
        //Navega al ProductFragment y le pasa el bundle como argumento
        view?.let { Navigation.findNavController(it) }
            ?.navigate(R.id.action_vendoFragment_to_productFragment, bundle)
    }

    //Controla si cambia el texto del SearchView
    override fun onQueryTextChange(newText:String?):Boolean {
        return true
    }
    //Envía el texto del SearchView a la función que filtra por dicho texto
    override fun onQueryTextSubmit(query: String?): Boolean {
        if(!query.isNullOrEmpty()){
            viewModel.getProductsByName(query)
        }
        return true
    }
}