package com.esardo.a2ndhand

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.esardo.a2ndhand.databinding.ActivityUploadProductBinding
import com.esardo.a2ndhand.model.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Integer.min
import java.util.*

private const val MAX_IMAGES = 5
private const val PICK_IMAGE_REQUEST_CODE = 1
class UploadProduct : AppCompatActivity() {
    private lateinit var binding: ActivityUploadProductBinding

    private var imageUris = mutableListOf<Uri>()
    //private var imageUrls = mutableListOf<String>()
    val storage = FirebaseStorage.getInstance()
    val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ActivityUploadProductBinding.inflate(layoutInflater).also { binding = it }.root)

        var userId = ""
        var isSell = false
        val userRefV = intent.getSerializableExtra("vendo_fragment") as User?
        val userRefC = intent.getSerializableExtra("compro_fragment") as User?
        if(userRefV != null){
            userId = userRefV.id
            isSell = true
        } else {
            if (userRefC != null) {
                userId = userRefC.id
            }
        }

        val listener = View.OnClickListener { view ->
            // Aquí va la lógica que se ejecutará cuando se haga clic en alguno de los botones
            selectImages()
        }

        binding.ibUpPhotos1.setOnClickListener(listener)
        binding.ibUpPhotos2.setOnClickListener(listener)
        binding.ibUpPhotos3.setOnClickListener(listener)
        binding.ibUpPhotos4.setOnClickListener(listener)
        binding.ibUpPhotos5.setOnClickListener(listener)



        binding.btnUpload.setOnClickListener {
            uploadProduct(userId, isSell)
            /*val name = binding.etProduct.text.toString()
            val description = binding.etDescription.text.toString()
            val price = binding.etPrice.text.toString().toDouble()
            val image = ""
            val publishDate = Timestamp(Date())
            val category = binding.etCategory.text.toString()

            //Get CategoryId
            val categoryCol = FirebaseFirestore.getInstance().collection("Category")
            val query = categoryCol.whereEqualTo("Name", category)
            lifecycleScope.launch {
                val querySnapshot = query.get().await()
                val docSnapshot = querySnapshot.documents[0]
                val categoryId = docSnapshot.id

                //Get TownId
                val userDoc = FirebaseFirestore.getInstance().collection("User").document(userId)
                userDoc.get().addOnSuccessListener { user ->
                    if (user != null) {
                        val townId = user.getString("TownId")
                        if (townId != null) {
                            //Create new Product with the data obtained
                            val db = FirebaseFirestore.getInstance()
                            val product = hashMapOf(
                                "Name" to name,
                                "Description" to description,
                                "Price" to price,
                                "Image" to image,
                                "CategoryId" to categoryId,
                                "IsSell" to isSell,
                                "UserId" to userId,
                                "TownId" to townId,
                                "PublishDate" to publishDate
                            )

                            db.collection("Product").add(product)
                                .addOnSuccessListener {
                                    //Product upload completed
                                    finish()
                                }
                                .addOnFailureListener {
                                    //Error
                                    showMessage("Error al intentar subir el Producto")
                                }
                        }
                    }
                }
            }
            finish()*/
        }
    }

    //To select product images
    private fun selectImages() {
        val intent = Intent()//Intent.ACTION_PICK
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // Permitir seleccionar múltiples imágenes
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(
                intent,
                "Selecciona hasta 5 imágenes"
            ), PICK_IMAGE_REQUEST_CODE)//intent, PICK_IMAGE_REQUEST_CODE
    }
    //To save those images into a list
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        /*if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data
            if (imageUri != null) {
                imageUris.add(imageUri)
            }
        }*/
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            val clipData = data?.clipData
            if (clipData != null) {
                for (i in 0 until min(MAX_IMAGES, clipData.itemCount)) {
                    imageUris.add(clipData.getItemAt(i).uri)
                }
            } else {
                data?.data?.let { imageUri ->
                    imageUris.add(imageUri)
                }
            }
        }
    }

    //To upload product and images to the database
    private fun uploadProduct(userId: String, isSell: Boolean) {
        // Crea un nuevo documento para el producto en Firestore
        val name = binding.etProduct.text.toString()
        val description = binding.etDescription.text.toString()
        val price = binding.etPrice.text.toString().toDouble()
        val publishDate = Timestamp(Date())
        val category = binding.etCategory.text.toString()
        var uri1: Uri? = null
        var uri2: Uri? = null
        var uri3: Uri? = null
        var uri4: Uri? = null
        var uri5: Uri? = null
        when(imageUris.size) {
            0 -> {
                println("No hacemos nada")
            }
            1 -> {
                uri1 = imageUris[0]
            }
            2 -> {
                uri1 = imageUris[0]
                uri2 = imageUris[1]
            }
            3 -> {
                uri1 = imageUris[0]
                uri2 = imageUris[1]
                uri3 = imageUris[2]
            }
            4 -> {
                uri1 = imageUris[0]
                uri2 = imageUris[1]
                uri3 = imageUris[2]
                uri4 = imageUris[3]
            }
            5 -> {
                uri1 = imageUris[0]
                uri2 = imageUris[1]
                uri3 = imageUris[2]
                uri4 = imageUris[3]
                uri5 = imageUris[4]
            }
            else -> {
                uri1 = imageUris[0]
                uri2 = imageUris[1]
                uri3 = imageUris[2]
                uri4 = imageUris[3]
                uri5 = imageUris[4]
            }
        }

        var pic1 = ""
        var pic2 = ""
        var pic3 = ""
        var pic4 = ""
        var pic5 = ""

        //Get CategoryId
        val categoryCol = db.collection("Category")
        val query = categoryCol.whereEqualTo("Name", category)
        lifecycleScope.launch {
            val querySnapshot = query.get().await()
            val docSnapshot = querySnapshot.documents[0]
            val categoryId = docSnapshot.id

            //Get TownId
            val userDoc = db.collection("User").document(userId)
            userDoc.get().addOnSuccessListener { user ->
                if (user != null) {
                    val townId = user.getString("TownId")
                    if (townId != null) {
                        if (uri1 != null) {
                            val storageRef = storage.reference.child("images/${UUID.randomUUID()}")
                            val uploadTask = storageRef.putFile(uri1)
                            uploadTask.addOnSuccessListener { taskSnapshot ->
                                storageRef.downloadUrl.addOnSuccessListener { uri1 ->
                                    pic1 = uri1.toString()
                                    if(uri2 != null) {
                                        val storageRef = storage.reference.child("images/${UUID.randomUUID()}")
                                        val uploadTask = storageRef.putFile(uri2)
                                        uploadTask.addOnSuccessListener { taskSnapshot ->
                                            storageRef.downloadUrl.addOnSuccessListener { uri2 ->
                                                pic2 = uri2.toString()
                                                if(uri3 != null) {
                                                    val storageRef = storage.reference.child("images/${UUID.randomUUID()}")
                                                    val uploadTask = storageRef.putFile(uri3)
                                                    uploadTask.addOnSuccessListener { taskSnapshot ->
                                                        storageRef.downloadUrl.addOnSuccessListener { uri3 ->
                                                            pic3 = uri3.toString()
                                                            if(uri4 != null) {
                                                                val storageRef = storage.reference.child("images/${UUID.randomUUID()}")
                                                                val uploadTask = storageRef.putFile(uri4)
                                                                uploadTask.addOnSuccessListener { taskSnapshot ->
                                                                    storageRef.downloadUrl.addOnSuccessListener { uri4 ->
                                                                        pic4 = uri4.toString()
                                                                        if(uri5 != null) {
                                                                            val storageRef = storage.reference.child("images/${UUID.randomUUID()}")
                                                                            val uploadTask = storageRef.putFile(uri5)
                                                                            uploadTask.addOnSuccessListener { taskSnapshot ->
                                                                                storageRef.downloadUrl.addOnSuccessListener { uri5 ->
                                                                                    pic5 = uri5.toString()
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    //Create new Product with the data obtained
                                    val pictures = hashMapOf(
                                        "Pic1" to pic1,
                                        "Pic2" to pic2,
                                        "Pic3" to pic3,
                                        "Pic4" to pic4,
                                        "Pic5" to pic5
                                    )
                                    val product = hashMapOf(
                                        "Name" to name,
                                        "Description" to description,
                                        "Price" to price,
                                        "CategoryId" to categoryId,
                                        "IsSell" to isSell,
                                        "UserId" to userId,
                                        "TownId" to townId,
                                        "PublishDate" to publishDate
                                    )

                                    db.collection("Product").document().apply{
                                        set(product)
                                        collection("Picture").add(pictures)
                                            .addOnSuccessListener {
                                                //Product upload completed
                                                finish()
                                            }
                                            .addOnFailureListener {
                                                //Error
                                                showMessage("Error al intentar subir el Producto")
                                            }
                                    }
                                }
                            }
                            uploadTask.addOnFailureListener {
                                // Manejar error en caso de falla en la subida de imagen
                            }
                        } else {
                            //Create new Product with the data obtained
                            val product = hashMapOf(
                                "Name" to name,
                                "Description" to description,
                                "Price" to price,
                                "CategoryId" to categoryId,
                                "IsSell" to isSell,
                                "UserId" to userId,
                                "TownId" to townId,
                                "PublishDate" to publishDate
                            )

                            db.collection("Product").add(product)
                                .addOnSuccessListener {
                                    //Product upload completed
                                    finish()
                                }
                                .addOnFailureListener {
                                    //Error
                                    showMessage("Error al intentar subir el Producto")
                                }

                        }

                        //}

                    }


                }

            }
        }

    }

    private fun showMessage(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }
}