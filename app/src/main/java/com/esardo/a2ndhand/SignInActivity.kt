package com.esardo.a2ndhand

import android.animation.Animator
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.esardo.a2ndhand.adapter.ItemAdapter
import com.esardo.a2ndhand.databinding.ActivitySignInBinding
import com.esardo.a2ndhand.model.User
import com.esardo.a2ndhand.viewmodel.TownViewModel
import com.esardo.a2ndhand.viewmodel.UserViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import org.bouncycastle.jcajce.provider.digest.SHA256
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding

    val db = FirebaseFirestore.getInstance()
    private var imageUri: Uri? = null

    private lateinit var userId: String

    private lateinit var viewModel: UserViewModel

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ActivitySignInBinding.inflate(layoutInflater).also { binding = it }.root)

        viewModel = ViewModelProvider(this)[UserViewModel::class.java]

        //Recibimos el userId del ProfileFragment
        val userRef = intent?.getSerializableExtra("object") as? User

        //Comprobamos si estamos en modo edición
        if(userRef != null) {
            //Obtenemos y cargamos los datos del usuario en los campos del formulario
            val userID = userRef.id
            userId = userID
            val userCol = db.collection("User").document(userId)
            userCol.get().addOnSuccessListener { user ->
                if(user != null) {
                    val picture = user.getString("Picture")
                    if(picture != "") {
                        Picasso.get().load(picture).placeholder(R.drawable.profile).into(binding.ibUpUserPhoto)
                    }
                    val userName = user.getString("User")
                    binding.etUser.setText(userName)
                    binding.tiPassword.visibility = GONE
                    val name = user.getString("Name")
                    if(name != null) binding.etName.setText(name)
                    val surname = user.getString("Surname")
                    if(surname != null) binding.etSurname.setText(surname)
                    binding.tiMail.visibility = GONE
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
        }

        //Al pulsarlo llama a la función de seleccionar imagen
        binding.ibUpUserPhoto.setOnClickListener {
            selectImage()
        }

        //Al pulsarlo inicia un diálogo para poder elegir Ciudad
        binding.etTown.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showDialog()
            }
        }

        //Al pulsarlo envía los datos del formulario a la bbdd
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
                            successAnimation(binding.ivSuccess, R.raw.success)
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
                                        successAnimation(binding.ivSuccess, R.raw.success)
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

    //Lanza la animación y cierra la actividad una vez termina
    private fun successAnimation(
        imageView: LottieAnimationView,
        animation: Int,
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

    //Para decodificar el bitmap de la imagen de forma 'manual'
    private fun decodeBitmap(uri: Uri): Bitmap? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options()
            options.inSampleSize = 2
            BitmapFactory.decodeStream(inputStream, null, options)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    //Guarda el bitmap de la imagen para poder mostrarla en el imageview
    @RequiresApi(Build.VERSION_CODES.P)
    private val someActivityResultLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri

            val bitmap: Bitmap? = decodeBitmap(imageUri!!)
            if (bitmap != null) {
                binding.ibUpUserPhoto.setImageBitmap(bitmap)
            }
        }
    }
    //Abre el explorador del teléfono y permite seleccionar una imagen
    @RequiresApi(Build.VERSION_CODES.P)
    private fun selectImage() {
        someActivityResultLauncher.launch("image/*")
    }
    //Guarda la instancia del bitmap
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable("image_uri", imageUri)
        super.onSaveInstanceState(outState)
    }
    //Cuando se vuelve a crear la actividad carga la instancia guardada del bitmap
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        imageUri = savedInstanceState.getParcelable("image_uri")
        if (imageUri != null) {
            val bitmap: Bitmap? = decodeBitmap(imageUri!!)
            if (bitmap != null) {
                binding.ibUpUserPhoto.setImageBitmap(bitmap)
            }
        }
    }

    //Función para encriptar la contraseña
    private fun encryptPassword(password: String): String {
        val passwordBytes = password.toByteArray(StandardCharsets.UTF_8)
        val messageDigest = SHA256.Digest()
        val passwordHashBytes = messageDigest.digest(passwordBytes)
        return passwordHashBytes.joinToString("") { String.format("%02x", it) }
    }

    //Función para mostrar un diálogo
    private fun showDialog() {
        //Crea el diálogo y define su layout
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_selection)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        //Asigna el RecyclerView
        val recyclerView = dialog.findViewById<RecyclerView>(R.id.rvList)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        //Llenamos el RecyclerView invocando al método getTowns del TownViewModel
        val viewModel = ViewModelProvider(this)[TownViewModel::class.java]
        lifecycleScope.launch {
            val towns = viewModel.getTowns()
            val adapter = ItemAdapter(towns)
            recyclerView.adapter = adapter
        }

        //Cuando se pulse una ciudad en concreto muestra su nombre en el EditText de Ciudad
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

    //Función para seleccionar un item del RecyclerView
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
}