package com.esardo.a2ndhand.viewmodel

import android.content.ContentValues.TAG
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.esardo.a2ndhand.model.Favorite
import com.esardo.a2ndhand.model.Picture
import com.esardo.a2ndhand.model.Product
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import java.util.Date

class ProductViewModel: ViewModel() {

    var productLiveData: MutableLiveData<List<Product>?> = MutableLiveData()

    var productList = mutableListOf<Product>()
    var favoriteList = mutableListOf<String>()
    var productIdList = mutableListOf<String>()

    val db = FirebaseFirestore.getInstance()

    fun getAllProductObserver(): MutableLiveData<List<Product>?> {
        return productLiveData
    }

    fun getAllProducts(isSell: Boolean, userId: String) {
        val favCol = db.collection("User")
            .document(userId)
            .collection("Favorite")
        favCol.get().addOnSuccessListener { documents ->
            favoriteList.clear()
            for (document in documents) {
                val productId = document.getString("ProductId")
                if (productId != null) {
                    favoriteList.add(productId)
                }
            }
        }.addOnFailureListener { exception ->
            Log.w(TAG, "Error cargando favoritos: ", exception)
        }
        val productCol = db.collection("Product")
        productCol.whereEqualTo("IsSell", isSell)
            .get().addOnSuccessListener { documents ->
                productList.clear()
                for (document in documents) {
                    val id = document.id
                    val data = document.toObject<Product>()
                    if(favoriteList.contains(id)) data.isChecked = true
                    val name = data.Name
                    val description = data.Description
                    val price = data.Price
                    //val image = data.Image
                    val picture = data.Picture
                    val pic1 = picture.Pic1
                    val pic2 = picture.Pic2
                    val pic3 = picture.Pic3
                    val pic4 = picture.Pic4
                    val pic5 = picture.Pic5
                    val categoryId = data.CategoryId
                    val isSell = data.IsSell
                    val userId = data.UserId
                    val townId = data.TownId
                    val publishDate = data.PublishDate
                    val product = Product(id, name, description, price, Picture(pic1, pic2, pic3, pic4, pic5), categoryId, isSell, userId, townId, publishDate, data.isChecked)
                    productList.add(product)
                }
                productLiveData.postValue(productList)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error obteniendo productos: ", exception)
            }
    }

    fun getProductsByName(query: String) {
        val filteredList: List<Product> = productList.filter { it.Name.contains(query, ignoreCase = true) }
        productLiveData.postValue(filteredList)
    }

    fun deleteFavorite(productId: String, userId: String) {
        val favCol = db.collection("User").document(userId)
            .collection("Favorite")
        val query = favCol.whereEqualTo("ProductId", productId)

        query.get().addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                val docSnapshot = querySnapshot.documents[0]
                val favRef = docSnapshot.reference
                favRef.delete().addOnSuccessListener {
                    Log.d(TAG, "Documento eliminado exitosamente!")
                }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error al eliminar el documento", e)
                    }
            }
        }
    }

    fun addFavorite(productId: String, userId: String) {
        val favorite = hashMapOf(
            "ProductId" to productId
        )
        db.collection("User").document(userId)
            .collection("Favorite").add(favorite)
            .addOnSuccessListener {
                //Favorite upload completed
                Log.d(TAG, "Producto guardado en favoritos")
            }
            .addOnFailureListener { exception ->
                //Error
                Log.w(TAG, "Error al intentar guardar como favorito: ", exception)
            }
    }

    fun getMyFavorites(userId: String) {
        productList.clear()
        val favCol = db.collection("User").document(userId)
            .collection("Favorite")
        favCol.get().addOnSuccessListener { result  ->
            productIdList.clear()
            for (document in result ) {
                val productId = document.getString("ProductId")
                if (productId != null) {
                    productIdList.add(productId)
                }
            }

            // Ahora que tenemos los IDs de los productos, realizamos una segunda consulta a la colección "productDetails"
            db.collection("Product")
                .whereIn(FieldPath.documentId(), productIdList)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val id = document.id
                        val data = document.toObject<Product>()
                        val name = data.Name
                        val description = data.Description
                        val price = data.Price
                        val picture = data.Picture
                        val pic1 = picture.Pic1
                        val pic2 = picture.Pic2
                        val pic3 = picture.Pic3
                        val pic4 = picture.Pic4
                        val pic5 = picture.Pic5
                        val categoryId = data.CategoryId
                        val isSell = data.IsSell
                        val userId = data.UserId
                        val townId = data.TownId
                        val publishDate = data.PublishDate
                        val product = Product(id, name, description, price, Picture(pic1, pic2, pic3, pic4, pic5), categoryId, isSell, userId, townId, publishDate, true)
                        productList.add(product)
                    }
                    productLiveData.postValue(productList)

                }.addOnFailureListener { exception ->
                    Log.d(TAG, "Error getting products: ", exception)
                    // Si ocurre un error, podemos devolver una lista vacía o manejar el error de otra manera
                    productLiveData.postValue(emptyList())
                }
        }.addOnFailureListener { exception ->
            Log.d(TAG, "Error getting product IDs: ", exception)
            // Si ocurre un error, podemos devolver una lista vacía o manejar el error de otra manera
            productLiveData.postValue(emptyList())
        }

    }

    fun getMyProducts(userId: String) {
        val favCol = db.collection("User")
            .document(userId)
            .collection("Favorite")
        favCol.get().addOnSuccessListener { documents ->
            favoriteList.clear()
            for (document in documents) {
                val productId = document.getString("ProductId")
                if (productId != null) {
                    favoriteList.add(productId)
                }
            }
        }.addOnFailureListener { exception ->
            Log.w(TAG, "Error cargando favoritos: ", exception)
        }
        val productCol = db.collection("Product")
        productCol.whereEqualTo("UserId", userId)
            .get().addOnSuccessListener { documents ->
                productList.clear()
                for (document in documents) {
                    val id = document.id
                    val data = document.toObject<Product>()
                    if(favoriteList.contains(id)) data.isChecked = true
                    val name = data.Name
                    val description = data.Description
                    val price = data.Price
                    //val image = data.Image
                    val picture = data.Picture
                    val pic1 = picture.Pic1
                    val pic2 = picture.Pic2
                    val pic3 = picture.Pic3
                    val pic4 = picture.Pic4
                    val pic5 = picture.Pic5
                    val categoryId = data.CategoryId
                    val isSell = data.IsSell
                    val userId = data.UserId
                    val townId = data.TownId
                    val publishDate = data.PublishDate
                    val product = Product(id, name, description, price, Picture(pic1, pic2, pic3, pic4, pic5), categoryId, isSell, userId, townId, publishDate, data.isChecked)
                    productList.add(product)
                }
                productLiveData.postValue(productList)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error obteniendo productos: ", exception)
            }
    }




    /*fun getFavoriteProducts(userId: String) {
        val favoriteCol = db.collection("Favorite")
        favoriteCol.whereEqualTo("UserId", userId)
            .get().addOnSuccessListener { documents ->
                productIdList.clear()
                for (document in documents) {
                    val productId = document.getString("ProductId")
                    productId?.let {
                        productIdList.add(it)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error obteniendo favoritos: ", exception)
            }

        productList.clear()
        for (productId in productIdList){
           getProductById(productId)
        }
        productLiveData.postValue(productList)
    }

    fun getProductById(productId: String) {
        val productCol = db.collection("Product")
        productCol.whereEqualTo("Product", productId)
            .get().addOnSuccessListener { documents ->
                for (document in documents) {
                    val id = document.id
                    val data = document.toObject<Product>()
                    val name = data.Name
                    val description = data.Description
                    val price = data.Price
                    val image = data.Image
                    val categoryId = data.CategoryId
                    val isSell = data.IsSell
                    val userId = data.UserId
                    val townId = data.TownId
                    val publishDate = data.PublishDate
                    val product = Product(id, name, description, price, image, categoryId, isSell, userId, townId, publishDate, true)
                    productList.add(product)
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error obteniendo productos: ", exception)
            }
    }*/
}