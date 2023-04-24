package com.esardo.a2ndhand.viewmodel

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.esardo.a2ndhand.model.Picture
import com.esardo.a2ndhand.model.Product
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.Query

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
        val query = productCol.whereEqualTo("IsSell", isSell)
        query.addSnapshotListener { documents, exception ->
            if (exception != null) {
                Log.w(TAG, "Error obteniendo productos: ", exception)
                return@addSnapshotListener
            } else {
                if (documents != null) {
                    productList.clear()
                    for (document in documents) {
                        val id = document.id
                        val data = document.toObject<Product>()
                        if (favoriteList.contains(id)) data.isChecked = true
                        val name = data.Name
                        val description = data.Description
                        val price = data.Price
                        val categoryId = data.CategoryId
                        val isSell = data.IsSell
                        val userId = data.UserId
                        val townId = data.TownId
                        val publishDate = data.PublishDate
                        val product = Product(
                            id,
                            name,
                            description,
                            price,
                            Picture("", "", "", "", ""),
                            categoryId,
                            isSell,
                            userId,
                            townId,
                            publishDate,
                            data.isChecked
                        )
                        productList.add(product)
                    }

                    //Ahora recorremos los productos para consultar la subcolección de Picture
                    for (product in productList) {
                        val pictureCol = productCol.document(product.id).collection("Picture")
                        pictureCol.get().addOnSuccessListener { documents ->
                            for (document in documents) {
                                val picData = document.toObject<Picture>()
                                val pic1 = picData.Pic1
                                val pic2 = picData.Pic2
                                val pic3 = picData.Pic3
                                val pic4 = picData.Pic4
                                val pic5 = picData.Pic5
                                product.Picture.Pic1 = pic1
                                product.Picture.Pic2 = pic2
                                product.Picture.Pic3 = pic3
                                product.Picture.Pic4 = pic4
                                product.Picture.Pic5 = pic5
                            }
                        }
                    }

                    productLiveData.postValue(productList)
                }
            }
        }
    }

    fun getProductsByName(query: String) {
        val filteredList: List<Product> = productList.filter { it.Name.contains(query, ignoreCase = true) }
        productLiveData.postValue(filteredList)
    }

    suspend fun getCategories(): List<String> {
        val catCol = db.collection("Category").orderBy("Name").get().await()
        val categories = mutableListOf<String>()
        val allProducts = "Todos los productos"
        val publishDate = "Novedades"
        val townId = "Cerca de ti"
        categories.add(allProducts)
        categories.add(publishDate)
        categories.add(townId)
        for(document in catCol.documents) {
            val category = document.getString("Name")
            category?.let {
                categories.add(it)
            }
        }

        return categories
    }

    fun getProductsByFilter(category: String, userId: String, isSell: Boolean) {
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
        var query: Query? = null
        if(category.contains("Todos")) {
            //Llamamos a la función que carga todos los productos
            getAllProducts(isSell, userId)
        } else if(category.contains("Cerca")) {
            //Primero obtenemos el TownId del usuario
            var townId = ""
            val userCol = db.collection("User").document(userId)
            userCol.get().addOnSuccessListener { document ->
                townId = document.getString("TownId") ?: ""

                //Filtrar por TownId
                query = productCol.whereEqualTo("IsSell", isSell).whereEqualTo("TownId", townId)
                query!!.addSnapshotListener { documents, exception ->
                    if(exception != null) {
                        Log.w("TAG", "Listen failed.", exception)
                        return@addSnapshotListener
                    } else {
                        if (documents != null) {
                            productList.clear()
                            for(document in documents) {
                                val id = document.id
                                val data = document.toObject<Product>()
                                if (favoriteList.contains(id)) data.isChecked = true
                                val name = data.Name
                                val description = data.Description
                                val price = data.Price
                                val categoryId = data.CategoryId
                                val isSell = data.IsSell
                                val userId = data.UserId
                                val townId = data.TownId
                                val publishDate = data.PublishDate
                                val product = Product(
                                    id,
                                    name,
                                    description,
                                    price,
                                    Picture("", "", "", "", ""),
                                    categoryId,
                                    isSell,
                                    userId,
                                    townId,
                                    publishDate,
                                    data.isChecked
                                )
                                productList.add(product)
                            }

                            //Ahora recorremos los productos para consultar la subcolección de Picture
                            for (product in productList) {
                                val pictureCol = productCol.document(product.id).collection("Picture")
                                pictureCol.get().addOnSuccessListener { documents ->
                                    for (document in documents) {
                                        val picData = document.toObject<Picture>()
                                        val pic1 = picData.Pic1
                                        val pic2 = picData.Pic2
                                        val pic3 = picData.Pic3
                                        val pic4 = picData.Pic4
                                        val pic5 = picData.Pic5
                                        product.Picture.Pic1 = pic1
                                        product.Picture.Pic2 = pic2
                                        product.Picture.Pic3 = pic3
                                        product.Picture.Pic4 = pic4
                                        product.Picture.Pic5 = pic5
                                    }
                                }
                            }

                            productLiveData.postValue(productList)
                        }
                    }

                }
            }
        } else if(category.contains("Novedades")) {
            //Ordenamos por la fecha de publicación más reciente
            query = productCol.whereEqualTo("IsSell", isSell).orderBy("PublishDate", Query.Direction.DESCENDING)
            query!!.addSnapshotListener { documents, exception ->
                if(exception != null) {
                    Log.w("TAG", "Listen failed.", exception)
                    return@addSnapshotListener
                } else {
                    if (documents != null) {
                        productList.clear()
                        for(document in documents) {
                            val id = document.id
                            val data = document.toObject<Product>()
                            if (favoriteList.contains(id)) data.isChecked = true
                            val name = data.Name
                            val description = data.Description
                            val price = data.Price
                            val categoryId = data.CategoryId
                            val isSell = data.IsSell
                            val userId = data.UserId
                            val townId = data.TownId
                            val publishDate = data.PublishDate
                            val product = Product(
                                id,
                                name,
                                description,
                                price,
                                Picture("", "", "", "", ""),
                                categoryId,
                                isSell,
                                userId,
                                townId,
                                publishDate,
                                data.isChecked
                            )
                            productList.add(product)
                        }

                        //Ahora recorremos los productos para consultar la subcolección de Picture
                        for (product in productList) {
                            val pictureCol = productCol.document(product.id).collection("Picture")
                            pictureCol.get().addOnSuccessListener { documents ->
                                for (document in documents) {
                                    val picData = document.toObject<Picture>()
                                    val pic1 = picData.Pic1
                                    val pic2 = picData.Pic2
                                    val pic3 = picData.Pic3
                                    val pic4 = picData.Pic4
                                    val pic5 = picData.Pic5
                                    product.Picture.Pic1 = pic1
                                    product.Picture.Pic2 = pic2
                                    product.Picture.Pic3 = pic3
                                    product.Picture.Pic4 = pic4
                                    product.Picture.Pic5 = pic5
                                }
                            }
                        }

                        productLiveData.postValue(productList)
                    }
                }

            }
        } else {
            //Sacamos solo los productos de la categoría indicada
            val colCategory = db.collection("Category")
            colCategory.whereEqualTo("Name", category).get()
                .addOnSuccessListener { categories ->
                    var categoryId = ""
                    for (category in categories) {
                        categoryId = category.id
                    }

                    query = productCol.whereEqualTo("IsSell", isSell).whereEqualTo("CategoryId", categoryId)
                    query!!.addSnapshotListener { documents, exception ->
                        if(exception != null) {
                            Log.w("TAG", "Listen failed.", exception)
                            return@addSnapshotListener
                        } else {
                            if (documents != null) {
                                productList.clear()
                                for(document in documents) {
                                    val id = document.id
                                    val data = document.toObject<Product>()
                                    if (favoriteList.contains(id)) data.isChecked = true
                                    val name = data.Name
                                    val description = data.Description
                                    val price = data.Price
                                    val categoryId = data.CategoryId
                                    val isSell = data.IsSell
                                    val userId = data.UserId
                                    val townId = data.TownId
                                    val publishDate = data.PublishDate
                                    val product = Product(
                                        id,
                                        name,
                                        description,
                                        price,
                                        Picture("", "", "", "", ""),
                                        categoryId,
                                        isSell,
                                        userId,
                                        townId,
                                        publishDate,
                                        data.isChecked
                                    )
                                    productList.add(product)
                                }

                                //Ahora recorremos los productos para consultar la subcolección de Picture
                                for (product in productList) {
                                    val pictureCol = productCol.document(product.id).collection("Picture")
                                    pictureCol.get().addOnSuccessListener { documents ->
                                        for (document in documents) {
                                            val picData = document.toObject<Picture>()
                                            val pic1 = picData.Pic1
                                            val pic2 = picData.Pic2
                                            val pic3 = picData.Pic3
                                            val pic4 = picData.Pic4
                                            val pic5 = picData.Pic5
                                            product.Picture.Pic1 = pic1
                                            product.Picture.Pic2 = pic2
                                            product.Picture.Pic3 = pic3
                                            product.Picture.Pic4 = pic4
                                            product.Picture.Pic5 = pic5
                                        }
                                    }
                                }

                                productLiveData.postValue(productList)
                            }
                        }

                    }
                }
        }
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

            if(productIdList.isNotEmpty()) {
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
                            val categoryId = data.CategoryId
                            val isSell = data.IsSell
                            val userId = data.UserId
                            val townId = data.TownId
                            val publishDate = data.PublishDate
                            val product = Product(id, name, description, price, Picture("", "", "", "", ""), categoryId, isSell, userId, townId, publishDate, true)
                            productList.add(product)
                        }

                        //Ahora recorremos los productos para consultar la subcolección de Picture
                        for (product in productList) {
                            val pictureCol = db.collection("Product").document(product.id).collection("Picture")
                            pictureCol.get().addOnSuccessListener { documents ->
                                for(document in documents) {
                                    val picData = document.toObject<Picture>()
                                    val pic1 = picData.Pic1
                                    val pic2 = picData.Pic2
                                    val pic3 = picData.Pic3
                                    val pic4 = picData.Pic4
                                    val pic5 = picData.Pic5
                                    product.Picture.Pic1 = pic1
                                    product.Picture.Pic2 = pic2
                                    product.Picture.Pic3 = pic3
                                    product.Picture.Pic4 = pic4
                                    product.Picture.Pic5 = pic5
                                }
                            }
                        }

                        productLiveData.postValue(productList)

                    }.addOnFailureListener { exception ->
                        Log.d(TAG, "Error getting products: ", exception)
                        // Si ocurre un error, podemos devolver una lista vacía o manejar el error de otra manera
                        productLiveData.postValue(emptyList())
                    }
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
                    val categoryId = data.CategoryId
                    val isSell = data.IsSell
                    val userId = data.UserId
                    val townId = data.TownId
                    val publishDate = data.PublishDate
                    val product = Product(id, name, description, price, Picture("", "", "", "", ""), categoryId, isSell, userId, townId, publishDate, data.isChecked)
                    productList.add(product)
                }

                //Ahora recorremos los productos para consultar la subcolección de Picture
                for (product in productList) {
                    val pictureCol = productCol.document(product.id).collection("Picture")
                    pictureCol.get().addOnSuccessListener { documents ->
                        for(document in documents) {
                            val picData = document.toObject<Picture>()
                            val pic1 = picData.Pic1
                            val pic2 = picData.Pic2
                            val pic3 = picData.Pic3
                            val pic4 = picData.Pic4
                            val pic5 = picData.Pic5
                            product.Picture.Pic1 = pic1
                            product.Picture.Pic2 = pic2
                            product.Picture.Pic3 = pic3
                            product.Picture.Pic4 = pic4
                            product.Picture.Pic5 = pic5
                        }
                    }
                }
                productLiveData.postValue(productList)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error obteniendo productos: ", exception)
            }
    }

}