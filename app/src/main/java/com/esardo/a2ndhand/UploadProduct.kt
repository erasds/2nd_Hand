package com.esardo.a2ndhand

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.esardo.a2ndhand.databinding.ActivityUploadProductBinding
import com.esardo.a2ndhand.model.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class UploadProduct : AppCompatActivity() {
    private lateinit var binding: ActivityUploadProductBinding

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

        binding.btnUpload.setOnClickListener {
            val name = binding.etProduct.text.toString()
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
            finish()
        }
    }

    private fun showMessage(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }
}