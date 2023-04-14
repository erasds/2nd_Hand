package com.esardo.a2ndhand

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.esardo.a2ndhand.adapter.MessageAdapter
import com.esardo.a2ndhand.databinding.FragmentMessagesBinding
import com.esardo.a2ndhand.model.Chat
import com.esardo.a2ndhand.model.Message
import com.esardo.a2ndhand.model.User
import com.esardo.a2ndhand.viewmodel.MessageViewModel
import com.google.firebase.Timestamp
import java.util.*

class MessagesFragment : Fragment() {
    private lateinit var _binding: FragmentMessagesBinding
    private val binding get() = _binding!!

    lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MessageAdapter
    private lateinit var viewModel: MessageViewModel

    private val messageList = mutableListOf<Message>()
    private lateinit var userId: String

    private lateinit var chat: Chat

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        //Obtain our own User reference
        val userRef = activity?.intent?.getSerializableExtra("object") as? User

        // Obtains object Chat from the arguments
        chat = requireArguments().getSerializable("objeto") as Chat

        viewModel = ViewModelProvider(this)[MessageViewModel::class.java]
        viewModel.getAllMessageObserver()
        //This will observe the messageList of the MessageViewModel class and load the necessary data into the recyclerview
        //everytime that the fragment is loaded
        viewModel.messageLiveData.observe(viewLifecycleOwner){
            messageList.clear()
            if (it != null) {
                messageList.addAll(it)
            }
            adapter.notifyDataSetChanged()
        }

        val userID = userRef?.id
        if (userID != null) {
            userId = userID
        }

        viewModel.getAllMessages(userId, chat.id)

        binding.ivSend.setOnClickListener {
            if(!binding.etMessage.text.isNullOrEmpty()) {
                val text = binding.etMessage.text.toString()
                val fromUser = userId
                val toUser = chat.OtherUser
                val date = Timestamp(Date())
                val message = Message("", text, fromUser, toUser, date)
                viewModel.sendMessage(userId, chat.id, message)
            }
            binding.etMessage.text.clear()
        }

        initRecyclerView()
        return binding.root
    }

    //Setups the RecyclerView
    private fun initRecyclerView() {
        adapter = MessageAdapter(userId, messageList)
        recyclerView = binding.rvMessage
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
    }

}