package com.esardo.a2ndhand.viewmodel

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.esardo.a2ndhand.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class ProductViewModel: ViewModel() {

    var productLiveData: MutableLiveData<List<Product>?> = MutableLiveData()

    var productList = mutableListOf<Product>()

    //var product: Product = Product()

    val db = FirebaseFirestore.getInstance()

    fun getAllProductObserver(): MutableLiveData<List<Product>?> {
        return productLiveData
    }

    fun getAllProducts(isSell : Boolean) {
        val productCol = db.collection("Product")
        productCol.whereEqualTo("IsSell", isSell)
            .get().addOnSuccessListener { documents ->
                productList.clear()
                for (document in documents) {
                    val data = document.toObject<Product>()
                    val id = data.id
                    val name = data.Name
                    val description = data.Description
                    val price = data.Price
                    val image = data.Image
                    val categoryId = data.CategoryId
                    val isSell = data.IsSell
                    val userId = data.UserId
                    val townId = data.TownId
                    val publishDate = data.PublishDate
                    val product = Product(id, name, description, price, image, categoryId, isSell, userId, townId, publishDate)
                    productList.add(product)
                }
                productLiveData.postValue(productList)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error obteniendo productos: ", exception)
            }
    }

    fun getSellProductsByName() {

    }

    fun getSellProductsWithFilters() {

    }

    fun getAllBuyProducts() {

    }

    fun getBuyProductsByName() {

    }

    fun getBuyProductsWithFilters() {

    }


}