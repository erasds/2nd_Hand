package com.esardo.a2ndhand

import android.animation.Animator
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.esardo.a2ndhand.adapter.ItemAdapter
import com.esardo.a2ndhand.databinding.ActivityUploadProductBinding
import com.esardo.a2ndhand.model.Product
import com.esardo.a2ndhand.model.User
import com.esardo.a2ndhand.viewmodel.CategoryViewModel
import com.esardo.a2ndhand.viewmodel.ProductViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

private const val PICK_IMAGE_REQUEST_CODE = 1
class UploadProduct : AppCompatActivity() {
    private lateinit var binding: ActivityUploadProductBinding
    val context: Context = this

    val db = FirebaseFirestore.getInstance()

    private var imageUris = mutableListOf<Uri>()
    var selectedButtonId: Int = 0


    private lateinit var viewModel: ProductViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ActivityUploadProductBinding.inflate(layoutInflater).also { binding = it }.root)

        viewModel = ViewModelProvider(this)[ProductViewModel::class.java]

        var pictureId = ""
        //Para saber desde donde se ha abierto la actividad
        var userId = ""
        var isSell = false
        val userRefV = intent.getSerializableExtra("vendo_fragment") as User?
        val userRefC = intent.getSerializableExtra("compro_fragment") as User?
        val productId = intent.getSerializableExtra("productId") as String?
        if(userRefV != null){
            userId = userRefV.id
            isSell = true
        } else if (userRefC != null){
            userId = userRefC.id
        } else {
            if(!productId.isNullOrEmpty()) {
                //Estamos en modo edición
                //Cargamos los datos del producto en los campos del formulario
                val productCol = db.collection("Product").document(productId)
                productCol.get().addOnSuccessListener { prod ->
                    if(prod != null) {
                        val name = prod.getString("Name")
                        binding.etProduct.setText(name)
                        val description = prod.getString("Description")
                        if(description != "") binding.etDescription.setText(description)
                        val price = prod.getDouble("Price")
                        binding.etPrice.setText((price).toString().replace(".0", ""))
                        //Obtenemos categoría
                        val categoryId = prod.getString("CategoryId")
                        if(categoryId != "") {
                            val categoryCol = db.collection("Category").document(categoryId!!)
                            categoryCol.get().addOnSuccessListener { category ->
                                if(category != null) {
                                    val categoryName = category.getString("Name")
                                    binding.etCategory.setText(categoryName)
                                }

                                //Obtenemos las fotos
                                val picturesCol = productCol.collection("Picture")
                                picturesCol.get().addOnSuccessListener { documents ->
                                    for (document in documents) {
                                        pictureId = document.id
                                        val pic1 = document.getString("Pic1")
                                        if(pic1 != "") Picasso.get().load(pic1).placeholder(R.drawable.logo).error(R.drawable.logo).into(binding.iBtn1)
                                        val pic2 =  document.getString("Pic2")
                                        if(pic2 != "") Picasso.get().load(pic2).placeholder(R.drawable.logo).error(R.drawable.logo).into(binding.iBtn2)
                                        val pic3 =  document.getString("Pic3")
                                        if(pic3 != "") Picasso.get().load(pic3).placeholder(R.drawable.logo).error(R.drawable.logo).into(binding.iBtn3)
                                        val pic4 =  document.getString("Pic4")
                                        if(pic4 != "") Picasso.get().load(pic4).placeholder(R.drawable.logo).error(R.drawable.logo).into(binding.iBtn4)
                                        val pic5 =  document.getString("Pic5")
                                        if(pic5 != "") Picasso.get().load(pic5).placeholder(R.drawable.logo).error(R.drawable.logo).into(binding.iBtn5)
                                    }
                                }

                            }
                        }
                    }
                }

                //Cambiamos el texto del botón
                binding.btnUpload.text = "Actualizar"
            }
        }

        val onClickListener = View.OnClickListener { view ->
            //Cuando se haga clic en cualquiera de los botones
            selectedButtonId = view.id
            selectImage(selectedButtonId)
        }

        binding.iBtn1.setOnClickListener(onClickListener)
        binding.iBtn2.setOnClickListener(onClickListener)
        binding.iBtn3.setOnClickListener(onClickListener)
        binding.iBtn4.setOnClickListener(onClickListener)
        binding.iBtn5.setOnClickListener(onClickListener)

        //Lanza el diálogo para elegir la categoría
        binding.etCategory.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showDialog()
            }
        }

        //Al pulsarlo se guardan los datos y se cierra la actividad
        binding.btnUpload.setOnClickListener {
            val name = binding.etProduct.text.toString()
            val description = binding.etDescription.text.toString()
            val price = binding.etPrice.text.toString()
            val category = binding.etCategory.text.toString()
            if(name.isNotEmpty() && price.isNotEmpty() && category.isNotEmpty()) {
                if (productId != null) {
                    viewModel.updateProduct( //Actualizar producto
                        productId,
                        name,
                        description,
                        price,
                        category,
                        imageUris,
                        pictureId,
                        context
                    ).observe(this) { updatedProduct ->
                        if(updatedProduct != null) {
                            imageUris.clear()
                            //Después de actualizar el producto
                            val resultIntent = Intent()
                            resultIntent.putExtra("updatedProduct", updatedProduct)
                            setResult(Activity.RESULT_OK, resultIntent)
                            successAnimation(binding.ivSuccess, R.raw.success)
                        }
                    }
                } else {
                    viewModel.insertProduct( //Nuevo producto
                        name,
                        description,
                        price,
                        category,
                        imageUris,
                        userId,
                        isSell,
                        context
                    ).observe(this) { isFinished ->
                        if (isFinished) {
                            imageUris.clear()
                            successAnimation(binding.ivSuccess, R.raw.success)
                        }
                    }
                }
            } else {
                showMessage("Debe rellenar los campos obligatorios")
            }
        }
    }

    //Lanza la animación y cierra la actividad una vez termina
    private fun successAnimation(
        imageView: LottieAnimationView,
        animation: Int
    ) {
        imageView.setAnimation(animation)
        imageView.playAnimation()
        imageView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                //No se necesita implementación aquí
            }
            override fun onAnimationEnd(animation: Animator) {
                finish()
            }
            override fun onAnimationCancel(animation: Animator) {
                //No se necesita implementación aquí
            }
            override fun onAnimationRepeat(animation: Animator) {
                //No se necesita implementación aquí
            }
        })
    }


    //Para seleccionar las imágenes del teléfono
    private fun selectImage(buttonId: Int) {
        selectedButtonId = buttonId
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }
    //Guarda el path a las imágenes en una lista
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUris.add(data.data!!)
            //Convierte la Uri en un Bitmap para la miniatura
            val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, data.data)
            //Busca el botón correspondiente y configura el Bitmap como fuente de la imagen
            when (selectedButtonId) {
                R.id.iBtn1 -> binding.iBtn1.setImageBitmap(bitmap)
                R.id.iBtn2 -> binding.iBtn2.setImageBitmap(bitmap)
                R.id.iBtn3 -> binding.iBtn3.setImageBitmap(bitmap)
                R.id.iBtn4 -> binding.iBtn4.setImageBitmap(bitmap)
                R.id.iBtn5 -> binding.iBtn5.setImageBitmap(bitmap)
            }
        }
    }

    //Mustra el diálogo
    private fun showDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_selection)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        val recyclerView = dialog.findViewById<RecyclerView>(R.id.rvList)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        val viewModel = ViewModelProvider(this)[CategoryViewModel::class.java]
        lifecycleScope.launch {
            val categories = viewModel.getCategories()
            val adapter = ItemAdapter(categories)
            recyclerView.adapter = adapter
        }

        recyclerView.addOnItemClickListener { position, _, _, _ ->
            val category = (recyclerView.adapter as ItemAdapter).items[position]
            binding.etCategory.setText(category)
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

    //Para darle formato a los mensajes que se muestran por pantalla
    private fun showMessage(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        val resultIntent = Intent()
        setResult(Activity.RESULT_CANCELED, resultIntent)
        super.onBackPressed()
    }

}