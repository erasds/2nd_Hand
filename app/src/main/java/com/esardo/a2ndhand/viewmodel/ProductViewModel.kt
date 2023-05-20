package com.esardo.a2ndhand.viewmodel

import android.Manifest
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.esardo.a2ndhand.model.Picture
import com.esardo.a2ndhand.model.Product
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

private const val PERMISSION_REQUEST_CODE = 123
class ProductViewModel: ViewModel() {
    var productLiveData: MutableLiveData<List<Product>?> = MutableLiveData()

    var productList = mutableListOf<Product>()
    private var favoriteList = mutableListOf<String>()
    var productIdList = mutableListOf<String>()
    private var picturesList = mutableListOf<String>()

    val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun getAllProductObserver(): MutableLiveData<List<Product>?> {
        return productLiveData
    }

    fun insertProduct(
        name: String,
        description: String,
        price: String,
        category: String,
        imageUris: List<Uri>,
        userId: String,
        isSell: Boolean,
        context: Context
    ): LiveData<Boolean>
    {
        val isProductUploadedSuccessfully = MutableLiveData<Boolean>()
        val priceD = price.toDouble()
        // Crea un nuevo documento para el producto en Firestore
        val publishDate = Timestamp(Date())
        var pic1 = ""
        var pic2 = ""
        var pic3 = ""
        var pic4 = ""
        var pic5 = ""

        //Get CategoryId
        val categoryCol = db.collection("Category")
        val query = categoryCol.whereEqualTo("Name", category)
        viewModelScope.launch {
            val querySnapshot = query.get().await()
            val docSnapshot = querySnapshot.documents[0]
            val categoryId = docSnapshot.id

            //Get TownId
            val userDoc = db.collection("User").document(userId)
            userDoc.get().addOnSuccessListener { user ->
                if (user != null) {
                    val townId = user.getString("TownId")
                    if (townId != null) {

                        //Create new Product with the data obtained
                        val product = hashMapOf(
                            "Name" to name,
                            "Description" to description,
                            "Price" to priceD,
                            "CategoryId" to categoryId,
                            "IsSell" to isSell,
                            "UserId" to userId,
                            "TownId" to townId,
                            "PublishDate" to publishDate
                        )

                        if(imageUris.isNotEmpty()) {
                            CoroutineScope(Dispatchers.IO).launch {
                                val permission = Manifest.permission.READ_EXTERNAL_STORAGE
                                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(context as Activity, arrayOf(permission), PERMISSION_REQUEST_CODE)
                                    return@launch
                                }
                                val deferredList = imageUris.map { image ->
                                    val storageRef = storage.reference.child("images/${UUID.randomUUID()}")
                                    val uploadTask = storageRef.putFile(image)
                                    uploadTask.await() // esperar a que la subida del archivo termine
                                    storageRef.downloadUrl.await() // esperar a que se genere la URL de descarga
                                }
                                picturesList.clear()
                                picturesList.addAll(deferredList.map { it.toString() })

                                when(picturesList.size) {
                                    0 -> {
                                        println("No hacemos nada")
                                    }
                                    1 -> {
                                        pic1 = picturesList[0]
                                    }
                                    2 -> {
                                        pic1 = picturesList[0]
                                        pic2 = picturesList[1]
                                    }
                                    3 -> {
                                        pic1 = picturesList[0]
                                        pic2 = picturesList[1]
                                        pic3 = picturesList[2]
                                    }
                                    4 -> {
                                        pic1 = picturesList[0]
                                        pic2 = picturesList[1]
                                        pic3 = picturesList[2]
                                        pic4 = picturesList[3]
                                    }
                                    else -> {
                                        pic1 = picturesList[0]
                                        pic2 = picturesList[1]
                                        pic3 = picturesList[2]
                                        pic4 = picturesList[3]
                                        pic5 = picturesList[4]
                                    }
                                }

                                //Create new Picture with the data obtained
                                val pictures = hashMapOf(
                                    "Pic1" to pic1,
                                    "Pic2" to pic2,
                                    "Pic3" to pic3,
                                    "Pic4" to pic4,
                                    "Pic5" to pic5
                                )

                                db.collection("Product").document().apply{
                                    set(product)
                                    collection("Picture").add(pictures)
                                        .addOnSuccessListener {
                                            //Product upload completed
                                            isProductUploadedSuccessfully.postValue(true)
                                            Log.d(TAG, "Producto y fotos insertadas con éxtio")

                                        }
                                        .addOnFailureListener { e ->
                                            isProductUploadedSuccessfully.postValue(false)
                                            Log.d(TAG, "Se ha producido un error al intentar insertar el producto y las fotos",e)
                                        }
                                }
                            }
                        } else {
                            db.collection("Product").add(product)
                                .addOnSuccessListener {
                                    //Product upload completed
                                    isProductUploadedSuccessfully.postValue(true)
                                    Log.d(TAG, "Producto insertado con éxtio")
                                }
                                .addOnFailureListener { e ->
                                    isProductUploadedSuccessfully.postValue(false)
                                    Log.d(TAG, "Se ha producido un error al intentar insertar el producto",e)
                                }

                        }

                    }
                }
            }
        }
        return isProductUploadedSuccessfully
    }

    fun updateProduct(
        productId: String?,
        name: String,
        description: String,
        price: String,
        category: String,
        imageUris: List<Uri>,
        pictureId: String,
        context: Context
    ): LiveData<Boolean>
    {
        val isProductUploadedSuccessfully = MutableLiveData<Boolean>()
        val priceD = price.toDouble()
        // Crea un nuevo documento para el producto en Firestore
        val publishDate = Timestamp(Date())
        var pic1 = ""
        var pic2 = ""
        var pic3 = ""
        var pic4 = ""
        var pic5 = ""
        var isSell: Boolean? = false
        var userId: String? = ""
        var townId: String? = ""

        //Get CategoryId
        val categoryCol = db.collection("Category")
        val query = categoryCol.whereEqualTo("Name", category)
        viewModelScope.launch {
            val querySnapshot = query.get().await()
            val docSnapshot = querySnapshot.documents[0]
            val categoryId = docSnapshot.id

            //Get isSell, userId and TownId
            val prodCol = db.collection("Product").document(productId!!)
            prodCol.get().addOnSuccessListener { prod ->
                if(prod != null) {
                    isSell = prod.getBoolean("IsSell")
                    userId = prod.getString("UserId")
                    townId = prod.getString("TownId")
                }

                //Create new Product with the data obtained
                val productUpdated = hashMapOf(
                    "Name" to name,
                    "Description" to description,
                    "Price" to priceD,
                    "CategoryId" to categoryId,
                    "IsSell" to isSell,
                    "UserId" to userId,
                    "TownId" to townId,
                    "PublishDate" to publishDate
                )

                if(imageUris.isNotEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val permission = Manifest.permission.READ_EXTERNAL_STORAGE
                        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(context as Activity, arrayOf(permission), PERMISSION_REQUEST_CODE)
                            return@launch
                        }
                        val deferredList = imageUris.map { image ->
                            val storageRef = storage.reference.child("images/${UUID.randomUUID()}")
                            val uploadTask = storageRef.putFile(image)
                            uploadTask.await() // esperar a que la subida del archivo termine
                            storageRef.downloadUrl.await() // esperar a que se genere la URL de descarga
                        }
                        picturesList.clear()
                        picturesList.addAll(deferredList.map { it.toString() })

                        when(picturesList.size) {
                            1 -> {
                                pic1 = picturesList[0]
                            }
                            2 -> {
                                pic1 = picturesList[0]
                                pic2 = picturesList[1]
                            }
                            3 -> {
                                pic1 = picturesList[0]
                                pic2 = picturesList[1]
                                pic3 = picturesList[2]
                            }
                            4 -> {
                                pic1 = picturesList[0]
                                pic2 = picturesList[1]
                                pic3 = picturesList[2]
                                pic4 = picturesList[3]
                            }
                            else -> {
                                pic1 = picturesList[0]
                                pic2 = picturesList[1]
                                pic3 = picturesList[2]
                                pic4 = picturesList[3]
                                pic5 = picturesList[4]
                            }
                        }

                        //Create new Picture with the data obtained
                        val pictures = hashMapOf(
                            "Pic1" to pic1,
                            "Pic2" to pic2,
                            "Pic3" to pic3,
                            "Pic4" to pic4,
                            "Pic5" to pic5
                        )

                        db.collection("Product").document(productId).apply{
                            set(productUpdated)
                                .addOnSuccessListener {
                                    db.collection("Product").document(productId).
                                    collection("Picture").document(pictureId).apply {
                                        set(pictures)
                                            .addOnSuccessListener {
                                                //Product upload completed
                                                isProductUploadedSuccessfully.postValue(true)
                                                Log.d(
                                                    TAG,
                                                    "Producto y fotos actualizadas con éxito"
                                                )
                                            }
                                    }

                                }
                                .addOnFailureListener { e ->
                                    isProductUploadedSuccessfully.postValue(false)
                                    Log.d(TAG, "Se ha producido un error al intentar actualizar el producto y las fotos",e)
                                }
                        }
                    }
                } else {
                    db.collection("Product").document(productId).apply {
                        set(productUpdated)
                            .addOnSuccessListener {
                                //Product upload completed
                                isProductUploadedSuccessfully.postValue(true)
                                Log.d(TAG, "Producto actualizado con éxito")
                            }
                            .addOnFailureListener { e ->
                                isProductUploadedSuccessfully.postValue(false)
                                Log.d(TAG, "Se ha producido un error al intentar actualizar el producto",e)
                            }
                    }
                }
            }
        }
        return isProductUploadedSuccessfully
    }

    //Función que obtiene todos los productos filtrados por si son en venta o en demanda
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
                            // Asignar el productId al objeto Product
                            data.id = id

                            if (favoriteList.contains(id)) data.isChecked = true
                            val pictureCol = productCol.document(id).collection("Picture")

                            pictureCol.get().addOnSuccessListener { imageDocuments ->
                                var image: Picture? = null

                                for (imageDocument in imageDocuments) {
                                    val picId = imageDocument.id
                                    val picData = imageDocument.toObject<Picture>()
                                    val pic1 = picData.Pic1
                                    val pic2 = picData.Pic2
                                    val pic3 = picData.Pic3
                                    val pic4 = picData.Pic4
                                    val pic5 = picData.Pic5

                                    image = Picture(picId, pic1, pic2, pic3, pic4, pic5)
                                }

                                // Si hay alguna imagen la asignamos al objeto Product
                                if (image != null) {
                                    data.Picture = image
                                }
                                // Agregamos el objeto Product a la lista
                                productList.add(data)
                                // Actualizamos la lista y publicamos los cambios
                                productLiveData.postValue(productList)
                            }
                        }
                    }
                }
            }
        }.addOnFailureListener { exception ->
            Log.w(TAG, "Error cargando favoritos: ", exception)
        }
    }

    //Obtiene los productos filtrando por el texto del SearchView
    fun getProductsByName(query: String) {
        val filteredList: List<Product> = productList.filter { it.Name.contains(query, ignoreCase = true) }
        productLiveData.postValue(filteredList)
    }

    //Obtiene las categorías para poder filtrar los productos
    suspend fun getCategories(): List<String> {
        val catCol = db.collection("Category").orderBy("Name").get().await()
        val categories = mutableListOf<String>()
        //Además de las categorías añadimos estas 3 opciones
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

    //Aplica el filtro a la lista de productos según lo que hayan seleccionado
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

            val productCol = db.collection("Product")
            var query: Query? = null
            //Si contiene Todos tiene que cargar todos los productos
            if(category.contains("Todos")) {
                //Llamamos a la función que carga todos los productos
                getAllProducts(isSell, userId)
            //Si contiene Cerca hay que sacar los de la ciudad del usuario que ha iniciado sesión
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
                            Log.w("TAG", "Error obteniendo productos: ", exception)
                            return@addSnapshotListener
                        } else {
                            if (documents != null) {
                                productList.clear()
                                for (document in documents) {
                                    val id = document.id
                                    val data = document.toObject<Product>()
                                    // Asignar el productId al objeto Product
                                    data.id = id

                                    if (favoriteList.contains(id)) data.isChecked = true
                                    val pictureCol = productCol.document(id).collection("Picture")

                                    pictureCol.get().addOnSuccessListener { imageDocuments ->
                                        var image: Picture? = null

                                        for (imageDocument in imageDocuments) {
                                            val picId = imageDocument.id
                                            val picData = imageDocument.toObject<Picture>()
                                            val pic1 = picData.Pic1
                                            val pic2 = picData.Pic2
                                            val pic3 = picData.Pic3
                                            val pic4 = picData.Pic4
                                            val pic5 = picData.Pic5

                                            image = Picture(picId, pic1, pic2, pic3, pic4, pic5)
                                        }

                                        //Si hay alguna imagen la asignamos al objeto Product
                                        if (image != null) {
                                            data.Picture = image
                                        }
                                        //Agregamos el objeto Product a la lista
                                        productList.add(data)
                                        //Actualizamos la lista y publicamos los cambios
                                        productLiveData.postValue(productList)
                                    }
                                }
                            }
                        }

                    }
                }
            //Si contiene Novedades hay que ordenarlos por los más recientes
            } else if(category.contains("Novedades")) {
                //Ordenamos por la fecha de publicación más reciente
                query = productCol.whereEqualTo("IsSell", isSell).orderBy("PublishDate", Query.Direction.DESCENDING)
                query!!.addSnapshotListener { documents, exception ->
                    if(exception != null) {
                        Log.w("TAG", "Error obteniendo productos: ", exception)
                        return@addSnapshotListener
                    } else {
                        if (documents != null) {
                            productList.clear()
                            for (document in documents) {
                                val id = document.id
                                val data = document.toObject<Product>()
                                // Asignar el productId al objeto Product
                                data.id = id

                                if (favoriteList.contains(id)) data.isChecked = true
                                val pictureCol = productCol.document(id).collection("Picture")

                                pictureCol.get().addOnSuccessListener { imageDocuments ->
                                    var image: Picture? = null

                                    for (imageDocument in imageDocuments) {
                                        val picId = imageDocument.id
                                        val picData = imageDocument.toObject<Picture>()
                                        val pic1 = picData.Pic1
                                        val pic2 = picData.Pic2
                                        val pic3 = picData.Pic3
                                        val pic4 = picData.Pic4
                                        val pic5 = picData.Pic5

                                        image = Picture(picId, pic1, pic2, pic3, pic4, pic5)
                                    }

                                    //Si hay alguna imagen la asignamos al objeto Product
                                    if (image != null) {
                                        data.Picture = image
                                    }
                                    //Agregamos el objeto Product a la lista
                                    productList.add(data)
                                    //Actualizamos la lista y publicamos los cambios
                                    productLiveData.postValue(productList)
                                }
                            }
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
                                Log.w("TAG", "Error obteniendo productos: ", exception)
                                return@addSnapshotListener
                            } else {
                                if (documents != null) {
                                    productList.clear()
                                    for (document in documents) {
                                        val id = document.id
                                        val data = document.toObject<Product>()
                                        // Asignar el productId al objeto Product
                                        data.id = id

                                        if (favoriteList.contains(id)) data.isChecked = true
                                        val pictureCol = productCol.document(id).collection("Picture")

                                        pictureCol.get().addOnSuccessListener { imageDocuments ->
                                            var image: Picture? = null

                                            for (imageDocument in imageDocuments) {
                                                val picId = imageDocument.id
                                                val picData = imageDocument.toObject<Picture>()
                                                val pic1 = picData.Pic1
                                                val pic2 = picData.Pic2
                                                val pic3 = picData.Pic3
                                                val pic4 = picData.Pic4
                                                val pic5 = picData.Pic5

                                                image = Picture(picId, pic1, pic2, pic3, pic4, pic5)
                                            }

                                            //Si hay alguna imagen la asignamos al objeto Product
                                            if (image != null) {
                                                data.Picture = image
                                            }
                                            //Agregamos el objeto Product a la lista
                                            productList.add(data)
                                            //Actualizamos la lista y publicamos los cambios
                                            productLiveData.postValue(productList)

                                        }
                                    }
                                }
                            }
                        }
                    }
            }
        }.addOnFailureListener { exception ->
            Log.w(TAG, "Error cargando favoritos: ", exception)
        }
    }

    //Elimina un producto de la colección Favorite al desmarcar el checkbox
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

    //Añade un producto a la colección Favorite al marcar el checkbox
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

    //Función que carga los productos que tenga marcados como favoritos el usuario que ha iniciado sesión
    fun getMyFavorites(userId: String) {
        productList.clear()
        val favCol = db.collection("User").document(userId)
            .collection("Favorite")
        favCol.get().addOnSuccessListener { result  ->
            productIdList.clear()
            for (document in result) {
                val productId = document.getString("ProductId")
                if (productId != null && productId.isNotEmpty()) {
                    productIdList.add(productId)
                }
            }

            if(productIdList.isNotEmpty()) {
                // Ahora que tenemos los IDs de los productos, realizamos una segunda consulta a la colección Product
                val productCol = db.collection("Product")
                val query = productCol.whereIn(FieldPath.documentId(), productIdList)
                query.addSnapshotListener { documents, exception ->
                    if (exception != null) {
                        Log.w(TAG, "Error obteniendo productos: ", exception)
                        return@addSnapshotListener
                    } else {
                        if(documents != null) {
                            productList.clear()
                            for (document in documents) {
                                val id = document.id
                                val data = document.toObject<Product>()
                                // Asignar el productId al objeto Product
                                data.id = id

                                data.isChecked = true
                                val pictureCol = productCol.document(id).collection("Picture")

                                pictureCol.get().addOnSuccessListener { imageDocuments ->
                                    var image: Picture? = null

                                    for (imageDocument in imageDocuments) {
                                        val picId = imageDocument.id
                                        val picData = imageDocument.toObject<Picture>()
                                        val pic1 = picData.Pic1
                                        val pic2 = picData.Pic2
                                        val pic3 = picData.Pic3
                                        val pic4 = picData.Pic4
                                        val pic5 = picData.Pic5

                                        image = Picture(picId, pic1, pic2, pic3, pic4, pic5)
                                    }

                                    //Si hay alguna imagen la asignamos al objeto Product
                                    if (image != null) {
                                        data.Picture = image
                                    }
                                    //Agregamos el objeto Product a la lista
                                    productList.add(data)
                                    //Actualizamos la lista y publicamos los cambios
                                    productLiveData.postValue(productList)
                                }
                            }
                        }
                    }
                }
            }
        }.addOnFailureListener { exception ->
            Log.d(TAG, "Error getting product IDs: ", exception)
            // Si ocurre un error, se devuelve una lista vacía
            productLiveData.postValue(emptyList())
        }
    }

    //Función que carga los productos que pertenecen al usuario que ha iniciado sesión
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

            val productCol = db.collection("Product")
            val query = productCol.whereEqualTo("UserId", userId)
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
                            // Asignar el productId al objeto Product
                            data.id = id

                            if (favoriteList.contains(id)) data.isChecked = true
                            val pictureCol = productCol.document(id).collection("Picture")

                            pictureCol.get().addOnSuccessListener { imageDocuments ->
                                var image: Picture? = null

                                for (imageDocument in imageDocuments) {
                                    val picId = imageDocument.id
                                    val picData = imageDocument.toObject<Picture>()
                                    val pic1 = picData.Pic1
                                    val pic2 = picData.Pic2
                                    val pic3 = picData.Pic3
                                    val pic4 = picData.Pic4
                                    val pic5 = picData.Pic5

                                    image = Picture(picId, pic1, pic2, pic3, pic4, pic5)
                                }

                                //Si hay alguna imagen la asignamos al objeto Product
                                if (image != null) {
                                    data.Picture = image
                                }
                                //Agregamos el objeto Product a la lista
                                productList.add(data)
                                //Actualizamos la lista y publicamos los cambios
                                productLiveData.postValue(productList)
                            }
                        }
                    }
                }
            }
        }.addOnFailureListener { exception ->
            Log.w(TAG, "Error cargando favoritos: ", exception)
        }
    }
}