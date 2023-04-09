package com.esardo.a2ndhand

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.esardo.a2ndhand.databinding.ActivityUploadProductBinding
import com.esardo.a2ndhand.model.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Integer.min
import java.util.*

private const val MAX_IMAGES = 5
private const val PICK_IMAGE_REQUEST_CODE = 1
private const val PERMISSION_REQUEST_CODE = 123
class UploadProduct : AppCompatActivity() {
    private lateinit var binding: ActivityUploadProductBinding
    val context: Context = this

    private var imageUris = mutableListOf<Uri>()
    //private var imageUrls = mutableListOf<String>()
    val storage = FirebaseStorage.getInstance()
    val db = FirebaseFirestore.getInstance()
    private var picturesList = mutableListOf<String>()

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

        binding.ibUpPhotos.setOnClickListener {
            selectImages()
        }

        binding.btnUpload.setOnClickListener {
            uploadProduct(userId, isSell)
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
                "Selecciona hasta $MAX_IMAGES imágenes"
            ), PICK_IMAGE_REQUEST_CODE)//intent, PICK_IMAGE_REQUEST_CODE
    }
    //To save those images into a list
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

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
                                            finish()
                                        }
                                        .addOnFailureListener {
                                            //Error
                                            showMessage("Error al intentar subir el Producto")
                                        }
                                }
                            }
                        } else {
                            db.collection("Product").add(product)
                                .addOnSuccessListener {
                                    //Product uploaded completed
                                    finish()
                                }
                                .addOnFailureListener {
                                    //Error
                                    showMessage("Error al intentar crear el Usuario")
                                }
                        }

                    }
                }
            }
        }

    }

    private fun showMessage(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }
}