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
    private val binding get() = _binding!!

    lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatAdapter
    private lateinit var viewModel: ChatViewModel

    private val chatList = mutableListOf<Chat>()
    private lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatsBinding.inflate(inflater, container, false)
        //Obtain our own User reference
        val userRef = activity?.intent?.getSerializableExtra("object") as? User

        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        viewModel.getAllChatObserver()
        //This will observe the chatList of the ChatViewModel class and load the necessary data into the recyclerview
        //everytime that the fragment is loaded
        viewModel.chatLiveData.observe(viewLifecycleOwner){
            chatList.clear()
            if (it != null) {
                chatList.addAll(it)
            }
            adapter.notifyDataSetChanged()
        }

        val userID = userRef?.id
        if (userID != null) {
            userId = userID
        }

        viewModel.getAllChats(userId)

        initRecyclerView()
        return binding.root
    }

    //Setups the RecyclerView
    private fun initRecyclerView() {
        adapter = ChatAdapter(userId, chatList) { chat -> loadMessages(chat) }
        recyclerView = binding.rvChats
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
    }

    //Load the messages Fragment
    private fun loadMessages(chat: Chat) {
        val bundle = Bundle()
        bundle.putSerializable("objeto", chat)
        // Navigates to MessagesFragment and pass the bundle as an argument
        view?.let { Navigation.findNavController(it) }
            ?.navigate(R.id.action_chatsFragment_to_messagesFragment, bundle)
    }

}