package com.esardo.a2ndhand.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.esardo.a2ndhand.R
import com.esardo.a2ndhand.databinding.ItemChatBinding
import com.esardo.a2ndhand.model.Chat
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
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
        holder.bind(item)
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val binding = ItemChatBinding.bind(view)

        private val ivPicture = binding.ivPicture
        private val tvUserName = binding.tvUserName
        private val tvLastMsg = binding.tvLastMsg
        private val tvDate = binding.tvDate
        private var otherUserPicture: String? = ""
        private var otherUserName: String? = ""
        private var text: String? = ""
        private var date: Date? = null

        fun bind (chat: Chat) {
            val db = FirebaseFirestore.getInstance()
            val userCol = db.collection("User").document(chat.OtherUser)
            userCol.get().addOnSuccessListener { otherUser ->
                if(otherUser != null) {
                    otherUserPicture = otherUser.getString("Picture")
                    otherUserName = otherUser.getString("User")
                }

                text = chat.Message.Text
                date = chat.Message.Date

                if(otherUserPicture != "") {
                    Picasso.get().load(otherUserPicture).placeholder(R.drawable.profile).error(R.drawable.profile).into(ivPicture)
                }
                tvUserName.text = otherUserName
                tvLastMsg.text = text
                //Formatea la fecha como un String legible
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                dateFormat.timeZone = TimeZone.getTimeZone("GMT+2")
                val dateString = date?.let { dateFormat.format(it) }
                tvDate.text = dateString

                //Carga el MessagesFragment con los datos del item pulsado
                itemView.setOnClickListener { loadMessages(chat) }
            }
        }
    }
}