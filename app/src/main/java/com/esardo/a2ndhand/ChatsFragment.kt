package com.esardo.a2ndhand

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.esardo.a2ndhand.adapter.ChatAdapter
import com.esardo.a2ndhand.databinding.FragmentChatsBinding
import com.esardo.a2ndhand.model.Chat
import com.esardo.a2ndhand.model.User
import com.esardo.a2ndhand.viewmodel.ChatViewModel

class ChatsFragment : Fragment() {
    private lateinit var _binding: FragmentChatsBinding
    private val binding get() = _binding

    lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatAdapter
    private lateinit var viewModel: ChatViewModel

    private val chatList = mutableListOf<Chat>()
    private lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatsBinding.inflate(inflater, container, false)
        //Recibimos la referencia del usuario que ha iniciado sesión
        val userRef = activity?.intent?.getSerializableExtra("object") as? User

        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        viewModel.getAllChatObserver()
        //Observamos el chatList del ChatViewModel y cargamos los datos en el recyclerview
        viewModel.chatLiveData.observe(viewLifecycleOwner){
            chatList.clear()
            if (it != null) {
                chatList.addAll(it)
            }
            adapter.notifyDataSetChanged()
        }

        //Guardamos el UserId de la referencia
        val userID = userRef?.id
        if (userID != null) {
            userId = userID
        }

        //Llamamos a la función getAllChats para que se llene la lista de chats
        viewModel.getAllChats(userId)

        initRecyclerView()
        return binding.root
    }

    //Setups RecyclerView
    private fun initRecyclerView() {
        adapter = ChatAdapter(chatList) { chat -> loadMessages(chat) }
        recyclerView = binding.rvChats
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
    }

    //Carga el fragmento de los mensajes
    private fun loadMessages(chat: Chat) {
        val bundle = Bundle()
        bundle.putSerializable("objeto", chat)
        //Navega al MessagesFragment y le pasa el bundle como argumento
        view?.let { Navigation.findNavController(it) }
            ?.navigate(R.id.action_chatsFragment_to_messagesFragment, bundle)
    }
}