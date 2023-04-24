package com.esardo.a2ndhand

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.View.GONE
import android.view.WindowManager
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.esardo.a2ndhand.adapter.ItemAdapter
import com.esardo.a2ndhand.databinding.ActivitySignInBinding
import com.esardo.a2ndhand.model.User
import com.esardo.a2ndhand.viewmodel.TownViewModel
import com.esardo.a2ndhand.viewmodel.UserViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import org.bouncycastle.jcajce.provider.digest.SHA256
import java.nio.charset.StandardCharsets

private const val PICK_IMAGE_REQUEST_CODE = 1
class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding

    val db = FirebaseFirestore.getInstance()
    private var imageUri: Uri? = null

    private lateinit var userId: String

    private lateinit var viewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ActivitySignInBinding.inflate(layoutInflater).also { binding = it }.root)

        val userRef = intent?.getSerializableExtra("object") as? User

        viewModel = ViewModelProvider(this)[UserViewModel::class.java]

        //Comprobar si estamos en modo edición
        if(userRef != null) {
            //Obtenemos y cargamos los datos del usuario en los campos del formulario
            val userID = userRef?.id
            if (userID != null) {
                userId = userID
            }
            val userCol = db.collection("User").document(userId)
            userCol.get().addOnSuccessListener { user ->
                if(user != null) {
                    val picture = user.getString("Picture")
                    if(picture != null) {
                        Picasso.get().load(picture).placeholder(R.drawable.prueba).error(R.drawable.prueba).into(binding.ibUpUserPhoto)
                    }
                    val userName = user.getString("User")
                    binding.etUser.setText(userName)
                    binding.etPassword.visibility = GONE
                    val name = user.getString("Name")
                    if(name != null) binding.etName.setText(name)
                    val surname = user.getString("Surname")
                    if(surname != null) binding.etSurname.setText(surname)
                    binding.etMail.visibility = GONE
                    val phone = user.getString("Phone")
                    if(phone != null) binding.etPhone.setText(phone)
                    val townId = user.getString("TownId")
                    if(townId != null) {
                        val townCol = db.collection("Town").document(townId)
                        townCol.get().addOnSuccessListener { town ->
                            if(town != null) {
                                val townName = town.getString("Name")
                                binding.etTown.setText(townName)
                            }
                        }
                    }
                }
            }

            //Cambiamos el texto del botón
            binding.btnSignIn.text = "Actualizar"

            //Mostramos la opción de eliminar cuenta?

        }

        binding.ibUpUserPhoto.setOnClickListener {
            selectImage()
        }

        binding.etTown.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showDialog()
            }
        }

        binding.btnSignIn.setOnClickListener {
            val userName = binding.etUser.text.toString()
            val name = binding.etName.text.toString()
            val surname = binding.etSurname.text.toString()
            val phone = binding.etPhone.text.toString()
            val town = binding.etTown.text.toString()
            if(userRef != null) { //Modo edición
                if(userName.isNotEmpty() && town.isNotEmpty()) {
                    viewModel.updateUser(
                        userId,
                        userName,
                        name,
                        surname,
                        phone,
                        town,
                        imageUri).observe(this) { isUpdated ->
                        if (isUpdated) {
                            finish()
                        }
                    }
                } else {
                    showMessage("Debe rellenar los campos obligatorios")
                }
            } else { //Modo registro
                val password = binding.etPassword.text.toString()
                val encryptedPassword = encryptPassword(password)
                val email = binding.etMail.text.toString()
                if(userName.isNotEmpty() && password.isNotEmpty() && email.isNotEmpty() && town.isNotEmpty()) {
                    viewModel.isMailAlreadyRegistered(email) { registered ->
                        if(registered) {
                            showMessage("El correo electrónico ya existe")
                        } else {
                            if(password.length < 6){
                                showMessage("La contraseña debe tener al menos 6 caracteres")
                            } else {
                                viewModel.registerUser(
                                    userName,
                                    password,
                                    encryptedPassword,
                                    name,
                                    surname,
                                    email,
                                    phone,
                                    town,
                                    imageUri
                                ).observe(this) { isRegistered ->
                                    if (isRegistered) {
                                        finish()
                                    }
                                }
                            }
                        }
                    }
                } else {
                    showMessage("Debe rellenar los campos obligatorios")
                }
            }
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }
    //To save the image path into a variable
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            //Convierte la Uri en un Bitmal para la miniatura
            val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            //Configura el Bitmap como fuente de la imagen en el ImageButton
            binding.ibUpUserPhoto.setImageBitmap(bitmap)
        }
    }

    private fun encryptPassword(password: String): String {
        val passwordBytes = password.toByteArray(StandardCharsets.UTF_8)
        val messageDigest = SHA256.Digest()
        val passwordHashBytes = messageDigest.digest(passwordBytes)
        return passwordHashBytes.joinToString("") { String.format("%02x", it) }
    }

    private fun showDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_selection)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        val recyclerView = dialog.findViewById<RecyclerView>(R.id.rvList)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        val viewModel = ViewModelProvider(this)[TownViewModel::class.java]
        lifecycleScope.launch {
            val towns = viewModel.getTowns()
            val adapter = ItemAdapter(towns)
            recyclerView.adapter = adapter
        }

        recyclerView.addOnItemClickListener { position, _, _, _ ->
            val town = (recyclerView.adapter as ItemAdapter).items[position]
            binding.etTown.setText(town)
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

    private fun showMessage(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }
}