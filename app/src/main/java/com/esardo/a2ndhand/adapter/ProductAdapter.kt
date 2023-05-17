package com.esardo.a2ndhand.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.esardo.a2ndhand.R
import com.esardo.a2ndhand.databinding.ItemProductBinding
import com.esardo.a2ndhand.model.Product
import com.esardo.a2ndhand.viewmodel.ProductViewModel
import com.squareup.picasso.Picasso

class ProductAdapter(
    private val viewModel: ProductViewModel,
    private val userId: String,
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
        holder.bind(item, userId)
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val binding = ItemProductBinding.bind(view)

        //Enlaza los elementos con su valor
        private val tvName = binding.tvName
        private val tvPrice = binding.tvPrice
        private val chxFav = binding.chxFav

        //With Picasso library this will load the Product image, an image to show while data is loading,
        // and an image to show if there's an error loading the Product image
        fun bind (product: Product, userId: String) {
            val productPic = product.Picture.Pic1
            if(productPic != "") {
                Picasso.get().load(productPic).placeholder(R.drawable.logo).error(R.drawable.logo).into(binding.ivImage)
            }
            tvName.text = product.Name
            //Format price erasing decimals if it's value is 0
            val price = product.Price.toString().replace(".0", "") + " €"
            tvPrice.text = price
            chxFav.isChecked = product.isChecked

            //To add/delete item to/from Favorite collection
            chxFav.setOnClickListener {
                if(chxFav.isChecked) {
                    product.isChecked = true
                    //Insert document to Favorite collection
                    viewModel.addFavorite(product.id, userId)

                } else {
                    product.isChecked = false
                    //Delete document from Favorite collection
                    viewModel.deleteFavorite(product.id, userId)
                }
            }

            //To load ProductFragment with the data of the item clicked
            itemView.setOnClickListener { loadProduct(product) }
        }

    }
}