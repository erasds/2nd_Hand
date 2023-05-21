package com.esardo.a2ndhand

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.esardo.a2ndhand.adapter.MessageAdapter
import com.esardo.a2ndhand.databinding.FragmentMessagesBinding
import com.esardo.a2ndhand.model.Chat
import com.esardo.a2ndhand.model.Message
import com.esardo.a2ndhand.model.User
import com.esardo.a2ndhand.viewmodel.MessageViewModel
import java.util.*

class MessagesFragment : Fragment() {
    private lateinit var _binding: FragmentMessagesBinding
    private val binding get() = _binding

    lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MessageAdapter
    private lateinit var viewModel: MessageViewModel

    private val messageList = mutableListOf<Message>()
    private var msgListSize: Int = 0
    private lateinit var userId: String
    private lateinit var chat: Chat

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        //Recibimos la referencia del usuario
        val userRef = activity?.intent?.getSerializableExtra("object") as? User
        //Recibimos el objeto Chat de los argumentos
        chat = requireArguments().getSerializable("objeto") as Chat

        viewModel = ViewModelProvider(this)[MessageViewModel::class.java]
        viewModel.getAllMessageObserver()
        //Observamos el messageList del MessageViewModel y cargamos los datos en el recyclerview
        viewModel.messageLiveData.observe(viewLifecycleOwner){
            messageList.clear()
            if (it != null) {
                messageList.addAll(it)
                binding.rvMessage.scrollToPosition(adapter.itemCount - 1)
            }
            adapter.notifyDataSetChanged()
        }

        //Guardamos el UserId de la referencia
        val userID = userRef?.id
        if (userID != null) {
            userId = userID
        }

        //Llamamos a la función getAllMessages para que se llene la lista de mensajes
        viewModel.getAllMessages(userId, chat.id)

        //Guardamos el tamaño de la lista de mensajes
        msgListSize = messageList.size

        //Cuando se pulse el botón de enviar se llama a la función que lo insertará en la base de datos
        binding.ivSend.setOnClickListener {
            if(!binding.etMessage.text.isNullOrEmpty()) {
                val text = binding.etMessage.text.toString()
                val fromUser = userId
                val toUser = chat.OtherUser
                val date = Date()
                val message = Message("", text, fromUser, toUser, date)
                viewModel.sendMessage(userId, chat.id, message)
            }
            binding.etMessage.text.clear()
        }

        initRecyclerView()
        return binding.root
    }

    //Setups RecyclerView
    private fun initRecyclerView() {
        adapter = MessageAdapter(userId, messageList)
        recyclerView = binding.rvMessage
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
    }
}