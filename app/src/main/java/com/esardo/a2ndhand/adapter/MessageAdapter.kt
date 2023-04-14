package com.esardo.a2ndhand.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.esardo.a2ndhand.R
import com.esardo.a2ndhand.databinding.ItemMessageReceivedBinding
import com.esardo.a2ndhand.databinding.ItemMessageSendedBinding
import com.esardo.a2ndhand.model.Message
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(
    private val userId: String,
    private val messageList: List<Message>
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ITEM_SENDED = 1
    private val  ITEM_RECEIVED= 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == 1) {
            val layoutInflater = LayoutInflater.from(parent.context)
            return SendedViewHolder(layoutInflater.inflate(R.layout.item_message_sended, parent, false))
        } else {
            val layoutInflater = LayoutInflater.from(parent.context)
            return ReceivedViewHolder(layoutInflater.inflate(R.layout.item_message_received, parent, false))
        }
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]
        return if(userId == currentMessage.FromUser) {
            ITEM_SENDED
        } else {
            ITEM_RECEIVED
        }
    }

    override fun getItemCount(): Int = messageList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder.javaClass == SendedViewHolder::class.java) {
            val viewHolder = holder as SendedViewHolder
            val item = messageList[position]
            viewHolder.bind(item, userId)
        } else {
            val viewHolder = holder as ReceivedViewHolder
            val item = messageList[position]
            viewHolder.bind(item, userId)
        }

    }

    inner class SendedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = ItemMessageSendedBinding.bind(view)
        fun bind (message: Message, userId: String) {
            if (message.FromUser == userId) {
                // Formatea la fecha como un String legible
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                val dateString = dateFormat.format(message.Date?.toDate())
                binding.tvDate.text = dateString
                binding.tvMessage.text = message.Text
            }
        }
    }

    inner class ReceivedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = ItemMessageReceivedBinding.bind(view)
        fun bind (message: Message, userId: String) {
            if (message.FromUser != userId) {
                // Formatea la fecha como un String legible
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                val dateString = dateFormat.format(message.Date?.toDate())
                binding.tvDate.text = dateString
                binding.tvMessage.text = message.Text
            }
        }
    }


}