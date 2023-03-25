package com.esardo.a2ndhand.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.esardo.a2ndhand.R
import com.esardo.a2ndhand.databinding.ItemProductBinding
import com.esardo.a2ndhand.model.Product

class ProductAdapter(
    private val productList: List<Product>,
    private val loadProduct: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.item_product, parent, false))
    }

    override fun getItemCount(): Int = productList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = productList[position]
        holder.bind(item)
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val binding = ItemProductBinding.bind(view)

        // Binds elements to it's value
        val tvName = view.findViewById<TextView>(R.id.tvName)
        val tvPrice = view.findViewById<TextView>(R.id.tvPrice)

        //With Picasso library this will load the Product image, an image to show while data is loading,
        // and an image to show if there's an error loading the Product image
        fun bind (product: Product) {
            //Picasso.get().load(product.image).placeholder(R.drawable.loading).error(R.drawable.error).into(binding.ivImage)
            tvName.text = product.Name
            //Format price erasing decimals if it's value is 0
            val price = product.Price.toString().replace(".0", "")
            tvPrice.text = price

            //To load ProductFragment with the data of the item clicked
            itemView.setOnClickListener { loadProduct(product) }
        }
    }
}