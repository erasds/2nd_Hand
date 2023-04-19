package com.esardo.a2ndhand.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.esardo.a2ndhand.R
import com.esardo.a2ndhand.databinding.ItemChatBinding
import com.esardo.a2ndhand.model.Chat
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
    private val userId: String,
    private val chatList: List<Chat>,
    private val loadMessages: (Chat) -> Unit
) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.item_chat, parent, false))
    }

    override fun getItemCount(): Int = chatList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = chatList[position]
        holder.bind(item, userId)
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val binding = ItemChatBinding.bind(view)

        // Binds elements to it's value
        private val ivPicture = binding.ivPicture
        private val tvUserName = binding.tvUserName
        private val tvLastMsg = binding.tvLastMsg
        private val tvDate = binding.tvDate
        private var otherUserPicture: String? = ""
        private var otherUserName: String? = ""
        private var text: String? = ""
        private var date: Timestamp? = null

        fun bind (chat: Chat, userId: String) {
            val db = FirebaseFirestore.getInstance()
            val userCol = db.collection("User").document(chat.OtherUser)//.collection("Chat").document(chat.id)
            userCol.get().addOnSuccessListener { otherUser ->
                if(otherUser != null) {
                    otherUserPicture = otherUser.getString("Picture")
                    otherUserName = otherUser.getString("User")
                }
            }

            text = chat.Message.Text
            date = chat.Message.Date

            //With Picasso library this will load the User image, an image to show while data is loading,
            // and an image to show if there's an error loading the User image
            if(otherUserPicture != "") {
                Picasso.get().load(otherUserPicture).placeholder(R.drawable.prueba).error(R.drawable.prueba).into(ivPicture)
            }
            tvUserName.text = otherUserName
            tvLastMsg.text = text
            // Formatea la fecha como un String legible
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("GMT+2")
            val dateString = dateFormat.format(date?.toDate())
            tvDate.text = dateString

            //To load MessagesFragment with the data of the item clicked
            itemView.setOnClickListener { loadMessages(chat) }
        }

    }
}